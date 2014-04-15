# WikipOff

## About
This app is an offline [Wikipedia](https://www.wikipedia.org/) reader. It helps any one with a few Gb of space on their android storage to be always right in arguments, whether or not they have a dataplan.

# WARNING
The whole app and DB building process is **heavily** french wiki oriented. Your mileage may vary.

I am not:
- an android developer
- a Java developer
- a developer.

Everything here has mostly been written by copy-pasting code from stackoverflow.com and bashing ctrl-space shortcut in Eclipse.

## Getting Started
1. `git clone https://github.com/conchyliculture/wikipoff.git`
2. `cd wikipoff` 
3. `export ANDROID_HOME=<path/to/sdk>; ant debug`
4. `ant install bin/fr.renzo.wikipoff-debug.apk`
5. `cd tool`
6. `make.sh` and follow instructions
7. `adb push wiki.sqlite /mnt/sdcard/fr.renzo.wikipoff/databases/
8. enjoy

## License
GPLv3. Get it, hack it, compile it, share it.

## Why
I was used to have [WikiDroyd](https://play.google.com/store/apps/details?id=com.osa.android.wikidroyd) for winning arguments and to have something to read when I don't have my ebook reader.
Unfortunately, the app isn't compatible with my [ParanoidAndroid](http://paranoidandroid.co/roms/mako/) ROM without Gapps. 
There are tons of offline readers on Google Playstore, but not on F-Droid. I would have had to get the corresponding APK (some are 4Gb because they ship with the whole database...), test them, check if I could update my own DB, etc.... 
It looked easier/funnier to make my own app.

## Donate
I accept donations in beer, various cryptcoins, angry and happy emails.
* AUR   AH9hYc6BxHNxqGWn21Gmv8Q3ztDtnWurSo
* BTC   1BAaxTvK1jkoFKf7qWF2C6M4UX1y86MxaF
* DGC   DQ1WiuWKwj8g5NYdq8PbzRaVFckm8TX7Sc
* DOGE  DAQhTKVj592GrjbzYgogDyiBAHm6t6HpiQ 
* FTC   6znenYP8Ry3sv1Mr7F2dgkuZmfvWwkgcss
* LTC   LYAaCu2SuPA36QDZrjvYCK8HcVHXxYVmfu
* MOON  2Pb3KvJ61vj9qcCkQj565owveAVuwctfdB
* NVC   4ad6a9Uwim8RhLn9tX4ouLSNGxo5chu2g8
* TIPS  EYKUTRAoum6f4rGxJZaGn8GnZdWs1amHwH
* VTC   VvEZ7iUrZZR8bhFLCyCqA3LbPUiM15oDrj
* WDC   WiCH7zuAwrS4EQ78dJrqrqLb8ESeEmgECc
* YAC   Y2fgdMign7vvjZzztWRZsWzWkJqTHSDat9
* happy emails : wikipoff@renzokuken.eu
* angry emails : devnull@renzokuken.eu


