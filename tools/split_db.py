#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys
import math
import re
import os.path
import base64
import sqlite3
import getopt
import datetime
from WikiExtractor import OutputSqlite 
languagedb="languages.sqlite"

nb_select=100

def show_usage():
    print """
This script was create because (stock) Android still only handles
obsolete file systems for external storage (FAT32 bitch!).

(Un)fortunately, some Wikis are huge (english will make a ~8Gb sqlite file),
and won't copy on your SDCard. 

This script will try to 
"""


def main():
    try:
        long_opts = ['size=', "db=","lang="]
        opts, args = getopt.gnu_getopt(sys.argv[1:], 's:d:l:', long_opts)
    except getopt.GetoptError:
        show_usage()
        sys.exit(1)

    for opt, arg in opts:
        if opt in ('-s', '--size'):
            desired_size=float(arg)
        if opt in ('-d', '--db'):
            sqlite_file=arg
        if opt in ('-l', '--lang'):
            lang=arg

    if not 'lang' in locals():
        print("If you don't specify a language with -l, you're gonna have a bad time")
        sys.exit()

    if not 'sqlite_file' in locals():
        print("Please give me a sqlite file to split with -d or --db")
        sys.exit()
    if not 'desired_size' in locals():
        print("No max file size given. Will use FAT32 4GB limit. Please give me a max file size -s or --size if that's not what you want")
        desired_size=float(2**32 - 1)

    if desired_size==0:
        print("No. That's not fun.")
        sys.exit(666)

    if not os.path.isfile(languagedb):
        print("%s doesn't exists. Please create it."%languagedb)
        sys.exit(1)

    lolpython=re.compile(r'^(.+)\.sqlite$')
    srsly_python_go_home_you_are_drunk=lolpython.match(sqlite_file)
    root_name="wiki"
    if (srsly_python_go_home_you_are_drunk == None):
        print "Input file name doesn't end with .sqlite"
        sys.exit(1)
    else:
        root_name=srsly_python_go_home_you_are_drunk.group(1)


    input_db_size=os.stat(sqlite_file).st_size
    max_main_table_size=desired_size*90/100 # We need space for indexes and FTS table
    final_db_count=math.ceil(input_db_size/max_main_table_size)
    if final_db_count==1:
        print "no need to split"
        sys.exit(0)
#    print "We'll split into %d files"%final_db_count

    conn_input = sqlite3.connect(sqlite_file)
    curs_input = conn_input.cursor()
    page_size=curs_input.execute("PRAGMA page_size").fetchone()[0]
    default_max_page_count=curs_input.execute("PRAGMA max_page_count").fetchone()[0]
    
    st = datetime.datetime.now() 
    max_output_page_count = max_main_table_size/page_size

    curr_index=1
    offset=0
    row_count=curs_input.execute("SELECT count(*) from articles").fetchone()[0]
    curr_output_sqlitefile=root_name+"-%d.sqlite"%curr_index
    curr_output = OutputSqlite(curr_output_sqlitefile,languagedb,max_output_page_count)
    curr_output.set_lang(lang)
    while offset < row_count:
        res=curs_input.execute("SELECT title,text FROM articles LIMIT %d OFFSET %d "%(nb_select,offset))
        for t,tt in res:
            offset+=1
            try: 
                curr_output.write(t,tt,True)
            except sqlite3.OperationalError as e:
                if (e.message=="database or disk is full"):
                    print "%s is full"%curr_output_sqlitefile
                    curr_output.set_max_page_count(default_max_page_count)
                    curr_output.write(t,tt,True)
                    curr_output.close()
                    curr_index+=1
                    curr_output_sqlitefile=root_name+"-%d.sqlite"%curr_index
                    print "Opening new DB : %s"%curr_output_sqlitefile
                    curr_output = OutputSqlite(curr_output_sqlitefile,languagedb,max_output_page_count)
        percent = offset*100.0/row_count
        delta=((100-percent)*(datetime.datetime.now()-st).total_seconds())/percent
        status_s= "%.02f%% ETA=%s\r"%(percent, str(datetime.timedelta(seconds=delta)))
        sys.stdout.write(status_s)
    curr_output.close()



    


if __name__ == '__main__':
    main()
