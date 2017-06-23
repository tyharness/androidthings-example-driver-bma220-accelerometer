Example-Driver-BMA220-accelerometer Rev41 for Android Things(TM) Dev. Prev 41 on the Raspberry Pi 3
----------------------------------------------------------------------------------------------

You only need these files if you want to modify the driver else the sample app apk already contains the driver. The driver app is an aar file contained in the app/libs folder.  

You can find the sample app here:
https://github.com/tyharness/androidthings-sample-app-bma220-accelerometer


If you want the latest driver version please visit and download the above driver and copy the bma220-debug.aar file over to the app/libs folder.

Please note this driver is a demo quality only and no where near production ready -- Use with caution.

----------------------------------------------------------------------------------------------
Prerequisties

Raspberry Pi 3 board with Android Things Dev. Prev. 4

Android Things is in Preview only.  To use you must agree to the Android Things T&C's
https://developer.android.com/things/terms.html

other boards ... to do

Bosch BMA220 - Triple Axis Accelerometer BMA220(Tiny) SKU:SEN0168

https://www.dfrobot.com/wiki/index.php/Triple_Axis_Accelerometer_BMA220(Tiny)_SKU:SEN0168

-----------------------------------------------------------------------------------------------

Wiring:
Please see schematic  .... to do

BMA220(Tiny) 3.3V--- 3.3V  rpi pin

BMA220(Tiny) GND --- GND   rpi pin

BMA220(Tiny) SDA --- SDA   rpi pin

BMA220(Tiny) SCK --- SCK   rpi pin


-----------------------------------------------------------------------------------------------

Install and Build from the command line:

./gradlew build

the bma220-debug.aar driver file resides in build/outputs/aar
copy this over to the sample app app/libs folder.



