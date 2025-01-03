#include <stdio.h>
#include <string.h>

int drive(char * command) {
    if (strstr(command, "UP") != NULL) {
        printf("DRIVIN UP\n");
        return 0;
    }

    if (strstr(command, "DOWN") != NULL) {
        printf("DRIVIN DOWN\n");
        return 0;
    }

    if (strstr(command, "RIGHT") != NULL) {
        printf("DRIVIN RIGHT\n");
        return 0;
    }

    if (strstr(command, "LEFT") != NULL) {
        printf("DRIVIN LEFT\n");
        return 0;
    }

    printf("ready for battle\n");
    return 1;
}
