#!/usr/bin/python
# -*- coding: utf-8 -*-
# 
# Copyright 2014 "Renzokuken" (pseudonym, first committer of WikipOff project) at
# https://github.com/conchyliculture/wikipoff
# 
# This file is part of WikipOff.
# 
#     WikipOff is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
# 
#     WikipOff is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
# 
#     You should have received a copy of the GNU General Public License
#     along with WikipOff.  If not, see <http://www.gnu.org/licenses/>.


import re
import sys
import locale
locale.setlocale(locale.LC_ALL, 'fr_FR.utf-8')

class SaveFRTemplates:
    def __init__(self):
        # Templates that allow inclusion of }} in parameters will fail....
        # We should use the dropNested thing  maybe?
        # TODO
        self.fr_saveDateTemplatesRE=re.compile(r'{{date\|(\d+)\|(\w+)\|(\d+)}}', re.IGNORECASE)
        self.fr_saveLangTemplatesRE=re.compile(r'{{lang\|(\w+)\|([^}]+)}}', re.IGNORECASE)
        self.fr_saveUnitsTemplatesRE=re.compile(r'{{unit.\|(\d+)\|([^}\|]+)(?:\|(\d+))?}}', re.IGNORECASE)
        self.fr_saveRefIncTemplatesRE=re.compile(ur'{{Référence [^\|]+\|([^\|]+)}}',re.IGNORECASE) # incomplete/insuff/a confirmer/nécessaire
        self.fr_saveNumeroTemplatesRE=re.compile(ur'{{(numéro|n°|nº)}}',re.IGNORECASE)
        self.fr_saveCitationTemplatesRE=re.compile(ur'{{citation ?(?:bloc|nécessaire)?\|([^}]+)}}',re.IGNORECASE)
        self.fr_savePassageEvasifTemplatesRE=re.compile(ur'{{passage évasif\|([^}]+)}}',re.IGNORECASE)
# not in my wikipedia
#self.fr_savePassNonNeutreTemplatesRE=re.compile(ur'{{(?:passage non neutre|non neutre|nonneutre)\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_saveDouteuxTemplatesRE=re.compile(r'{{douteux\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_savePasspromotionnelTemplatesRE=re.compile(r'{{(?:passage promotionnel|pub !|promo !)\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_savePassIneditTemplatesRE=re.compile(ur'{{passage inédit\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_savePasClairTemplatesRE=re.compile(r'{{pas clair\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_saveWTFTemplatesRE=re.compile(ur'{{incomprénsible\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_savePrecNecTemplatesRE=re.compile(ur'{{Précision nécessaire\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_saveQuoiTemplatesRE=re.compile(r'{{Quoi\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_savePourquoiTemplatesRE=re.compile(r'{{Pourquoi\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_saveCommentTemplatesRE=re.compile(r'{{Comment\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_saveWhereTemplatesRE=re.compile(r'{{Où\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_saveQuandTemplatesRE=re.compile(r'{{Quand\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_saveDepuisQuandTemplatesRE=re.compile(r'{{Depuis Quand\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_saveQuiQuoiTemplatesRE=re.compile(r'{{(?:Qui|Lequel|combien|enquoi|en quoi|lesquels|laquelle|lesquelles|par qui|parqui|pour qui)\|([^\|]+)}}', re.IGNORECASE)
        self.fr_savestyleTemplatesRE=re.compile(r'{{style\|([^\|]+)(?:\|[^}]+)?}}', re.IGNORECASE)
        self.fr_saveFormatnumTemplatesRE=re.compile(r'{{formatnum:([0-9.]+)}}', re.IGNORECASE)
        self.fr_saveWeirdNumbersTemplatesRE=re.compile(r'{{((?:(exp|ind)\|[^}]+)|\d|e|1er|1re|2nd|2nde)}}', re.IGNORECASE)
        self.fr_saveCouleursTemplatesRE=re.compile(r'{{(rouge|bleu|vert|jaune|orange|gris|marron|rose)\|([^\|}]+)}}', re.IGNORECASE)
        self.fr_saveCodeTemplatesRE=re.compile(r'{{code\|([^\|}]+)}}', re.IGNORECASE)

        self.fr_saveSieclesTempaltesRE=re.compile(ur'{{(?:([^|}]+(?:siècle|millénaire)[^|}]*)|(-?s2?-?(?:\|[^|}]+\|e)+))}}', re.IGNORECASE)

        self.aRE=re.compile(r'<math>')
        self.bRE=re.compile(r'</math>')

    def save(self,t):
        text=self.fr_saveFormatnumTemplates(t)
        text=self.fr_saveWeirdNumbersTemplates(text)
        text=self.fr_saveNumeroTemplate(text)
        text=self.fr_saveSieclesTempaltes(text)
        text=self.fr_saveLangTemplates(text)
        text=self.fr_saveUnitsTemplates(text)
        text=self.fr_saveCouleursTemplates(text)
        text=self.fr_saveRefIncTemplates(text)
        text=self.fr_saveQuiQuoiTemplates(text)
        text=self.fr_saveCodeTemplates(text)
        text=self.fr_saveCitationTemplate(text)
#        text=self.fr_savePasspromotionnelTemplates(text)
#        text=self.fr_savePassIneditTemplates(text)
#        text=self.fr_savePasClairTemplates(text)
#        text=self.fr_saveWTFTemplates(text)
#        text=self.fr_savePrecNecTemplates(text)
#        text=self.fr_savestyleTemplates(text)
        return text

    def fr_saveDouteuxTemplates(self,text):
        return self.fr_saveDouteuxTemplatesRE.sub(r'\1<sup>[douteux]</sup>',text)
    def fr_savePasspromotionnelTemplates(self,text):
        return self.fr_savePasspromotionnelTemplatesRE.sub(r'\1<sup>[passage promotionnel]</sup>',text)
    def fr_savePassIneditTemplates(self,text):
        return self.fr_savePassIneditTemplatesRE.sub(r'\1<sup>[interprétion personnelle]</sup>',text)
    def fr_savePasClairTemplates(self,text):
        return self.fr_savePasClairTemplatesRE.sub(r'\1<sup>[pas clair]</sup>',text)
    def fr_saveWTFTemplates(self,text):
        return self.fr_saveWTFTemplatesRE.sub(r'\1<sup>[incomprénsible]</sup>',text)
    def fr_savePrecNecTemplates(self,text):
        return self.fr_savePrecNecTemplatesRE.sub(r'\1<sup>[précision néssaire]</sup>',text)
    def fr_saveQuiQuoiTemplates(self,text):
        return self.fr_saveQuiQuoiTemplatesRE.sub(r'\1',text)
    def fr_savestyleTemplates(self,text):
        return self.fr_savestyleTemplatesRE.sub(r'\1',text)

    
    def replsiecles(self,m):
        nb=m.group(2)
        if nb:
            siecle=""
            avjc=""
            a,b,c=nb.split("|",2)
            if a.startswith("-"):
                avjc=" av. J.-C. "
            a=a[1:]
            if a.startswith("s2"):
                _,d,e = c.split("|")
                return b+"e et "+d+u"e siècles"+avjc
            else:
                return b+u"e siècle"+avjc
        else:
            nb=m.group(1)
            return "<a href=\"%s\">%s</a>"%(nb,nb)

    def fr_saveSieclesTempaltes(self,text):
        return re.sub(self.fr_saveSieclesTempaltesRE,self.replsiecles,text)

    def fr_saveCitationTemplate(self,text):
        return self.fr_saveCitationTemplatesRE.sub(ur'«&#160;<i>\1</i>&#160;»',text)

    def fr_savePassageEvasifTemplates(self,text):
        return self.fr_savePassageEvasifTemplatesRE.sub(r'<u>\1</u><sup>[evasif??]</sup>',text)

    def fr_saveNumeroTemplate(self,text):
        return self.fr_saveNumeroTemplatesRE.sub(u"n°",text)

    def fr_saveRefIncTemplates(self,text):
        return self.fr_saveRefIncTemplatesRE.sub(ur'<u>\1</u><sup>[ref incomplète??]</sup>',text)

    def fr_saveDateTemplates(self,text):
        return self.fr_saveDateTemplatesRE.sub(r'\1 \2 \3',text) 

    def fr_saveLangTemplates(self,text):
        return self.fr_saveLangTemplatesRE.sub(r'\2',text) 

    def replcolors(self,m):
        col=m.group(1)
        t=m.group(2)
        if col.lower=="rouge":
            return "<font color=\"red\">%s</font>"%t
        elif col.lower=="bleu":
            return "<font color=\"blue\">%s</font>"%t
        elif col.lower=="vert":
            return "<font color=\"green\">%s</font>"%t
        elif col.lower=="jaune":
            return "<font color=\"yellow\">%s</font>"%t
        elif col.lower=="orange":
            return "<font color=\"orange\">%s</font>"%t
        elif col.lower=="gris":
            return "<font color=\"grey\">%s</font>"%t
        elif col.lower=="marron":
            return "<font color=\"brown\">%s</font>"%t
        elif col.lower=="rose":
            return "<font color=\"pink\">%s</font>"%t
        else:
            return t

    def fr_saveCodeTemplates(self,text):
        return self.fr_saveCodeTemplatesRE.sub(r'<span style=\"font-family:monospace,Courier\">\1</span>',text)


    def fr_saveCouleursTemplates(self,text):
        return re.sub(self.fr_saveCouleursTemplatesRE,self.replcolors,text)

    def replweirdnum(self,m):
        nb=m.group(1)
        if nb.find("|")>=0:
            lol,nb=nb.split('|',1)
            if lol=="exp":
                return "<sup>%s</sup>"%nb
            elif lol=="ind":
                return "<sub>%s</sub>"%nb
        else:
            if nb=="e":
                return "<sup>e</sup>"
            elif nb=="1er":
                return "1<sup>er</sup>"
            elif nb=="1re":
                return "1<sup>re</sup>"
            elif nb=="2nd":
                return "2<sup>nd</sup>"
            elif nb=="2nde":
                return "2<sup>de</sup>"
            else:
                return "<sup>%s</sup>"%nb

    def fr_saveWeirdNumbersTemplates(self,text):
        return re.sub(self.fr_saveWeirdNumbersTemplatesRE,self.replweirdnum,text)
    
    def replformat(self,m):
        nb=m.group(1)
        try:
            if nb:
                if nb.find(".")>0 or nb.find(",")>0:
                    return locale.format("%f",float(nb),grouping=True)
                else:
                    return locale.format("%d",int(nb),grouping=True)
        except ValueError:
            return nb

    def fr_saveFormatnumTemplates(self,text):
        return re.sub(self.fr_saveFormatnumTemplatesRE,self.replformat,text)

    def replunit(self,m):
        sup = m.group(3)
        if not sup:
            return "%s %s"%(m.group(1),m.group(2))
        else:
            return "%s %s<sup>%s</sup>"%(m.group(1),m.group(2),m.group(3))

    def fr_saveUnitsTemplates(self,text):
        return re.sub(self.fr_saveUnitsTemplatesRE,self.replunit,text)
