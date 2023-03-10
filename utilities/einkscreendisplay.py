import board
import busio
import digitalio
import sys
from adafruit_epd.epd import Adafruit_EPD

spi = busio.SPI(board.SCK, MOSI=board.MOSI, MISO=board.MISO)
ecs = digitalio.DigitalInOut(board.CE0)
dc = digitalio.DigitalInOut(board.D22)
rst = digitalio.DigitalInOut(board.D27)
busy = digitalio.DigitalInOut(board.D17)
srcs = None

from adafruit_epd.ssd1680 import Adafruit_SSD1680

display = Adafruit_SSD1680(122, 250, spi, cs_pin=ecs, dc_pin=dc, sramcs_pin=srcs,
                           rst_pin=rst, busy_pin=busy)

display.rotation = 1

display.fill(Adafruit_EPD.WHITE)

displaySize = 2

# string, x, y, color, font name, size
# IP
display.text(sys.argv[1], 10, 10, Adafruit_EPD.BLACK, size=displaySize)

# name of config
display.text(sys.argv[2], 10, 34, Adafruit_EPD.BLACK, size=displaySize)

# pill count
display.text(sys.argv[3], 10, 58, Adafruit_EPD.BLACK, size=displaySize)

# extra
display.text(sys.argv[4], 10, 82, Adafruit_EPD.BLACK, size=displaySize)

# version
display.text(sys.argv[5], 10, 106, Adafruit_EPD.BLACK, size=displaySize)

display.display()
