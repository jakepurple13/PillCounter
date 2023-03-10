#!/usr/bin/env bash

#Step 1 - Download load cell/weight sensor scripts
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/hx711_example.py >> ~/Desktop/hx711_example.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/emulated_hx711.py >> ~/Desktop/emulated_hx711.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/hx711.py >> ~/Desktop/hx711.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/einkscreendisplay.py >> ~/Desktop/hx711.py

#Step 2 - Download and set autostart server
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/pillcounter.desktop >> ~/Desktop/pillcounter.desktop
sudo mv ~/Desktop/pillcounter.desktop /etc/xdg/autostart/pillcounter.desktop

#Step 3 - Download server
# Work on this!
#curl https://github.com/jakepurple13/PillCounter/releases/download/v0.0.1/pillcounter.jar -o ~/Desktop/pillcounter.jar

#Step 4 - Java installing
yes | sudo apt update
yes | sudo apt install default-jdk

#Step 5 - Setup Wifi-Connect
echo "deb http://repository.nymea.io bullseye rpi" | sudo tee /etc/apt/sources.list.d/nymea.list
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-key A1A19ED6

yes | sudo apt-get update
yes | sudo apt-get install nymea-networkmanager dirmngr

sudo systemctl disable dhcpcd

#yes | bash <(curl -L https://github.com/balena-io/wifi-connect/raw/master/scripts/raspbian-install.sh)

# Step 5.1 - Set the Discoverable Timeout to 0 so it can always be discoverable
sed -i 's/^#DiscoverableTimeout = 0/DiscoverableTimeout = 0/g' /etc/bluetooth/main.conf

# Step 6 - Install support for screen
sudo pip3 install adafruit-circuitpython-bitmap_font
sudo pip3 install adafruit-circuitpython-framebuf
sudo pip3 install adafruit-circuitpython-lis3dh
sudo pip3 install adafruit-circuitpython-busdevice
sudo pip3 install adafruit-circuitpython-epd
sudo apt-get install python3-pil

wget https://github.com/adafruit/Adafruit_CircuitPython_framebuf/raw/main/examples/font5x8.bin

cd ~
sudo pip3 install --upgrade adafruit-python-shell
wget https://raw.githubusercontent.com/adafruit/Raspberry-Pi-Installer-Scripts/master/raspi-blinka.py
yes | sudo python3 raspi-blinka.py

#Final Step
#sudo reboot