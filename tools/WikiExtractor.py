#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# =============================================================================
#  Version: Lorraine (21 Jan 2014
#   Heavily Hacked by renzo for the need of https://github.com/conchyliculture/wikipoff
#
#  Version: 2.6 (Oct 14, 2013)
#  Author: Giuseppe Attardi (attardi@di.unipi.it), University of Pisa
#	   Antonio Fuschetto (fuschett@di.unipi.it), University of Pisa
#
#  Contributors:
#	Leonardo Souza (lsouza@amtera.com.br)
#	Juan Manuel Caicedo (juan@cavorite.com)
#	Humberto Pereira (begini@gmail.com)
#	Siegfried-A. Gevatter (siegfried@gevatter.com)
#	Pedro Assis (pedroh2306@gmail.com)
#
# =============================================================================
#  Copyright (c) 2009. Giuseppe Attardi (attardi@di.unipi.it).
# =============================================================================
#  This file is part of Tanl.
#
#  Tanl is free software; you can redistribute it and/or modify it
#  under the terms of the GNU General Public License, version 3,
#  as published by the Free Software Foundation.
#
#  Tanl is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
# =============================================================================

"""Wikipedia Extractor:
Extracts and cleans text from Wikipedia database dump and stores output in a
sqlite database

Usage:
  WikiExtractor.py [options]

Options:
  -c, --compress        : compress output files using bzip
  -b, --bytes= n[KM]    : put specified bytes per output file (default 500K)
  -B, --base= URL       : base URL for the Wikipedia pages
  -l, --link            : preserve links
  -n NS, --ns NS        : accepted namespaces (separated by commas)
  -o, --output= dir     : place output files in specified directory (default
                          current)
  -s, --sections	: preserve sections
  -h, --help            : display this help and exit
"""
import sys
sys.path.append("./lib")
sys.path.append("./lib/python2.7/site-packages/pylzma-0.4.4-py2.7-linux-x86_64.egg/")
import gc
import getopt
import urllib
import re
import struct
from cStringIO import StringIO
import bz2
import os.path
import base64
import sqlite3
import wikitools
import wikiglobals
import pylzma
from time import strftime


#=========================================================================
#
# MediaWiki Markup Grammar
 
# Template = "{{" [ "msg:" | "msgnw:" ] PageName { "|" [ ParameterName "=" AnyText | AnyText ] } "}}" ;
# Extension = "<" ? extension ? ">" AnyText "</" ? extension ? ">" ;
# NoWiki = "<nowiki />" | "<nowiki>" ( InlineText | BlockText ) "</nowiki>" ;
# Parameter = "{{{" ParameterName { Parameter } [ "|" { AnyText | Parameter } ] "}}}" ;
# Comment = "<!--" InlineText "-->" | "<!--" BlockText "//-->" ;
#
# ParameterName = ? uppercase, lowercase, numbers, no spaces, some special chars ? ;
#
#=========================================================================== 

# Program version
version = '2.5'
dbversion = "0.0.0.1"

##### Main function ###########################################################

inputsize = 0

class OutputSqlite:
    def __init__(self, sqlite_file):
        global dbversion
        self.sqlite_file=sqlite_file
        self.conn = sqlite3.connect(sqlite_file)
        self.conn.isolation_level="EXCLUSIVE"
        self.curs = self.conn.cursor()
        self.curs.execute("PRAGMA synchronous=NORMAL")
        self.curs.execute('''CREATE TABLE IF NOT EXISTS articles (_id INTEGER PRIMARY KEY AUTOINCREMENT, 
                                                                  article_id INTEGER UNIQUE NOT NULL,
                                                                  title VARCHAR(255) NOT NULL,
                                                                  text BLOB)''')
        self.curs.execute('''CREATE TABLE IF NOT EXISTS redirects (_id INTEGER PRIMARY KEY AUTOINCREMENT, 
                                                                  title_from VARCHAR(255) NOT NULL,
                                                                  title_to VARCHAR(255))''')
        self.curs.execute('''CREATE TABLE IF NOT EXISTS metadata (key VARCHAR(255), value VARCHAR(255));''')
        self.set_gen_date(strftime("%Y-%m-%d %H:%M:%S"))
        self.set_type("wikipedia")
        self.set_version(version)
        self.conn.commit()
        self.curr_values=[]
        self.max_inserts=100

    def insert_redirect(self,from_,to_):
        self.curs.execute("INSERT INTO redirects VALUES (NULL,?,?)",(from_,to_))

    def set_lang(self,lang):
        self.curs.execute("INSERT OR REPLACE INTO metadata VALUES ('lang',?)",(lang,))

    def set_gen_date(self,sdate):
        self.curs.execute("INSERT OR REPLACE INTO metadata VALUES ('date',?)",(sdate,))

    def set_version(self,version):
        self.curs.execute("INSERT OR REPLACE INTO metadata VALUES ('version',?)",(version,))

    def set_type(self,stype):
        self.curs.execute("INSERT OR REPLACE INTO metadata VALUES ('type',?)",(stype,))

    def reserve(self,size):
        pass

    def write(self,article_id,title,text):
        if (len(self.curr_values)==self.max_inserts):
            self.curs.executemany("INSERT INTO articles VALUES (NULL,?,?,?)",self.curr_values)
            self.conn.commit()
            self.curr_values=[]
        else:
            c=pylzma.compressfile(StringIO(text),dictionary=23)
            result=c.read(5)
            result+=struct.pack('<Q', len(text))
            self.curr_values.append((article_id,title,buffer(result+c.read())))
            
    def close(self):
        if (len(self.curr_values)>0):
            self.curs.executemany("INSERT INTO articles VALUES (NULL,?,?,?)",self.curr_values)
        self.conn.commit()
        print("Building indexes")
        self.curs.execute("CREATE INDEX tidx1 ON articles(title)")
        self.curs.execute("CREATE INDEX tidx2 ON redirects(title_from)")
        self.curs.execute("CREATE VIRTUAL TABLE searchTitles USING fts3(_id, title);")
        print("Building FTS table")
        self.curs.execute("INSERT INTO searchTitles(_id,title) SELECT _id,title FROM articles;")
        self.conn.commit()
        print("Cleaning up")
        self.curs.execute("VACUUM")
        self.curs.close()
        self.conn.close()
        sys.exit(0)


### READER ###################################################################

tagRE = re.compile(r'(.*?)<(/?\w+)[^>]*>(?:([^<]*)(<.*?>)?)?')
redirRE = re.compile(r'(?:.*?)<redirect title="(.+)"\s*/>')

eta_every = 100

def process_data(input, output):
    global prefix
    st = datetime.datetime.now() 
    i=0
    page = []
    id = None
    inText = False
    redirect = False
    redir_title = ""
    article_id=None
    past_rev = False
    for line in input:
   #     print(input.tell())
        line = line.decode('utf-8')
        tag = ''
        if '<' in line:
            m = tagRE.search(line)
            if m:
                tag = m.group(2)
        if tag == 'page':
            page = []
            redirect = False
        elif tag == "regision":
            past_rev = True
        elif tag == 'id' and not id:
            id = m.group(3)
            if not past_rev:
                article_id = id 
        elif tag == 'title':
            title = m.group(3)
        elif tag == 'redirect':
            redirect = True
            res=redirRE.match(line)
            if (res):
                redir_title=res.group(1)
        elif tag == 'text':
            inText = True
            line = line[m.start(3):m.end(3)] + '\n'
            page.append(line)
            if m.lastindex == 4: # open-close
                inText = False
        elif tag == '/text':
            if m.group(1):
                page.append(m.group(1) + '\n')
            inText = False
        elif inText:
            page.append(line)
        elif tag == '/page':
            colon = title.find(':')
            past_rev=False
            if (colon < 0 or title[:colon] in wikiglobals.acceptedNamespaces): 
                if redirect:
                    output.insert_redirect(title,redir_title)
                else:
                #    print(id, title.encode('utf-8'))
                    sys.stdout.flush()
                    wikitools.WikiDocumentSQL(output, article_id, title, ''.join(page))
                    i+=1
                    if i%eta_every == 0:
                        percent =  (100.0 * input.tell()) / inputsize
                        delta=((100-percent)*(datetime.datetime.now()-st).total_seconds())/percent
                        status_s= "%.02f%% ETA=%s\r"%(percent, str(datetime.timedelta(seconds=delta)))
                        sys.stdout.write(status_s)
            id = None
            page = []
        elif tag == 'base':
            # discover prefix from the xml dump file
            # /mediawiki/siteinfo/base
            base = m.group(3)
            prefix = base[:base.rfind("/")]
            if wikiglobals.lang =="":
                wikiglobals.lang=base.split(".wikipedia.org")[0].split("/")[-1]
                if wikiglobals.lang!="":
                    print("Autodetected language : %s."%wikiglobals.lang)
                    print("Will apply corresponding conversion rules from lib/wiki%s.py, if this files exists"%wikiglobals.lang)
                    output.set_lang(wikiglobals.lang)

### CL INTERFACE ############################################################

def show_help():
    print >> sys.stdout, __doc__,

def show_usage(script_name):
    print >> sys.stderr, 'Usage: %s [options]' % script_name

def main():
    global prefix,inputsize
    script_name = os.path.basename(sys.argv[0])

    try:
        long_opts = ['help', 'basename=', 'ns=', "db=", "xml="]
        opts, args = getopt.gnu_getopt(sys.argv[1:], 'hn:B:L:x:d:', long_opts)
    except getopt.GetoptError:
        show_usage(script_name)
        sys.exit(1)

    compress = False
    output_file="./wiki.sqlite"

    for opt, arg in opts:
        if opt in ('-h', '--help'):
            show_help()
            sys.exit()
        elif opt in ('-L'):
            wikiglobals.lang = arg
        elif opt in ('-s', '--sections'):
            wikiglobals.keepSections = True
        elif opt in ('-B', '--base'):
            prefix = arg
        elif opt in ('-n', '--ns'):
            wikiglobals.acceptedNamespaces = set(arg.split(','))
        elif opt in ('-d', '--db'):
            output_file = arg
        elif opt in ('-x','--xml'):
            input_file = arg

    if not 'input_file' in locals():
        print("Please give me a wiki xml dump with -x or --xml")
        sys.exit()

    if not wikiglobals.keepLinks:
        wikiglobals.ignoreTag('a')

    inputsize = os.path.getsize(input_file)

    if os.path.isfile(output_file):
        print("%s already exists. Won't overwrite it."%output_file)
        sys.exit(1)

    input = open(input_file,"r")
    print("Converting xml dump %s to database %s. This may take eons..."%(input_file,output_file))
    worker = OutputSqlite(output_file)

    process_data(input, worker)
    worker.close()

if __name__ == '__main__':
    main()
