WikipOff
========

Offline Wikipedia android browser.
Not working yet.

Introduction
------------

WikipOff is an offline Wikipedia browser for android.


DISCLAIMER
----------

I am not:
- an android developer
- a Java developer
- a developer.

Everything here has mostly been written by copy-pasting code from stackoverflow.com and bashing ctrl-space shortcut in Eclipse.


Preparing Databases
-------------------

- download  `http://dumps.wikimedia.org/<lang>/lates/<lang>-latest-pages-articles.xml.bz2`
- strip off stuff `bzcat <lang>-latest-pages-articles.xml.bz2 | python tools/WikiExtractor.py -l -s -o .`   # ~ 2h30 for french wiki
- extract text, compress it with lzma `ruby tools/ruby to_db.rb` # ~ 9h
- send the database to your device `adb push <lang>.sqlite /mnt/sdcard/fr.renzo.wikipoff/databases/<lang>.sqlite`


LICENSE
--------
GPL V2 looks fine
