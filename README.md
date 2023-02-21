# PillCounter

Look at https://tutorials-raspberrypi.com/digital-raspberry-pi-scale-weight-sensor-hx711/!!!

# Instructions

Software instructions to get server working!

1. Run
   1. `sudo apt update`
   2. `sudo apt install default-jdk`

2. `utilities/pillcounter.desktop` needs to go into
   `/etc/xdg/autostart/pillcounter.desktop`
   in order to allow the server to start on reboot

3. Move `utilties/emulated_hx711.py`, `utilties/hx711.py`, and `utilties/hx711_example.py`
   to the desktop

4. Move `PillCounter/build/libs/pillcounter.jar` to the desktop

5. Run
   1. `bash <(curl -L https://github.com/balena-io/wifi-connect/raw/master/scripts/raspbian-install.sh)`

# Making use of

1. https://github.com/tatobari/hx711py
    1. For hx711 communication
2. https://github.com/JetBrains/compose-jb
    1. For UI https://github.com/jakepurple13/PillCounterApplication
3. https://www.raspberrypi.com/software/
   1. Raspberry Pi OS
4. https://github.com/balena-os/wifi-connect
   1. This is the auto wi-fi connect setup to allow hooking the device up to a wi-fi connection