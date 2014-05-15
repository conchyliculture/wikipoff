# Tools

You'll find here a few tools to build your own Wiki databases

## WikiExtractor.py

I've originally snatched this script from [Here](http://medialab.di.unipi.it/wiki/Wikipedia_Extractor).
Its purpose is to take a Wikipedia XML dump file and convert it to a sqlite database file.
It will:
* Loop over each article and redirect page
* Convert the wikicode to HTML (with the help of lib/wiki<lang>.py files)
* Compress the text using LZMA
* Store it in the database

It's been tested on French, Basque, Friulian, and English dumps.

Example use:

    python WikiExtractor.py -x enwiki-latest-pages-articles.xml -d en.wiki.sqlite

### Troubleshooting

How to fix: `Error: database or disk is full (for example after the VACUUM command)`
SQlite will use PRAGMA temp_store_directory; for its temporary work. It defaults to /tmp.
If your /tmp is lacking some space, you can do:

    sqlite> PRAGMA temp_store_directory = '<some place with disk space>';
    sqlite> PRAGMA temp_store =1;
    sqlite> vacuum;

## WikiConvert.py

This scripts helps tracking converting issues. It's able to parse the raw xml file and, with the use of a helper database (which contains locations of articles in the XML file), displays outputs of the wikicode to HTML conversion.

Examples:
Show the HTML conversion output of an article with title `Algorithme` :

    python ConvertArticle.py  -d helper.sqlite -f /raid/incoming/tests_wiki/frwiki-latest-pages-articles.xml -l fr -t Algorithme

Show the raw wikicode article with title `Algorithme` :

    python ConvertArticle.py  -d helper.sqlite -f /raid/incoming/tests_wiki/frwiki-latest-pages-articles.xml -l fr -t Algorithme -r

## split_db.py

This scripts helps spliting huge (read: bigger than FAT32 max file size) sqlite databases into 2

Example:

    python split_db.py -l en -d en.wiki.sqlite
