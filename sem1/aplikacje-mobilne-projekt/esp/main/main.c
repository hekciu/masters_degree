#include <stdio.h>
#include "bluetooth.h"
#include "drive.h"
#include "nvs_flash.h"

#define STACK_SIZE 4096
#define DRIVE_COMMAND_PERIOD_MS 100


void drive_task(void * _) {
    for (;;) {
        vTaskDelay(DRIVE_COMMAND_PERIOD_MS);

        char * commandString;

        esp_gatt_status_t status = esp_ble_gatts_get_attr_value(tanker_handle_table[IDX_CHAR_VAL_MOVEMENT], &MOVEMENT_STRING_SIZE, (const uint8_t **)&commandString);

        if (status != ESP_OK) {
            ESP_LOGE(GATTS_TABLE_TAG, "esp function esp_ble_gatts_get_attr_value failed with code: %d, skipping flash_leds_task", status);   
            continue;
        }

        drive(commandString);
    }
}

void app_main(void) {
    printf("starting\n");

    BaseType_t xReturned;
    TaskHandle_t xHandle = NULL;

    esp_err_t ret;

    /* Initialize NVS. */
    ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK( ret );

    esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
    ret = esp_bt_controller_init(&bt_cfg);

    if (ret) {
        ESP_LOGE(GATTS_TABLE_TAG, "%s init controller failed: %s", __func__, esp_err_to_name(ret));
    }

    ret = esp_bt_controller_enable(ESP_BT_MODE_BLE);
    if (ret) {
        ESP_LOGE(GATTS_TABLE_TAG, "%s enable controller failed: %s", __func__, esp_err_to_name(ret));
        return;
    }

    ret = esp_bluedroid_init();
    if (ret) {
        ESP_LOGE(GATTS_TABLE_TAG, "%s init bluetooth failed: %s", __func__, esp_err_to_name(ret));
        return;
    }

    ret = esp_bluedroid_enable();
    if (ret) {
        ESP_LOGE(GATTS_TABLE_TAG, "%s enable bluetooth failed: %s", __func__, esp_err_to_name(ret));
        return;
    }

    ret = esp_ble_gatts_register_callback(gatts_event_handler);
    if (ret){
        ESP_LOGE(GATTS_TABLE_TAG, "gatts register error, error code = %x", ret);
        return;
    }

    ret = esp_ble_gap_register_callback(gap_event_handler);
    if (ret){
        ESP_LOGE(GATTS_TABLE_TAG, "gap register error, error code = %x", ret);
        return;
    }

    ret = esp_ble_gatts_app_register(ESP_APP_ID);
    if (ret){
        ESP_LOGE(GATTS_TABLE_TAG, "gatts app register error, error code = %x", ret);
        return;
    }

    esp_err_t local_mtu_ret = esp_ble_gatt_set_local_mtu(500);
    if (local_mtu_ret){
        ESP_LOGE(GATTS_TABLE_TAG, "set local  MTU failed, error code = %x", local_mtu_ret);
    }


    xReturned = xTaskCreate(drive_task, "drive_task", STACK_SIZE, NULL, configMAX_PRIORITIES - 1, &xHandle);
}
