#!/bin/bash
# Need python-dev
# CHANGE ME
#WIKIDUMPURL="http://dumps.wikimedia.org/frwiki/latest/frwiki-latest-pages-articles.xml.bz2"
#WIKIBZ="frwiki-latest-pages-articles.xml.bz2"
#WIKILANG="fr"
WIKIDUMPURL="http://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2"
WIKIBZ="enwiki-latest-pages-articles.xml.bz2"
WIKILANG="en"

######################

LIBDIRR="`pwd`/lib/"
PYLZMA="pylzma-0.4.4"
EGGDIR="$LIBDIRR/python2.7/site-packages/pylzma-0.4.4-py2.7-linux-x86_64.egg"
#PYLZMAPATH=`pwd`/$PYLZMADIR
PYLZMAURL="https://pypi.python.org/packages/source/p/pylzma/pylzma-0.4.4.tar.gz#md5=a2be89cb2288174ebb18bec68fa559fb"
PYLZMAARCH="pylzma-0.4.4.tar.gz"

export PYTHONPATH=$PYTHONPATH:$EGGDIR/../

PYTHON="python"
BUNZIP=`which bunzip2`

if [ "x$BUNZIP" == "x" ] ; then
    echo "Please install bunzip2"
    exit 1
fi

if [ ! -f "$WIKIBZ" ]; then
    wget $WIKIDUMPURL
fi

if [ ! -d "$LIBDIRR" ]; then
    mkdir -p "$EGGDIR"
    wget "$PYLZMAURL"
    tar xzf "$PYLZMAARCH"
    cd "$PYLZMA"
    wget "http://pypi.python.org/packages/2.7/s/setuptools/setuptools-0.6c11-py2.7.egg"
    $PYTHON setup.py install --prefix="`pwd`/../"
    cd -
    rm -rf ./bin 
    echo "done"
    echo "you can : rm -rf $PYLZMA*"
fi

echo "Unbziping $WIKIBZ"
bunzip2 "$WIKIBZ"

$PYTHON WikiExtractor.py -x "${WIKIBZ/.bz2}" -d $WIKILANG.wiki.sqlite
