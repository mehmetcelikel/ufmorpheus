'''
Created on May 26, 2010

@author: Joir-dan
'''

import re

import nltk
from nltk import tokenize


class TermExtractor(object):
    def __init__(self):
        self.__baseGrammar = """ 
                                 VG: {<RB>?<V.*>+<RB|ADJPh>?}
                                  N: {(<NN.*>+)|<PRP>}
                                ADV: {<RB>}
                                  P: {<IN|TO>}
                                 WH: {<W.*>}
                                POS: {<POS>}
                              ADJPh: { <RBS>?(<JJ.*>+)?<CD>? }
                            PreNoun: {<DT|PRP\$|WP\$>}
                                END: {<\.>} """
        self.__lvl1Grammar = "NPh: {<PreNoun>?(<ADJPh>*)?<N>}"
        self.__lvl2Grammar = "PPh: {<P><NPh|ADJPh>}"
        self.__lvl3Grammar = "NPh: {<NPh>(<POS><NPh>)+}"
        self.__lvl4Grammar = "NPh: {<NPh><PPh>}"
        self.__lvl5Grammar = "WHNPh: {<WH><NPh>}"
        self.__lvl6Grammar = "VPh: {<VG><NPh|WHNPh|PPh|WH>}"
        #END: Grammars 
        self.__grammarList = [self.__baseGrammar, self.__lvl1Grammar, self.__lvl2Grammar, 
                              self.__lvl3Grammar, self.__lvl4Grammar, self.__lvl5Grammar, 
                              self.__lvl6Grammar]
        
    def run(self, query, verboseMode):
        print
        self.__query = query
        self.__resetVars(verboseMode)
        self.__firstTokenize(query)
        self.__thenChunkQuery()
        self.__andFinallyExtractTerms()
        self.__createNGramsForExporting()
        self.__formatForExporting()
        return self.__returnGroup

    def __firstTokenize(self, query):
        self.__terms = tokenize.word_tokenize(query)
        self.__taggedSentence = nltk.pos_tag(self.__terms)
        if self.__VERBOSE: 
            print(self.__taggedSentence)
            print

    def __thenChunkQuery(self):
        tempChunk = self.__taggedSentence
        for i in range(len(self.__grammarList)):
            grammar = self.__grammarList[i]
            chunkParser = nltk.RegexpParser(grammar)
            tempChunk = chunkParser.parse(tempChunk)
            if self.__VERBOSE: 
                print("Chunk level: "+str(i))
                print(tempChunk)
                print
        self.__finalChunk = tempChunk
    
    
    
    def __createNGramsForExporting(self): 
        newTerms = []
        ngrams = []
        breaks = [".", ",", "?", "!", "\""]
        # Ngram Closure
        def __nGram(i,  maxLvl=3):
            if i > len(newTerms): return
            else:
                gram = ""
                for k in range(maxLvl):
                    if i + k > len(newTerms): break
                    else:
                        if newTerms[i+k] in breaks: break
                        gram += (" " if k != 0 else "") + newTerms[i+k]
                        ngrams.append(gram)
        
        for term in self.__returnGroup["Descriptive_Info"]:
            list = term.split()
            for ng in list:
                if ng[len(ng)-1] in breaks:
                    newTerms.append(ng[:len(ng)-1]) # Take stuff before the breakchar
                    newTerms.append(ng[len(ng)-1])  # Take the breakchar
                elif ng[0] != "@" : newTerms.append(ng)
                else: newTerms.append("?") #So we can deal with multiples in one go...
            newTerms.append("!")
        for term in self.__returnGroup["Asking_For"]:
            list = term.split()
            for ng in list:
                if ng[len(ng)-1] in breaks:
                    newTerms.append(ng[:len(ng)-1]) # Take stuff before the breakchar
                    newTerms.append(ng[len(ng)-1])  # Take the breakchar
                elif ng[0] != "@" : newTerms.append(ng)
                else: newTerms.append("?") #So we can deal with multiples in one go...
            newTerms.append("!")
            
        for i in range(len(newTerms)):  __nGram(i)
        self.__nGrams.extend(ngrams)
            

    def __formatForExporting(self):
        str = "QU:" +      self.__query+";"
        str+= "AF:" + ", ".join(self.__returnGroup["Asking_For"])+";"
        str+= "DI:" + ", ".join(self.__returnGroup["Descriptive_Info"])+";"
        str+= "VG:" + ", ".join(self.__returnGroup["Verb_Groups"])+";"
        str+= "NG:" + ", ".join(self.__nGrams)+";"
        str+= "WH:" + (self.__returnGroup["WH_Term"])+";"
        self.__exportVar = str
        
    def getExportString(self):
        return self.__exportVar

    def showResults(self):
        #print self.__npCache
        for key, value in self.__flags.items(): print(key+": "+str(value))
        for key, value in self.__returnGroup.items(): 
            print key,value

    def __resetVars(self, verboseMode):
        self.__VERBOSE = verboseMode
        if self.__VERBOSE: print("Verbose: True" if self.__VERBOSE is True else "Verbose: False")
        self.__flags = {"Found_What": False, "Found_Verb": False , "Found_Object" : False, "Has_WHNP" : False, "Has_Multiple_WHNP": False}
        self.__returnGroup = {"Asking_For": [], "Descriptive_Info" : [], "Verb_Groups": [], "WH_Term": None }
        self.__terms = None
        self.__taggedSentence = None
        self.__npCache = []
        self.__exportVar = None
        self.__nGrams = []
    
    def __foundWHNPh(self, phrase):
        if self.__flags["Has_WHNP"]: self.__flags["Has_Multiple_WHNP"] = True
        else: self.__flags["Found_What"] = self.__flags["Has_WHNP"] = self.__flags["Found_Object"] = True
        self.__returnGroup["Asking_For"].append(" ".join(nltk.tag.untag(phrase[1].leaves())))
        self.__returnGroup["WH_Term"] = " ".join(nltk.tag.untag(phrase[0].leaves()))
    
 

    def __dealWithNPCache(self):
        #Use of @flags allows us to retain some form of the original query
        FOUND_WH = False
        FOUND_VG = False
        MULT_VG = False
        if self.__VERBOSE:
            print("Length of Cache: "+str(len(self.__npCache)))
        for term in self.__npCache:
            if term == "@what": FOUND_WH = True 
            elif term == "@verb": 
                if FOUND_VG: MULT_VG = True
                FOUND_VG = True
            else:
                if term[0].node != "NPh": #Then we have something interesting... Question of form <NPh>(<and/or><NPh>)+<verb><WHNPh|WH> we wont deal w/ it now tho...
                    if self.__returnGroup["WH_Term"] == "what":
                        self.__returnGroup["Descriptive_Info"].append(" ".join(nltk.tag.untag(term.leaves())))
                        if not self.__flags["Has_WHNP"]: self.__returnGroup["Asking_For"].append("@description")
                        self.__flags["Found_Object"] = True
                    elif self.__returnGroup["WH_Term"] == "where":
                        self.__returnGroup["Descriptive_Info"].append(" ".join(nltk.tag.untag(term.leaves())))
                        self.__returnGroup["Asking_For"].append("@location")
                    elif self.__returnGroup["WH_Term"] == "when":
                        self.__returnGroup["Descriptive_Info"].append(" ".join(nltk.tag.untag(term.leaves())))
                        self.__returnGroup["Asking_For"].append("@time")
                    if self.__VERBOSE:
                        print(" ".join(nltk.tag.untag(term.leaves())))
                else:
                    position = 1
                    POS_FLAG = False
                    if self.__VERBOSE:
                        print("Number of terms after NPh(1): "+ str(len(term[1:])))
                    for chunk in term[1:]:
                        if self.__VERBOSE: print chunk
                        if chunk.node == "POS": # Then we have <NPh><POS><NPh>((<POS><NPh>)+)?
                            if not POS_FLAG: POS_FLAG = True
                            self.__returnGroup["Descriptive_Info"].append(" ".join(nltk.tag.untag(term[position-1].leaves())))
                            POS_FLAG = True
                            
                        elif chunk.node == "NPh":
                            if POS_FLAG:
                                if self.__flags["Has_WHNP"]:
                                    self.__returnGroup["Descriptive_Info"].append(" ".join(nltk.tag.untag(chunk.leaves())))
                                else:
                                    self.__returnGroup["Asking_For"].append(" ".join(nltk.tag.untag(chunk.leaves())))
                                    self.__flags["Found_Object"] = True
                            else:
                                pass # Remember, not ready to deal w/ the other stuff... for now lets EX: Japan is in what continent
                        elif chunk.node == "PPh":
                            self.__returnGroup["Descriptive_Info"].append(" ".join(nltk.tag.untag(chunk.leaves())))
                            self.__returnGroup["Asking_For"].append(" ".join(nltk.tag.untag(term[0].leaves())))
                            break
                        #Eventually I need to deal w/ ands and ors...
                        position += 1 
                
                
                    
    def __foundNPh(self, phrase):
        if self.__flags["Has_WHNP"]:
            self.__returnGroups["Descriptive_Info"].append(" ".join(nltk.tag.untag(phrase.leaves())))
        else: self.__npCache.append(phrase)
         
    
    def __foundWH(self, phrase):
        self.__flags["Found_What"] = True
        self.__returnGroup["WH_Term"] = " ".join(nltk.tag.untag(phrase.leaves())).lower()
        if self.__returnGroup["WH_Term"] == "when": self.__returnGroup["Asking_For"].append("@time")
        self.__npCache.append("@what")
    
    def __foundPPh(self, phrase):
        for chunk in phrase:
            if chunk.node == "WHNPh":
                self.__foundWHNPh(chunk)
            elif chunk.node == "NPh":
                self.__returnGroup["Descriptive_Info"].append(" ".join(nltk.tag.untag(phrase.leaves())))

    def __foundVPh(self, phrase):
        for group in phrase:
            if group.node == "VG":                                                                         # Verb Group Found
                self.__returnGroup["Verb_Groups"].append(" ".join(nltk.tag.untag(group.leaves())))         # Add it to return dictionary
                self.__flags["Found_Verb"] = True
                self.__npCache.append("@verb")
            elif group.node == "NPh":                                                                      # Noun Phrase Found
                if self.__flags["Has_WHNP"]:                                                               #    If WHNPh was found
                    self.__returnGroup["Descriptive_Info"].append(" ".join(nltk.tag.untag(group.leaves())))#        Add this to Desc. Info
                else: self.__npCache.append(group)                                                         #    Else Add it to NP Cache
            elif group.node == "PPh":                                                                      # Prepositional Phrase Found
                self.__foundPPh(group)                                                                     #    Send to @method __foundPPh()
            elif group.node == "WHNPh":                                                                    # WH Noun Phrase found
                self.__foundWHNPh(group)                                                                   #    Send to @method __foundWHNPh()
            elif group.node == "WH":                                                                       # WH Term found
                self.__foundWH(group)                                                                      #    Send to @method __foundWH()

                    
                
    def __andFinallyExtractTerms(self):
        weDidntFindAnEndChunk = True
        if self.__VERBOSE:
            print("Extracting Terms.")
            print("Number of Phrases: "+str(len(self.__finalChunk)))
        for phrase in self.__finalChunk:
            if phrase.node == "WHNPh":          # WH-Noun Phrase was found
                if self.__VERBOSE: print(phrase.leaves())
                self.__foundWHNPh(phrase)
            elif phrase.node == "WH":           # WH term found
                if self.__VERBOSE: print(phrase.leaves())
                self.__foundWH(phrase)
            elif phrase.node == "PPh":
                self.__foundPPh(phrase)
            elif phrase.node == "NPh":          # Noun Phrase found...
                if self.__VERBOSE: print(phrase.leaves())
                self.__foundNPh(phrase)         
            elif phrase.node == "VPh":          # Verb Phrase found
                self.__foundVPh(phrase)
            elif phrase.node == "END":
                if self.__VERBOSE: print("End of question")
                weDidntFindAnEndChunk = False
                self.__dealWithNPCache();
            else: 
                if self.__VERBOSE: print("ERROR")
        if weDidntFindAnEndChunk:
            self.__dealWithNPCache()
            
