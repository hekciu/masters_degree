package com.tanker;

import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.os.Handler;
import android.util.Log;


public class BluetoothService extends Service {
    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private Handler handler;
    private String espMacAddress;
    private String driveCharacteristicUuid = "0000ff01-0000-1000-8000-00805f9b34fb";
    private BluetoothGatt bluetoothGatt = null;
    private BluetoothGattCharacteristic driveCharacteristic = null;
    private String driveString;
    private static final String LOG_TAG = "tanker_app";
    
    public enum DriveKey {
        UP,
        DOWN,
        RIGHT,
        LEFT
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "Starting BluetoothService"); 
        this.manager = getSystemService(BluetoothManager.class);
        this.adapter = manager.getAdapter();
        this.scanner = this.adapter.getBluetoothLeScanner();
        this.scanCallback = this.getScanCallback();
        this.resetDriveData();

        this.espMacAddress = Secrets.getAddress();

        this.scanner.startScan(this.scanCallback);

        return binder;
    }

    private ScanCallback scanCallback;

    private ScanCallback getScanCallback() {
        BluetoothService serviceInstance = this; 

        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanFailed(int errorCode) {
                Log.d(LOG_TAG, "BLE scan failed with code: " + errorCode);
            }

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                serviceInstance.handleScanResult(result);
            }
        
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    serviceInstance.handleScanResult(result);
                }
            }
        }; 

        return scanCallback;
    }

    public void setDriveData(DriveKey key) {
        switch(key) {
            case UP:
                this.driveString = "up";
                break;
            case DOWN:
                this.driveString = "down";
                break;
            case LEFT:
                this.driveString = "left";
                break;
            case RIGHT:
                this.driveString = "right";
                break;
        }
    }

    public void resetDriveData() {
        this.driveString = "";
    }

    public void sendData() {
        if (this.driveCharacteristic == null || this.bluetoothGatt == null) {
            Log.d(LOG_TAG, "could not sendData - driveCharacteristic or bluetoothGatt is null");
            return; 
        }

        try {
            Log.d(LOG_TAG, "writing to esp, drive data: " + this.driveString);

            this.driveCharacteristic.setValue(this.driveString);

            this.bluetoothGatt.writeCharacteristic(this.driveCharacteristic);

            Log.d(LOG_TAG, "data written");
        } catch (Exception e) {
            Log.d(LOG_TAG, "an error occured while writing drive string bytes to characteristic");
            Log.d(LOG_TAG, "error details: "+e.toString());
        }
    }

    private void handleScanResult(ScanResult result) {
        BluetoothDevice device = result.getDevice();

        Log.d(LOG_TAG, "Found device with address: " + device.getAddress());

        if (!device.getAddress().equals(this.espMacAddress)) {
            return;
        }

        this.scanner.stopScan(this.scanCallback);

        Log.d(LOG_TAG, "Esp found");

        BluetoothService serviceInstance = this;

        this.bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else {
                    gatt.close();

                    serviceInstance.scanner.startScan(serviceInstance.scanCallback);
                }
            }

            @Override 
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.d(LOG_TAG, "services discovered");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    List<BluetoothGattService> services = gatt.getServices();
                    for (BluetoothGattService service : services) {
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : characteristics) { 
                            if (characteristic.getUuid().toString().equals(serviceInstance.driveCharacteristicUuid)) {
                                Log.d(LOG_TAG, "found characteristic matching driveCharacteristic uuid");
                                serviceInstance.driveCharacteristic = characteristic;

                                serviceInstance.sendData();
                            }
                        }
                    }
                }
            }
        }, 2); // TRANSPORT_LE == 2
    }
}


