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
import unittest
import traceback
locale.setlocale(locale.LC_ALL, 'fr_FR.utf-8')

def is_allowed_title(title):
    if title in [u"Modèle",u"Catégorie",u"Portail",u"Fichier",u"Wikipédia",u"Projet",u"Référence",u"MediaWiki",u"Aide",u"Module"]:
        return False
    return True

class SaveFRTemplates:
    def __init__(self):
        # Templates that allow inclusion of }} in parameters will fail....
        # We should use the dropNested thing  maybe?
        # TODO
        self.fr_saveDateTemplatesRE=re.compile(ur'{{date(?: de naissance| de décès)?\|(|\d+(?:er)?)\|([^|}]+)\|?(\d*)(?:\|[^}]+)?}}', re.IGNORECASE|re.UNICODE)
        self.fr_saveDateShortTemplatesRE=re.compile(r'{{1er (janvier|f.vrier|mars|avril|mai|juin|juillet|ao.t|septembre|octobre|novembre|d.cembre)}}', re.IGNORECASE|re.UNICODE)
        self.fr_saveLangTemplatesRE=re.compile(r'{{(lang(?:ue)?(?:-\w+)?(?:\|[^}\|]+)+)}}', re.IGNORECASE|re.UNICODE)
        self.fr_saveUnitsTemplatesRE=re.compile(ur'{{unit.\|([^|{}]+(?:\|[^{}[|]*)*)}}', re.IGNORECASE|re.UNICODE)
        self.fr_saveTemperatureTemplatesRE=re.compile(ur'{{tmp\|([^\|]+)\|°C}}', re.IGNORECASE|re.UNICODE)
        self.fr_saveRefIncTemplatesRE=re.compile(ur'{{Référence [^|}]+\|([^|]+)}}',re.IGNORECASE) # incomplete/insuff/a confirmer/nécessaire
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
        self.fr_saveFormatnumTemplatesRE=re.compile(r'{{(?:formatnum|nombre|num|nau):([0-9.,]+)}}', re.IGNORECASE)
        self.fr_saveWeirdNumbersTemplatesRE=re.compile(r'{{((?:(exp|ind)\|[^}]+)|\d|e|1er|1re|2nd|2nde)}}', re.IGNORECASE)
        self.fr_saveCouleursTemplatesRE=re.compile(r'{{(rouge|bleu|vert|jaune|orange|gris|marron|rose)\|([^\|}]+)}}', re.IGNORECASE)
        self.fr_saveCodeTemplatesRE=re.compile(r'{{code\|([^\|}]+)}}', re.IGNORECASE)
        self.fr_saveJaponaisTemplatesRE=re.compile(r'{{japonais\|([^\|]+)\|([^}]+)}}', re.IGNORECASE)
        self.fr_saveSimpleSieclesTempaltesRE=re.compile(ur'{{([^}]+ (?:si.cle|mill.naire)[^}]*)}}', re.IGNORECASE|re.LOCALE)
        self.fr_saveSieclesTempaltesRE=re.compile(ur'{{(?:([^|}]+(?:si.cle|mill.naire)[^|}]*)|(-?s2?-?(?:\|[^|}]+\|e)+))\|?}}', re.IGNORECASE)

        self.aRE=re.compile(r'<math>')
        self.bRE=re.compile(r'</math>')

    def save(self,t):
        text=t
        text=self.fr_saveTemperatureTemplates(text)
        text=self.fr_saveFormatnumTemplates(text)
        text=self.fr_saveUnitsTemplates(text)
        text=self.fr_saveWeirdNumbersTemplates(text)
        text=self.fr_saveNumeroTemplate(text)
        text=self.fr_saveSieclesTemplates(text)
        text=self.fr_saveLangTemplates(text)
        text=self.fr_saveCouleursTemplates(text)
        text=self.fr_saveRefIncTemplates(text)
        text=self.fr_saveDateTemplates(text)
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
            try:
                siecle=""
                avjc=""
                a,b,c=nb.split("|",2)
                if a.startswith("-"):
                    avjc=" av. J.-C. "
                if a.startswith("s2"):
                    _,d,e = c.split("|")
                    return b+"e et "+d+u"e siècles"+avjc
                elif a.startswith("-s2"):
                    _,d,e = c.split("|")
                    return b+"e et "+d+u"e siècles"+avjc

                else:
                    return b+u"e siècle"+avjc
            except ValueError as e:
                # Failing humbly...
                return nb
        else:
            nb=m.group(1)
            return "<a href=\"%s\">%s</a>"%(nb,nb)

    def fr_saveSieclesTemplates(self,text):
        return re.sub(self.fr_saveSieclesTempaltesRE,self.replsiecles,text)

    def fr_saveSimpleSieclesTemplates(self,text):
        return self.fr_saveSimpleSieclesTempaltesRE.sub(ur'\1',text,re.LOCALE)

    def fr_saveCitationTemplate(self,text):
        return self.fr_saveCitationTemplatesRE.sub(ur'«&#160;<i>\1</i>&#160;»',text)

    def fr_savePassageEvasifTemplates(self,text):
        return self.fr_savePassageEvasifTemplatesRE.sub(r'<u>\1</u><sup>[evasif??]</sup>',text)

    def fr_saveNumeroTemplate(self,text):
        return self.fr_saveNumeroTemplatesRE.sub(u"n°",text)

    def fr_saveRefIncTemplates(self,text):
        return self.fr_saveRefIncTemplatesRE.sub(ur'<u>\1</u><sup>[ref incomplète??]</sup>',text)

    def repldate(self,m):
        res=""
        if m.group(1)=="":
            res= "%s %s"%(m.group(2),m.group(3))
        elif m.group(1)=="1":
            res= "1er %s %s"%(m.group(2).lstrip(),m.group(3))
        else:
            res= "%s %s %s"%(m.group(1),m.group(2).lstrip(),m.group(3))
        return res.strip()

    def fr_saveDateTemplates(self,text):
        return re.sub(self.fr_saveDateTemplatesRE,self.repldate,text)

    def fr_saveDateShortTemplates(self,text):
        return self.fr_saveDateShortTemplatesRE.sub(r'1<sup>er</sup> \1',text)

    def repllang(self,m):
        lol=m.group(1).split("|")
        if (re.match(r'.*\[\[.+\|.+\]\]',m.group(0))):
            return "|".join(lol[2:])

        if (re.match(r'lang-\w+',lol[0],re.IGNORECASE)):
            return lol[1]
        else:
            reste=lol[2:]
            if len(reste)==1:
                return reste[0].replace("texte=",'')
            else: 
                for p in reste:
                    coin=re.match(r'texte=(.*)',p,re.IGNORECASE)
                    if coin:
                        return coin.group(1)


    def fr_saveLangTemplates(self,text):
        return re.sub(self.fr_saveLangTemplatesRE,self.repllang,text) 

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

    def fr_saveTemperatureTemplates(self,text):
        return self.fr_saveTemperatureTemplatesRE.sub(ur'\1°C',text)

    def repljaponais(self,m):
         return m.group(1)+" ("+m.group(2).replace("|",", ").replace("extra=","")+")"

    def fr_saveJaponaisTemplates(self,text):
        return re.sub(self.fr_saveJaponaisTemplatesRE,self.repljaponais,text)

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
            # Failing humbly...
            return nb

    def fr_saveFormatnumTemplates(self,text):
        return re.sub(self.fr_saveFormatnumTemplatesRE,self.replformat,text)

    def num_to_loc(self,num):
        try:
            virg=num.replace(" ","").replace(",",".").split(r".")
            res=locale.format("%d",int(virg[0]),grouping=True)
            if len(virg)>1:
                res+="."+virg[1]
            return res
        except ValueError as e:
            # Failing humbly...
            return num

    def replunit(self,m):
        l=m.group(1).split("|")
        if len(l)==1:
            a=self.num_to_loc(l[0])
            return "%s"%(a)
        else: 
            # It becomes clear in the test function testUnit below THAT THIS IS A FUCKING MESS
            dot=""
            exp=" "
            tab=[]
            for s in l[1:]:
                index=s.find("=")
                if index>0:
                    s=s[2:]
                    exp=u"×10"
                if (re.match(r'-?[0-9]',s)):
                    tab.append(u"%s<sup>%s</sup>"%(dot,self.num_to_loc(s)))
                elif s=="":
                    tab.append(dot)
                else:
                    dot=s
            res=self.num_to_loc(l[0])+exp
            if len(tab)==0:
                res+=dot
            elif len(tab)==1:
                res+=tab[0]
            else:
                res+= u"⋅".join(tab)
                res=re.sub(ur'×10<sup>([^<]+)</sup>⋅', ur'×10<sup>\1</sup> ',res)
            return res 

    def fr_saveUnitsTemplates(self,text):
        return re.sub(self.fr_saveUnitsTemplatesRE,self.replunit,text)




class WikiFRTests(unittest.TestCase):

    sfrt=SaveFRTemplates()

    def testLang(self):
        tests=[
            ["lolilol ''{{lang|la|domus Dei}}''","lolilol ''domus Dei''"],
            ["''{{lang-en|Irish Republican Army}}, IRA'' ; ''{{lang-ga|Óglaigh na hÉireann}}'') est le nom porté","''Irish Republican Army, IRA'' ; ''Óglaigh na hÉireann'') est le nom porté"],
            ["{{lang|ko|입니다.}}","입니다."],
            ["Ainsi, le {{lang|en|''[[Quicksort]]''}} (ou tri rapide)","Ainsi, le ''[[Quicksort]]'' (ou tri rapide)"],
            [" ''{{lang|hy|Hayastan}}'', {{lang|hy|Հայաստան}} et ''{{lang|hy|Hayastani Hanrapetut’yun}}'', {{lang|hy|Հայաստանի Հանրապետություն}}"," ''Hayastan'', Հայաստան et ''Hayastani Hanrapetut’yun'', Հայաստանի Հանրապետություն"],
            ["{{langue|ja|酸度}} || １.４（{{langue|ja|芳醇}}","酸度 || １.４（芳醇"],
            ["{{langue|thaï|กรุงเทพฯ}}","กรุงเทพฯ"],
            ["{{Lang|ar|texte=''Jabal ad Dukhan''}}","''Jabal ad Dukhan''"],
            ["{{lang|arc-Hebr|dir=rtl|texte=ארמית}} {{lang|arc-Latn|texte=''Arāmît''}},}}","ארמית ''Arāmît'',}}"],
            ["ce qui augmente le risque de {{lang|en|''[[Mémoire virtuelle#Swapping|swapping]]''}})","ce qui augmente le risque de ''[[Mémoire virtuelle#Swapping|swapping]]'')"]
        ]

        for t in tests:
            self.assertEqual(self.sfrt.fr_saveLangTemplates(t[0]), t[1])

    def testDateShort(self):
        tests=[
            ["{{1er janvier}}","1<sup>er</sup> janvier"],
            [u"{{1er février}}",u"1<sup>er</sup> février"],
            ["{{1er mars}}","1<sup>er</sup> mars"],
            ["{{1er avril}}","1<sup>er</sup> avril"],
            ["{{1er mai}}","1<sup>er</sup> mai"],
            ["{{1er juin}}","1<sup>er</sup> juin"],
            ["{{1er juillet}}","1<sup>er</sup> juillet"],
            [u"{{1er août}}",u"1<sup>er</sup> août"],
            ["{{1er septembre}}","1<sup>er</sup> septembre"],
            ["{{1er octobre}}","1<sup>er</sup> octobre"],
            ["{{1er novembre}}","1<sup>er</sup> novembre"],
            [u"{{1er décembre}}",u"1<sup>er</sup> décembre"],
        ]
        for t in tests:
            self.assertEqual(self.sfrt.fr_saveDateShortTemplates(t[0]),t[1])

    def testDate(self):
        tests=[
            ["{{date|10|août|1425}}","10 août 1425"],
            ["{{Date|10|août|1989}} - {{Date|28|février|1990}}","10 août 1989 - 28 février 1990"],
            ["{{date|6|février|1896|en France}}","6 février 1896"],
            ["{{Date|1er|janvier|537}}","1er janvier 537"],
            ["{{Date||Octobre|1845|en sport}}","Octobre 1845"],
            ["{{Date|1|octobre|2005|dans les chemins de fer}}","1er octobre 2005"],
            ["les {{Date|25|mars}} et {{Date|8|avril|1990}}","les 25 mars et 8 avril 1990"],
            [u"'''Jean-François Bergier''', né à [[Lausanne]], le {{date de naissance|5|décembre|1931}} et mort le {{date de décès|29|octobre|2009}}&lt;ref name=&quot;swissinfo&quot;/&gt;, est un [[historien]] [[suisse]].",u"'''Jean-François Bergier''', né à [[Lausanne]], le 5 décembre 1931 et mort le 29 octobre 2009&lt;ref name=&quot;swissinfo&quot;/&gt;, est un [[historien]] [[suisse]]."],
        ]
        for t in tests:
            self.assertEqual(self.sfrt.fr_saveDateTemplates(t[0]), t[1])

    def testSimpleSiecle(self):
        tests=[
                [u"{{Ier siècle}}, {{IIe siècle}}, ... {{XXe siècle}}, ...",u"Ier siècle, IIe siècle, ... XXe siècle, ..."],
                [u"{{Ier siècle av. J.-C.}}, {{IIe siècle av. J.-C.}}, ...",u"Ier siècle av. J.-C., IIe siècle av. J.-C., ..."],
                [u"{{Ier millénaire}}, {{IIe millénaire}}, ...",u"Ier millénaire, IIe millénaire, ..."],
                [u"{{Ier millénaire av. J.-C.}}, {{IIe millénaire av. J.-C.}}, ...",u"Ier millénaire av. J.-C., IIe millénaire av. J.-C., ..."],
        ]
        for t in tests:
            self.assertEqual(self.sfrt.fr_saveSimpleSieclesTemplates(t[0]), t[1])



    def testTemperature(self):
        tests=[
                [u"température supérieure à {{tmp|10|°C}}.",u"température supérieure à 10°C."],
                [u"Il se décompose de façon explosive aux alentours de {{tmp|95|°C}}.",u"Il se décompose de façon explosive aux alentours de 95°C."],
                [u"Entre 40 et {{tmp|70|°C}}",u"Entre 40 et 70°C"],
                 ]
        for t in tests:
            self.assertEqual(self.sfrt.fr_saveTemperatureTemplates(t[0]), t[1])

    def testSiecle(self):
        tests=[
                ["{{s|III|e}}",     u"IIIe siècle"],
                ["{{-s|III|e}}",    u"IIIe siècle av. J.-C. "],
                ["{{s-|III|e}}",    u"IIIe siècle"],
                ["{{-s-|III|e}}",   u"IIIe siècle av. J.-C. "],
                ["{{s2|III|e|IX|e}}",   u"IIIe et IXe siècles"],
                ["{{-s2|III|e|IX|e}}",  u"IIIe et IXe siècles av. J.-C. "],
                ["{{s2-|III|e|IX|e}}",  u"IIIe et IXe siècles"],
                ["{{-s2-|III|e|IX|e}}",     u"IIIe et IXe siècles av. J.-C. "],
                [u"{{s-|XIX|e|}}", u"XIXe siècle"],

        ]
        for t in tests:
            self.assertEqual(self.sfrt.fr_saveSieclesTemplates(t[0]), t[1])
    def testUnit(self):
        tests=[
                [u"{{Unité|1234567}}","1 234 567"],
                [u"{{Unité|1234567.89}}","1 234 567.89"],
                [u"{{Unité|1234567,89}}","1 234 567.89"],
                [u"{{Unité|1.23456789|e=15}}",u"1.23456789×10<sup>15</sup>"],
                [u"{{Unité|10000|km}}",u"10 000 km"],
                [u"{{Unité|10000|km/h}}","10 000 km/h"],
                [u"{{Unité|10000|km|2}}","10 000 km<sup>2</sup>"],
                [u"{{Unité|10000|m|3}}","10 000 m<sup>3</sup>"],
                [u"{{Unité|10000|km||h|-1}}",u"10 000 km⋅h<sup>-1</sup>"],
                [u"{{Unité|10000|J|2|K|3|s|-1}}",u"10 000 J<sup>2</sup>⋅K<sup>3</sup>⋅s<sup>-1</sup>"],
                [u"{{Unité|10000|J||kg||m|-2}}",u"10 000 J⋅kg⋅m<sup>-2</sup>"],
                [u"{{Unité|-40.234|°C}}",u"-40.234 °C"],
                [u"{{Unité|1.23456|e=9|J|2|K|3|s|-1}}",u"1.23456×10<sup>9</sup> J<sup>2</sup>⋅K<sup>3</sup>⋅s<sup>-1</sup>"],
                [u"{{Unité|1,23456|e=9|J|2|K|3|s|-1}}",u"1.23456×10<sup>9</sup> J<sup>2</sup>⋅K<sup>3</sup>⋅s<sup>-1</sup>"],
        ]
        for t in tests:
            self.assertEqual(self.sfrt.fr_saveUnitsTemplates(t[0]), t[1])

    def testFormatNum(self):
        tests=[
                [u"Elle comporte plus de {{formatnum:1000}} [[espèce]]s dans {{formatnum:90}}",u"Elle comporte plus de 1 000 [[espèce]]s dans 90"],
                ]
        for t in tests:
            self.assertEqual(self.sfrt.fr_saveFormatnumTemplates(t[0]), t[1])

    def testJaponais(self):
        tests=[
                [u"{{Japonais|'''Happa-tai'''|はっぱ隊||Brigade des feuilles}}",u"'''Happa-tai''' (はっぱ隊, , Brigade des feuilles)"],
                [u"{{Japonais|'''Lolicon'''|ロリータ・コンプレックス|''rorīta konpurekkusu''}}, ou {{japonais|'''Rorikon'''|ロリコン}}",u"'''Lolicon''' (ロリータ・コンプレックス, ''rorīta konpurekkusu''), ou '''Rorikon''' (ロリコン)"],
            ]

        for t in tests:
            self.assertEqual(self.sfrt.fr_saveJaponaisTemplates(t[0]), t[1])



def main():
    unittest.main()

if __name__ == '__main__':
    main()

