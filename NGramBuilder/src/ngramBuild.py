# -*- coding: UTF-8 -*-
""" 
 Created by Joir-dan Gumbs
 Date: 2010-07-15
 D2DB: Ngram Frequency Generator
 ngramBuild.py
"""
import os
import re
import sys

ACCEPTED_FILE_EXT = [".corpus", ".txt"]
BREAKS = ["'", ".", ",", "?", "!", "/", "\"", "\"", ";", ":", ")", "-", "]", "—", "•", "·"]
FORWARD_BREAKS = ["'", "\"", "(", "[", "•", "·", "#", "=", "*", "?", "_"]
DUMP_TEXT = ["\".",",\"", ".\"", ".[", ",[", "\";", "\":", "..","[ ", "\"[", ").", "].", ".\"", ",\"","?)", ")?", ")!", "!)", ");", "):", "(#", " •"," ·", ") " ]
REPLACE = {"&":"and"} 

LOCATION = None
EXIT = -1
END = -2
OLD = 2
CONTINUE = 1

NUMERIC_AND_DATE_REGEX = "([$({+-]+ (\s)?)?[\d]+( (\s)? ([,./^*+-e]+)? [\d]+ ([%)}^!])? (\s)? (A(.)?D(.)?|B(.)?C(.)?(E(.)?)?|C(.)?E(.)?)? (th|rd|st|)?)?"

class NgramBuild(object):
    def __printProcessNfo(self):
        print("[NGRAMBUILD] module name: {0}".format(__name__))
        print("[NGRAMBUILD] PID: {0}".format(os.getpid()))
        print("[NGRAMBUILD] Parent PID: {0}".format(os.getppid()))
        print("[NGRAMBUILD] Corpus Location: {0}".format(self.__location))
    
    def __initVars(self, toCache, shutoffCache, loc):
        self.__toCache = toCache
        self.__cacheOff = shutoffCache
        self.__location = loc
        self.__newTerms = []
        self.__docFiles = []
        self.__archiveFiles = []
        
    def __ngram(self, i, maxLvl=3):
        global BREAKS, FORWARD_BREAKS, EXIT, CONTINUE
        if i > len(self.__newTerms): return
        else:
            gram = ""
            for k in range(maxLvl):
                if i+k > len(self.__newTerms): break
                else:
                    if self.__newTerms[i+k] in BREAKS or self.__newTerms[i+k] in FORWARD_BREAKS: break
                    gram = (" " if k != 0 else "").join([gram, self.__newTerms[i+k].title()])
                    if self.__cacheOff.poll(): return EXIT
                    if not re.match(NUMERIC_AND_DATE_REGEX, gram) or k != 0: 
                        self.__toCache.put(gram, block=True)
        return CONTINUE
    
    def run(self):
        fileCount = 0
        # Check for already used files...
        for filename in filter(lambda x: os.path.splitext(x)[1] == ".used", os.listdir(self.__location)):
            with open(os.path.join(self.__location, filename), "r") as archive:
                for file in archive:
                    self.__archiveFiles.append(file.rstrip())     
        for w in self.__archiveFiles: print("[ARCHIVEFILE] {0}".format(w))     
        for filename in filter(lambda x: os.path.splitext(x)[1] in ACCEPTED_FILE_EXT, os.listdir(self.__location)):
            print("[DIRECTORY] {0}".format(filename))
            if filename not in self.__archiveFiles: 
                print("[NGRAMBUILD] FOUND NEW FILE: {0}".format(filename))
                self.__docFiles.append(os.path.join(self.__location, filename))
                fileCount+= 1
        self.__makeNgrams()
        print("[NGRAMBUILD] All ngrams extracted...")
        self.__toCache.put(END, block=True)
        
    def __makeNgrams(self):
        global REPLACE, FORWARD_BREAKS, BREAKS, DUMP_TEXT
        for docFile in self.__docFiles:
            print("[NGRAMBUILD] Opening File: [{0}]".format(docFile))
            corpus = None
            with open(docFile, "r") as corpusFile:
                corpus = corpusFile.read()
            list = corpus.split()
            for ng in list:
                if self.__cacheOff.poll():return
                ngTemp = ng
                fTemp = None
                bTemp = None
                if ngTemp in REPLACE.keys():
                    ngTemp = REPLACE[ngTemp]
                if ngTemp[0] in FORWARD_BREAKS: 
                    fTemp = ngTemp[0]; ngTemp = ngTemp[1:]
                if len(ngTemp) == 0: continue
                else:
                    if ngTemp[len(ngTemp)-1] in BREAKS:
                        bTemp = ngTemp[len(ngTemp)-1]; ngTemp = ngTemp[:len(ngTemp)-1] 
                    if len(ngTemp) == 0: continue
                    else:
                        for i in range(0,len(ngTemp)-2):
                            jump = False
                            for j in range(1, len(ngTemp)-1):
                                niko = "".join([ngTemp[i], ngTemp[j]])
                                if niko in DUMP_TEXT:
                                    bTemp = ngTemp[i]
                                    ngTemp = ngTemp[:i]
                                    jump = True
                                    break
                            if jump: break
                        if fTemp != None: self.__newTerms.append(fTemp)
                        self.__newTerms.append(ngTemp)
                        if bTemp != None: self.__newTerms.append(bTemp)
            self.__newTerms.append("!")
        for i in range(len(self.__newTerms)):
            if i % 1000 == 0 and i != 0: print("[NGRAMBUILD] {0} terms being processed".format(i))
            if self.__ngram(i) == EXIT: return 
            
    
    def __init__(self, toCache, shutoffCache, location, verbose = False):
        self.__initVars(toCache, shutoffCache, location)
        self.__printProcessNfo()
        self.run()
        