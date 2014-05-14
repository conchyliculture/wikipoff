#!/usr/bin/ruby

require "pp"
require "net/http"
require "uri"
require "dbi"

$dbh=DBI.connect("DBI:SQLite3:languages.sqlite")

$dbh.do("CREATE TABLE IF NOT EXISTS languages  ( _id INTEGER PRIMARY KEY AUTOINCREMENT,
                                               code VARCHAR(255),
                                               sens VARCHAR(255),
                                                english VARCHAR(255),
                                                local VARCHAR(255)
                                                )
                            ")

url="http://meta.wikimedia.org/wiki/Template:List_of_language_names_ordered_by_code"

array=[]
curlang={}

status=""
res=Net::HTTP.get(URI.parse(url))
res.force_encoding("utf-8").each_line do  |l|
    case l
    when /<th>Comment<\/th>/
        status="start"
    when /<tr>/
        curlang={}
        status="start"
    when /<\/tr>/
        array << curlang
    when /<td[^<>]*>(.*)<\/td>/
        val=$1
        case status
        when "start"
            curlang["code"]=val.strip()
            status="english"
        when "english"
            curlang["english"]=val.strip
            status="sens"
        when "sens"
            curlang["sens"]=val.strip
            status="local"
        when "local"
            curlang["local"]=val.strip
            status="article"
        when "article"
            status="comment"
        when "comment"
            status="start"
        end
    end
end
array.each do |lol|
    unless lol == {}
        $dbh.do("INSERT INTO languages VALUES (NULL,?,?,?,?)",lol["code"],lol["sens"],lol["english"],lol["local"])
    end
end
