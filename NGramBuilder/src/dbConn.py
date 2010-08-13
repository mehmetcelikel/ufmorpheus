""" 
 Created by Joir-dan Gumbs
 Date: 2010-07-15
 D2DB: Ngram Frequency Generator
 dbConn.py
"""
import os
import sys
import psycopg2

START = 1
EXIT = -1
HAPAX = 0
CONN_STRING = ""

class DBConn(object):
    def __printBootNfo(self):
        print("[DBCONN] module name ", __name__)
        print("[DBCONN] PID: ", os.getpid())
        print("[DBCONN] Parent PID: ", os.getppid())
        print("[DBCONN] Category: ", self.category)
        
    def __initVars(self, queue, shutoff, cat, loc):
        self.__cacheQueue = queue
        self.__shutoff = shutoff
        self.category = cat
        self.__location = loc
        self.__conn = None
        self.__cursor = None
        self.__tmpHapax = []
    
    def __initFlags(self):
        self.__hapaxFlag = self.__oldFlag = False
             
    def __grabConn(self):
        global CONN_STRING
        print("[DBCONN] ATTEMPTING TO GRAB DB CONNECTION")
        try: self.__conn = psycopg2.connect("host=babylon.cise.ufl.edu dbname=NLPDB user=morpheus3 password=crimson03.sql")
        except psycopg2.OperationalError:
            self.__shutoff.send_bytes("0")
            self.__conn = -1
            err = sys.exc_info()
            print("[DBCONN] Connection to DB Failed: {0}\n{1}\n{2}".format(err[0], err[1], err[2]))
    
    def __exit(self):
        # Prepare to close dbconn after updating category table...
            count = self.__cacheQueue.get(block=True)
            #Prepare to send logger message
            print("[DBCONN] ATTEMPTING TO COMPLETE EXECUTION")
            # Prepare to write hapax file, and file of read files
            try:
                self.__cursor.execute("UPDATE category SET count = count + %s WHERE category = %s;", (count, self.category, ))
                self.__conn.commit()
                self.__cursor.close()
                # Now deal with the hapax legomena that are left...
                with open(os.path.join(self.__location, ".".join([self.category, "hapax"])), "w") as hapaxFile:
                    for hapax in self.__tmpHapax:
                        print >> hapaxFile, hapax
                # Finally, write a file that notes the corpora we ve counted already...
                with open(os.path.join(self.__location, ".".join([self.category, "used"])), "a") as logFile:
                    for file in filter(lambda x: os.path.splitext(x)[1] in [".corpus", ".txt"], os.listdir(self.__location)):
                        print >> logFile, file
                print("[DBCONN] EXECUTION COMPLETED")
            except:
                self.__conn.rollback()
                err = sys.exc_info()
                print("[DBCONN] FAILED TO PROPERLY CLOSE: {0}\n{1}\n{2}".format(err[0], err[1], err[2]))
            finally:
                self.__conn.close()
    def __start(self):
        if self.__conn == -1:
            self.__shutoff.send_bytes("0")
            sys.exit()
        else:
            try:
                self.__cursor = self.__conn.cursor()
                # We need to check if the category exists in database
                self.__cursor.execute("SELECT 1 FROM category WHERE category = %s;", (self.category,))
                if self.__cursor.fetchone() is None:
                    print("[DBCONN] Creating category {0}".format(self.category))
                    self.__cursor.execute("INSERT INTO category (category, count) VALUES (%s, %s);", (self.category,0,))
                    self.__conn.commit()
                    self.__cursor.close()
                else: print("[DBCONN] Found category {0}".format(self.category))
            except:
                err = sys.exc_info()
                print("[DBCONN] Connection to DB Failed: {0}\n{1}\n{2}".format(err[0], err[1], err[2]))
                self.__conn.rollback()
                self.__conn.close()
                self.__shutoff.send_bytes("0")
                sys.exit()
            try:
                self.__cursor = self.__conn.cursor()
            except:
                err = sys.exc_info()
                print("[DBCONN] Building Cursor Failed (\"Doc2DB Execution\"): {0}\n{1}\n{2}".format(err[0], err[1], err[2]))
                self.__shutoff.send_bytes("0")  
    
    def __pushData(self, datagram):
        try:
            #Check the db for the term...
            self.__cursor.execute("SELECT 1 FROM category_has_gram WHERE gram = %s AND category = %s;", (datagram, self.category,))
            if self.__cursor.fetchone() is not None: # Then it exists in the database... 
                self.__cursor.execute("UPDATE category_has_gram SET count = count + 1 WHERE category = %s AND gram = %s", (self.category, datagram, ))
            else:
                if not self.__hapaxFlag: # Normal Operation...
                    self.__cursor.execute("INSERT INTO category_has_gram (category, gram, count) VALUES(%s,%s,%s);", (self.category, datagram, 1,))
                    self.__conn.commit()
                else: #it doesn't exist in the db, so we need a way to access it again...
                    self.__tmpHapax.append(datagram)
        except:
            print("[DBCONN] ATTEMPT to insert {0} into category_has_gram with category {1}. HapaxFlag = {2} Failed".format(datagram, self.category, self.__hapaxFlag))
            print(sys.exc_info())
            self.__conn.rollback()
            self.__shutoff.send_bytes("0")
            self.__conn.close()
    
    def run(self):
        global EXIT, START, HAPAX
        while True:
            datagram = self.__cacheQueue.get(block=True)
            if datagram == EXIT: 
                self.__exit()
                break
            elif datagram == START: 
                self.__start()
                print("STARTING UP DB")
            elif datagram == HAPAX:
                self.__hapaxFlag = True
                print("DEALING with HAPAX now")
            else: self.__pushData(datagram)
        
    
    def __init__(self, fromCacheQueue, cacheShutoffPipe, category, location, verbose):
        self.__initVars(fromCacheQueue, cacheShutoffPipe, category, location)
        self.__initFlags()
        if verbose: self.__printBootNfo()
        self.__grabConn() 
        self.run()
