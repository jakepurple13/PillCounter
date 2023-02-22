#!/usr/bin/env bash

#Step 1 - Download load cell/weight sensor scripts
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/hx711_example.py >> ~/Desktop/hx711_example.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/emulated_hx711.py >> ~/Desktop/emulated_hx711.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/hx711.py >> ~/Desktop/hx711.py

#Step 2 - Download and set autostart server
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/pillcounter.desktop >> ~/Desktop/pillcounter.desktop
sudo mv ~/Desktop/pillcounter.desktop /etc/xdg/autostart/pillcounter.desktop

#Step 3 - Download server
curl https://github.com/jakepurple13/PillCounter/releases/download/v0.0.1/pillcounter.jar -o ~/Desktop/pillcounter.jar

#Step 4 - Java installing
yes | sudo apt update
yes | sudo apt install default-jdk

#Step 5 - Setup Balena Wifi-Connect?

#Final Step
rm -f ~/Desktop/full_setup.sh