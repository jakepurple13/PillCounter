# SPDX-FileCopyrightText: 2021 ladyada for Adafruit Industries
# SPDX-License-Identifier: MIT

import board
import time
from digitalio import DigitalInOut, Direction

# create two buttons
switch1 = DigitalInOut(board.D6)
switch2 = DigitalInOut(board.D5)
switch1.direction = Direction.INPUT
switch2.direction = Direction.INPUT

while True:
    if not switch1.value:
        print("Switch 1")
        while not switch1.value:
            time.sleep(0.01)
    if not switch2.value:
        print("Switch 2")
        while not switch2.value:
            time.sleep(0.01)
    time.sleep(0.01)
