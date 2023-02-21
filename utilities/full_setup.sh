#!/usr/bin/env bash

curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/hx711_example.py >> ~/Desktop/hx711_example.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/emulated_hx711.py >> ~/Desktop/emulated_hx711.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/hx711.py >> ~/Desktop/hx711.py
curl https://raw.githubusercontent.com/jakepurple13/PillCounter/main/utilities/pillcounter.desktop >> ~/Desktop/pillcounter.desktop
sudo mv ~/Desktop/pillcounter.desktop /etc/xdg/autostart/pillcounter.desktop

sudo apt update
sudo apt install default-jdk

curl https://github.com/jakepurple13/PillCounter/releases/download/v0.0.1/pillcounter.jar -o ~/Desktop/pillcounter.jar