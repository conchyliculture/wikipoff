#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys
sys.path.append("./lib")
sys.path.append("./lib/python2.7/site-packages/pylzma-0.4.4-py2.7-linux-x86_64.egg/")
import re
import os
import getopt
import wikitools
import wikiglobals
import sqlite3

article_ids = []

class OutputText:
    def __init__(self):
        pass

    def reserve(self,size):
        pass

    def write(self,artid,title,text):
        print(title+"/"+artid+" : ")
        print(text.decode("utf-8"))
            
    def close(self):
        pass

def get_pos_from_id(db,id_):
    c = db.cursor()
    c.execute("SELECT position FROM indexes WHERE id=?",(id_,))
    row = c.fetchone()
    if row:
        return row[0]
    else:
        return 0
def get_pos_from_title(db,title):
    c = db.cursor()
    c.execute("SELECT position FROM indexes WHERE title=?",(title.decode('utf-8'),))
    row = c.fetchone()
    if row:
        return row[0]
    else:
        return 0



### READER ###################################################################

tagRE = re.compile(r'(.*?)<(/?\w+)[^>]*>(?:([^<]*)(<.*?>)?)?')
redirRE = re.compile(r'(?:.*?)<redirect title="(.+)"\s*/>')

def process_data(input, output):
    global prefix,article_ids

    page = []
    id = None
    inText = False
    redirect = False
    redir_title = ""
    for line in input:
        line = line.decode('utf-8')
        tag = ''
        if '<' in line:
            m = tagRE.search(line)
            if m:
                tag = m.group(2)
        if tag == 'page':
            page = []
            redirect = False
        elif tag == 'id' and not id:
            id = m.group(3)
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
            if (colon < 0 or title[:colon] in wikiglobals.acceptedNamespaces): 
                sys.stdout.flush()
                wikitools.WikiDocumentSQL(output, id, title, ''.join(page))
            id = None
            page = []
            return
        elif tag == 'base':
            # discover prefix from the xml dump file
            # /mediawiki/siteinfo/base
            base = m.group(3)
            prefix = base[:base.rfind("/")]
            if wikiglobals.lang =="":
                wikiglobals.lang=base.split(".wikipedia.org")[0].split("/")[-1]

### CL INTERFACE ############################################################


def main():
    global prefix, article_ids
    script_name = os.path.basename(sys.argv[0])

    titles=[]
    try:
        long_opts = ['id=',"db=","--dumpfile=","--title=","--orig","--lang"]
        opts, args = getopt.gnu_getopt(sys.argv[1:], 'i:d:f:t:rl:', long_opts)
    except getopt.GetoptError:
        show_usage(script_name)
        sys.exit(1)

    for opt, arg in opts:
        if opt in ('-i', '--id'):
            article_ids= map((lambda x: int(x)), arg.split(","))
        if opt in ('-d', '--db'):
            sqlite_file= arg
        if opt in ('-f','--dumpfile'):
            xmlfile=arg
        if opt in ('-t','--title'):
            titles=arg.split(",")
        if opt in ('-r',"--orig"):
            wikiglobals.convert=False
        if opt in ('-l',"--lang"):
            wikiglobals.lang=arg

    if article_ids==[] and titles==[]:
        print "Need at least one article id or one title"
        sys.exit(1)

    if len(args) > 0:
        show_usage(script_name)
        sys.exit(4)

    if not wikiglobals.keepLinks:
        wikitools.ignoreTag('a')

    conn = sqlite3.connect(sqlite_file)
    for id__ in article_ids:

        id_=int(id__)
        io=open(xmlfile)
        pos = get_pos_from_id(conn,id_)
        if pos != 0:
            io.seek(pos)
            worker = OutputText()
            process_data(io, worker)
            worker.close()
        else:
            print "Can't find article with id %d"%id_

    for title in titles:
        io=open(xmlfile)
        pos = get_pos_from_title(conn,title)
        if pos != 0:
            io.seek(pos)
            worker = OutputText()
            process_data(io, worker)
            worker.close()
        else:
            print "Can't find article with id %d"%id_
    


if __name__ == '__main__':
    main()
