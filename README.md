# PillCounter

Look at https://tutorials-raspberrypi.com/digital-raspberry-pi-scale-weight-sensor-hx711/!!!

# Instructions

Software instructions to get server working!

1. `utilities/pillcounter.desktop` needs to go into
   `/etc/xdg/autostart/pillcounter.desktop`
   in order to allow the server to start on reboot

2. Move `utilties/emulated_hx711.py`, `utilties/hx711.py`, and `utilties/hx711_example.py`
   to the desktop

3. Move `PillCounter/build/libs/pillcounter.jar` to the desktop

# Making use of

1. https://github.com/tatobari/hx711py
    1. For hx711 communication
2. https://github.com/JetBrains/compose-jb
    1. For UI https://github.com/jakepurple13/PillCounterApplication
3. https://www.raspberrypi.com/software/
    1. Raspberry Pi OS