import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
GPIO.setup(18, GPIO.OUT)
GPIO.setup(17, GPIO.OUT)

if GPIO.input(17):
    print("Pin 11 is HIGH")
else:
    print("Pin 11 is LOW")

GPIO.cleanup()