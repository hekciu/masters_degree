#include <stdio.h>
#include <string.h>
#include "driver/gpio.h"

#define PIN_RIGHT_0 GPIO_NUM_9
#define PIN_RIGHT_1 GPIO_NUM_10
#define PIN_LEFT_0 GPIO_NUM_11
#define PIN_LEFT_1 GPIO_NUM_12

void drive_setup() {
    gpio_set_direction(PIN_RIGHT_0, GPIO_MODE_OUTPUT);
    gpio_set_direction(PIN_RIGHT_1, GPIO_MODE_OUTPUT);
    gpio_set_direction(PIN_LEFT_0, GPIO_MODE_OUTPUT);
    gpio_set_direction(PIN_LEFT_1, GPIO_MODE_OUTPUT);
}

void drive_reset() {
    gpio_set_level(PIN_RIGHT_0, 0);
    gpio_set_level(PIN_RIGHT_1, 0);
    gpio_set_level(PIN_LEFT_0, 0);
    gpio_set_level(PIN_LEFT_1, 0);
}

int drive(char * command) {
    if (strstr(command, "UP") != NULL) {
        printf("DRIVIN UP\n");
        drive_reset();
        gpio_set_level(PIN_RIGHT_0, 1);
        gpio_set_level(PIN_LEFT_0, 1);
        return 0;
    }

    if (strstr(command, "DOWN") != NULL) {
        printf("DRIVIN DOWN\n");
        drive_reset();
        gpio_set_level(PIN_RIGHT_1, 1);
        gpio_set_level(PIN_LEFT_1, 1);
        return 0;
    }

    if (strstr(command, "RIGHT") != NULL) {
        printf("DRIVIN RIGHT\n");
        drive_reset();
        gpio_set_level(PIN_RIGHT_1, 1);
        gpio_set_level(PIN_LEFT_0, 1);
        return 0;
    }

    if (strstr(command, "LEFT") != NULL) {
        printf("DRIVIN LEFT\n");
        drive_reset();
        gpio_set_level(PIN_RIGHT_0, 1);
        gpio_set_level(PIN_LEFT_1, 1);
        return 0;
    }

    drive_reset();
    printf("ready for battle\n");
    return 1;
}
