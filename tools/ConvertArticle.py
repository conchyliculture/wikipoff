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

class OutputText:
    def __init__(self):
        pass

    def reserve(self,size):
        pass

    def write(self,title,text):
        print(title+" : ")
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
    global prefix

    page = []
    id_ = None
    inText = False
    redirect = False
    redir_title = ""
    title=""
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
        elif tag == 'id' and id_==None:
            id_ = m.group(3)
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
            sys.stdout.flush()
            wikitools.WikiDocumentSQL(output, title, ''.join(page))
            id_ = None
            page = []
            return
        elif tag == 'base':
            # discover prefix from the xml dump file
            # /mediawiki/siteinfo/base
            base = m.group(3)
            prefix = base[:base.rfind("/")]
            if wikiglobals.lang =="":
                wikiglobals.lang=base.split(".wikipedia.org")[0].split("/")[-1]
            
class OutputHelper:
    def __init__(self,sqlite_file):
        self.sqlite_file=sqlite_file
        self.conn = sqlite3.connect(sqlite_file)
        self.conn.isolation_level="EXCLUSIVE"
        self.curs = self.conn.cursor()
        self.curs.execute("PRAGMA synchronous=NORMAL")
        self.curs.execute('''CREATE TABLE IF NOT EXISTS indexes ( id INTEGER PRIMARY KEY, title VARCHAR(255), position INTEGER);''')
        self.conn.commit()
        self.curr_values=[]
        self.max_inserts=100

    def check(self):
        res=self.curs.execute("SELECT COUNT(*) FROM indexes").fetchone()[0]
        if res==0:
            return False
        return True
        

    def insert(self,id_,title,pos):
        if (len(self.curr_values)==self.max_inserts):
            self.curs.executemany("INSERT INTO indexes VALUES (?,?,?)",self.curr_values)
            self.conn.commit()
            self.curr_values=[]
        else:
            self.curr_values.append((id_,title.decode('utf-8'),pos))


    def close(self):
        if (len(self.curr_values)>0):
            self.curs.executemany("INSERT INTO articles VALUES (?,?,?)",self.curr_values)
        self.conn.commit()
        print "Building indexes"
        self.curs.execute("CREATE INDEX tidx on indexes(title);")
        self.curs.close()
        self.conn.commit()
        self.conn.close()
        sys.exit(0)

def check_helper_db(output_sql):
    output=OutputHelper(output_sql)
    return output.check()


def create_helper_db(input_xml,output_sql):
    output=OutputHelper(output_sql)
    filesize=os.stat(input_xml).st_size
    inputstream=open(input_xml,"r")
    id_=0
    title=""
    start=0
    revision=False
    endpageRE=re.compile(r'^\s*<\/page>\s*$')
    idRE=re.compile(r'^\s*<id>(\d+)<\/id>\s*$')
    startpageRE=re.compile(r'^\s*<page>\s*$')
    titleRE=re.compile(r'^\s*<title>(.+)<\/title>\s*')
    revisionRE=re.compile(r'\s*<revision>$')
    curpos=0
    for line in iter(inputstream.readline, ''):
        lol_python_is_shit=endpageRE.match(line)
        if lol_python_is_shit!=None:
            #print("%d %s %d"%(id_,title,start))
            output.insert(id_,title,start)
            title=""
            id_=0
            start=0
            revision=False
            next

        seriously_what_am_i_doing=idRE.match(line)
        if seriously_what_am_i_doing!=None:
            if not revision:
                id_=int(seriously_what_am_i_doing.group(1))
            next

        get_me_out_of_here=startpageRE.match(line)
        if get_me_out_of_here!=None:
            start=curpos
            id_=0
            next

        this_can_t_be_happening=titleRE.match(line)
        if this_can_t_be_happening!=None:
            title=this_can_t_be_happening.group(1)
            next

        when_will_this_craziness_end=revisionRE.match(line)
        if when_will_this_craziness_end!=None:
            revision=True
            next

        curpos=inputstream.tell()
        percentdone=100*curpos/filesize
        if curpos%500==0:
            sys.stdout.write(u"\r%f %%"%(100.0*curpos/filesize))







### CL INTERFACE ############################################################

def show_usage(s):
    print("""%s : Selectively converts articles from XML file to stdout. It needs a "helper database"
"""%(s))

def main():
    global prefix
    script_name = os.path.basename(sys.argv[0])

    titles=[]
    try:
        long_opts = ["db=","--dumpfile=","--title=","--orig","--lang"]
        opts, args = getopt.gnu_getopt(sys.argv[1:], 'd:f:t:rl:H:', long_opts)
    except getopt.GetoptError, e:
        print("ERROR : ",e)
        show_usage
        sys.exit(1)

    for opt, arg in opts:
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
    if not 'sqlite_file' in locals():
        print("Please give me a SQLite database file with indexes with -d")
        sys.exit()
    if not 'xmlfile' in locals():
        print("Please give me a XML wiki dump file with -f")
        sys.exit()
    if wikiglobals.lang=="":
        print("If you don't specify a language with -l, you're gonna have a bad time")
        sys.exit()

    if not check_helper_db(sqlite_file):
        print "%s is not a correct helper db."%sqlite_file
        create_helper_db(xmlfile,sqlite_file)


    if titles==[]:
        print "Need at least one article id or one title"
        sys.exit(1)

#    if len(args) > 5:
#        show_usage(script_name)
#        sys.exit(4)

    if not wikiglobals.keepLinks:
        wikitools.ignoreTag('a')

    conn = sqlite3.connect(sqlite_file)

    for title in titles:
        io=open(xmlfile)
        pos = get_pos_from_title(conn,title)
        if pos != 0:
            print pos
            io.seek(pos)
            worker = OutputText()
            process_data(io, worker)
            worker.close()
        else:
            print "Can't find article with title %s in helper database : %s "%(title,sqlite_file)
            print "Maybe you need to re-create it? rm will help"

if __name__ == '__main__':
    main()
