""" 
 Created by Joir-dan Gumbs
 Date: 2010-07-15
 D2DB: Ngram Frequency Generator
 boot.py
"""

from multiprocessing import Process, Queue, Pipe
import os
import ngramBuild, dbConn, cache

DEFAULT_LOGFILE_DIR = os.path.abspath("../LogFile/")

choices = range(1,3)
menu1 = """
       D2DB: Menu 
       1 : Run from folder of documents
       2 : Exit
       """

menu2A = "Folder Location: "
menu2C = "Document Category: "
def __getMenu1():
    c = -1
    while c not in choices:
        print(menu1)
        c = int(raw_input("Choice: "))
    return c

def __getMenu2(c):
    location = category = ""
    isDir = False
    if c == 1:
        while not os.path.isdir(location): 
            location = raw_input((menu2A))
            if not os.path.isdir(location): print("Not a valid Directory")
        isDir = True
        category = __getDocCategory()
    else: location = category = -1
    return (location, isDir, category)

def __getDocCategory():
    category = ""
    while len(category) < 3: # Categories should be larger than two letters...
        category = raw_input(menu2C)
    return category

    
def __init():    
    choice1 = __getMenu1()
    if choice1 == 2: return -1
    else:
        location, isDir, category = __getMenu2(choice1)
        if -1 in [location, category]: return -1
        else:
            return (location, isDir, category)
                 
def __run(location, isDir, category):
    # Object Passing pipe/queue building
    ngram2CacheQueue = Queue(maxsize=5000)
    cache2DbQueue = Queue(maxsize=500)
    
    (ngram2CacheShutoff, ngramFromCacheShutOff) = Pipe(duplex=True)
    (db2CacheShutOff, cacheFromDBShutOff) = Pipe(duplex=True)
    
    # Process Creation
    ngramProc = Process(target=ngramBuild.NgramBuild, args=(ngram2CacheQueue, ngramFromCacheShutOff, location, True))
    cacheProc = Process(target=cache.Cache, args=(ngram2CacheQueue, cache2DbQueue,cacheFromDBShutOff, ngram2CacheShutoff, True)) #Need to add shutoffs here...
    dbConProc = Process(target=dbConn.DBConn, args=(cache2DbQueue, db2CacheShutOff, category, location, True))
    
    # Process Forking
    ngramProc.start()
    cacheProc.start()
    dbConProc.start()
    
    ngramProc.join()
    cacheProc.join()
    dbConProc.join()
    
    if ngramProc.is_alive(): ngramProc.terminate()
    if cacheProc.is_alive(): cacheProc.terminate()
    if dbConProc.is_alive(): dbConProc.terminate()

def main():
    tuples = __init()
    if tuples == -1: return
    else: 
        location, isDir, category = tuples
        __run(location, isDir, category)

    
    

