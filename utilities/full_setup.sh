#!/usr/bin/env bash

#Step 1 - Download load cell/weight sensor scripts
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/hx711_example.py >> ~/Desktop/hx711_example.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/emulated_hx711.py >> ~/Desktop/emulated_hx711.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/hx711.py >> ~/Desktop/hx711.py

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

#Final Step
#sudo reboot