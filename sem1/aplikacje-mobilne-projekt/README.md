## Building mobile app:
### If first time on new machine use this script: https://github.com/hekciu/leds/blob/master/mobile/init.sh
- sudo ./generate_keys.sh # once
- cp mobile/com/tanker/Secrets.java.template mobile/com/tanker/Secrets.java
#### Fill Secrets.java with esp32 mac address
### Later:
- sudo ./build.sh
- ./run.sh
- ./monitor.sh

