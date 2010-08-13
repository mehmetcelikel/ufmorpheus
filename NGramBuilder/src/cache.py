'''
Created on Jul 24, 2010
cache.py
@author: Joir-dan
'''

import os
import sys

START = 1
EXIT = -1
HAPAX = 0
END = -2
OLD = 2

class Cache(object):
    def __printProcessNfo(self):
        print("[CACHE] module name ", __name__)
        print("[CACHE] PID: ", os.getpid())
        print("[CACHE] Parent PID: ", os.getppid())
    
    def __initFlags(self, verbose):
        self.__oldHapax = False
        self.__verbose = verbose
        
    def __initVars(self, fromNgram, toDBConn, shutoff, ngramShutoff):
        self.__fromNgram = fromNgram
        self.__toDBConn = toDBConn
        self.__shutoff = shutoff
        self.__shutoffswitch = ngramShutoff
        self.__cache = dict()
        self.count = 0
        
    def __init__(self, fromNgram, toDBConn, shutoff, ngramShutoff, verbose=False):
        if verbose: self.__printProcessNfo()
        self.__initFlags(verbose)
        self.__initVars(fromNgram, toDBConn, shutoff, ngramShutoff)
        self.run()
    
    def run(self):
        global END, OLD
        self.__toDBConn.put(START)
        while True:
            if self.count % 2500 == 0: print("[CACHE] Number of ngrams seen: [{0}]".format(self.count))
            if self.__shutoff.poll():
                self.__cache = None
                self.__shutoffswitch.send_bytes("0")
                return
            ngram = self.__fromNgram.get(block=True)
            if ngram == END: break
            elif ngram == OLD:
                self.__oldHapax = True
            elif ngram in self.__cache.keys():
                if self.__cache[ngram] == 1:
                    print("[CACHE] Second Sighting of [{0}]".format(ngram))
                    self.__toDBConn.put(ngram)
                self.__cache[ngram] += 1
                self.__toDBConn.put(ngram)
                self.count = self.count + 1 if not self.__oldHapax else self.count
            else:
                self.__cache[ngram] = 1
                self.count = self.count + 1 if not self.__oldHapax else self.count
        self.__toDBConn.put(HAPAX)
        hapaxes = [w for w in self.__cache.keys() if self.__cache[w]==1]
        for term in hapaxes: self.__toDBConn.put(term)
        self.__toDBConn.put(EXIT)
        self.__toDBConn.put(self.count)