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
Unfortunately, the app isn't compatible with my [ParanoidAndroid](http://paranoidandroid.co/roms/mako/) ROM without Gapps. So I had to make my own.

## Donate
I accept donations in beer, litecoins, angry and happy emails.
* LTC: LYAaCu2SuPA36QDZrjvYCK8HcVHXxYVmfu
* happy emails : wikipoff@renzokuken.eu
* angry emails : devnull@renzokuken.eu


