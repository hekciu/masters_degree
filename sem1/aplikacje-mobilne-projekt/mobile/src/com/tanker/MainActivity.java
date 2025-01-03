package com.tanker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Color;
import android.content.Intent;
import android.util.Log;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;


public class MainActivity extends Activity {
    private static final String LOG_TAG = "tanker_app";
    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private Intent bindBluetoothServiceIntent;

    BluetoothService bluetoothService = null;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            bluetoothService = ((BluetoothService.LocalBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            bluetoothService = null;
        }
    };

    public void drive(BluetoothService.DriveKey key) {
        Log.d(LOG_TAG, "DRIVEEEEE");
        if (this.bluetoothService == null) {
            Log.d(LOG_TAG, "tried to send message to service, but this.bluetoothService is null");
            return;
        }

        this.bluetoothService.setDriveData(key);
        this.bluetoothService.sendData();
    }

    public void stop() {
        if (this.bluetoothService == null) {
            Log.d(LOG_TAG, "tried to send message to service, but this.bluetoothService is null");
            return;
        }

        this.bluetoothService.resetDriveData();
        this.bluetoothService.sendData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.manager = getSystemService(BluetoothManager.class);
        this.adapter = manager.getAdapter();
        this.bindBluetoothServiceIntent = new Intent(MainActivity.this, BluetoothService.class);

        setContentView(R.layout.activity_main);

        if (this.adapter == null) {
            Log.d(LOG_TAG, "Could not get bluetooth adapter");
            return;
        }

        Log.d(LOG_TAG, "Successfully got bluetooth adapter");

        if (!this.adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(this.adapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            bindService(this.bindBluetoothServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }

        Button buttonRight = findViewById(R.id.buttonRight);
        Button buttonLeft = findViewById(R.id.buttonLeft);
        Button buttonUp = findViewById(R.id.buttonUp);
        Button buttonDown = findViewById(R.id.buttonDown);

        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    drive(BluetoothService.DriveKey.LEFT); 
                    Log.d(LOG_TAG, "DRIVIN LEFT"); 
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stop(); 
                    Log.d(LOG_TAG, "DRIVIN LEFT STOPPED"); 
                }

                return false;
            }
        });

        buttonUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    drive(BluetoothService.DriveKey.UP); 
                    Log.d(LOG_TAG, "DRIVIN UP"); 
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stop(); 
                    Log.d(LOG_TAG, "DRIVIN UP STOPPED"); 
                }

                return false;
            }
        });

        buttonDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    drive(BluetoothService.DriveKey.DOWN); 
                    Log.d(LOG_TAG, "DRIVIN DOWN"); 
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stop(); 
                    Log.d(LOG_TAG, "DRIVIN DOWN STOPPED"); 
                }

                return false;
            }
        });

        buttonRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    drive(BluetoothService.DriveKey.RIGHT); 
                    Log.d(LOG_TAG, "DRIVIN RIGHT"); 
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stop(); 
                    Log.d(LOG_TAG, "DRIVIN RIGHT STOPPED"); 
                }

                return false;
            }
        });
    }

    @Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // idk why it's -1 on user's acceptance but it is what it is
        if (requestCode == REQUEST_ENABLE_BT && resultCode == -1) {
            Log.d(LOG_TAG, "Successfully enabled bluetooth");
            bindService(this.bindBluetoothServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
