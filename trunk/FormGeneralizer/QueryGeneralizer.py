"""
    QueryGeneralizer is responsible for generalizing two paths for answering a given query. It generalizes matching pages 
    using the FormGeneralizer
"""
__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

import pdb
import sys
import Loader
from Qrm import Qrm
import lxml
import lxml.html    
sys.path.append('../badica')
from ExtractionPath import ExtractionPath
from Badica import Badica
from Highlight import Highlight
from Page import Page
from ScriptBuilder import ScriptBuilder

#This method accepts two queries which have been determined to be similar 
def GeneralizeQueries(queryid, qrmid):

#Build Tree
    #Select all pRefs for each query sorted by timestamp

    qrm1 = Qrm()
    qrm2 = Qrm()    
        
    if qrmid != None:
        qrm1 = Loader.GetQRMFromDb(qrmid)
    else:
        qrm1 = Loader.GetQRMFromQuery(queryid)

    if qrm1 == None or qrm2 == None:
        print("Query data not found")
        return    
    
    qrm2 = Loader.GetQRMFromQuery(queryid)
    
#Check queries for appropriateness of generalization
    if qrm1.pageLength() != qrm2.pageLength():
       print("Entities containing differing page lengths cannot be generalized")    
       return
   
    #if !qrm1.matchInputOutputClasses(qrm2):
    #   print("Both entities must have matching SSQ input/output classes")
    #   return

    #Need to ensure the usages of the SSQ inputs and highlights is the same between both entities
     
#Normalization
    #Normalize each query separately
    qrm1 = normalize(qrm1)
    
    qrm2 = normalize(qrm2)
    
#Generalization
    #generalize all matching pages (same page, same form)
    #Whichever tree has fewer nodes, replace all matching nodes with their generalized versions
    #remaining nodes are left alone

    genQrm = generalizeQRMS(qrm1,qrm2)

#Script generation

    #Generate the actually text of the script, this is to be stored in the 'code' field in the qrm table
    #code = ScriptBuilder.create(genQrm,qrm1,qrm2)

    #insert code with new qrm into database 
    #Loader.insertQrm(genQrm, code)
    
#Rules:
#A page with a highlight must be retained in canonical set
#A page that links to a page in the above set must be retained
def normalize(q):
    
    savedPages = list()

    for y in range(q.pageLength()-1,-1,-1):

        p = q.pageList[y]
        
        #If page has highlights save it
        if len(p.outputs) > 0:
            savedPages.insert(0,p)
        else:
        
            sp = savedPages.pop(0)
            
            #If this page links to page that has been saved, save it too
            if linkBetweenPages(p,sp):
                
                savedPages.insert(0,sp)
                
                savedPages.insert(0,p)
            else:
                savedPages.insert(0,sp)

        y = y - 1

    q.pageList = savedPages

    return q  

def linkBetweenPages(p1, p2):
    import urllib

    url = urllib.quote(p2.url)
    
    url = url.replace("/","%2F")

    if p1.destinationUrl.find(url) >= 0:
        return True

    if p1.isFormSubmit:
        return True

    return False;

#Perform two extractions and compare them to see if they give the same result
def compareExtractions(genXpath, nonGenXpath, src):

    NO_SIMILARITY = 0
    WEAK_SIMILARITY = 1
    STRONG_SIMILARITY = 2
 
    root = lxml.html.fromstring(src)
    
    genNodes = root.xpath(genXpath)
    
    nonGenNodes = root.xpath(nonGenXpath)

    #1) If the number of nodes returned is not the same, but the correct node(s) are in both lists
    #   this represents weak similarity
    #2) If the number is the same and the nodes match, this is strong similarity
    #3) If there is no correspondence, then this is no similarity

    if len(nonGenNodes) == 0 or len(genNodes) == 0:
        return NO_SIMILARITY

    #Ensure that both lists have the same length
    if len(nonGenNodes) != len(genNodes):
    
	#See if nodes in one list are contained in the other

        matchCount = 0

        for n in nonGenNodes:
            for k in genNodes:
                if n.text_content() == k.text_content():
                    print("Generalized xpath extraction:\n "+k.text_content()+"\n")
                    print("Non-generalized xpath extraction:\n"+n.text_content()+"\n")
                    matchCount += 1

        if matchCount == len(nonGenNodes):
            return WEAK_SIMILARITY 
    
        return NO_SIMILARITY

    else:
        #see if nodes are exactly matching, if so return 2, otherwise 0
        for n in nonGenNodes:
            for k in genNodes:
                if n.text_content() != k.text_content():
                    return NO_SIMILARITY
                else:
                    print("Generalized xpath extraction:\n "+k.text_content()+"\n")
                    print("Non-generalized xpath extraction:\n"+n.text_content()+"\n")
    
    return STRONG_SIMILARITY
        
def generalizeQRMS(q1, q2):
    
    qS = None
    qL = None
    
    q3 = Qrm()
    
    #Find smaller query in terms of lenght of page refs
    if q1.pageLength() > q2.pageLength():
        qL = q1
        qS = q2
    else: 
        qL = q2
        qS = q1
    
    #For each page in qS, generalize the form xpath, the links clicked and the highlight xpaths
    
    for i in range(0, qS.pageLength()):

        gp = generalizePages(qS.pageList[i],qL.pageList[i])
  
        #Check if generalizing failed - Stop generalizing if so
        #Temporary approach
        if gp == None:
            print("Generalization of page: "+gp.url+" failed, exiting....\n")
            return None
        
        qS.pageList[i] = gp
        
    return qS

def generalizePages(p1, p2):

    NO_SIMILARITY = 0

    p3 = Page()
    
    g = Badica()
 
    p1Epath = ExtractionPath()
    
    p2Epath = ExtractionPath()
    
    #Check for validity of comparison
    if len(p1.links) != len(p2.links) | len(p1.outputs) != len(p2.outputs):
        return None
    
    print("Generalizing page: "+ p1.url +" content*************************\n")

    #Generalize form xpaths
    if p1.xpath != "":
        
        p1Epath.xpath_to_epath(p1.pagesrc,p1.xpath)
        
        p2Epath.xpath_to_epath(p2.pagesrc,p2.xpath)
        
        p3Epath = g.Learn([p1Epath, p2Epath])
        
        p3.xpath = p3Epath.epath_to_xpath()
    
        #Check the validity of the result
        if compareExtractions(p3.xpath,p1.xpath,p1.pagesrc) != NO_SIMILARITY:
            p3.xpath = p1.xpath
            print("Processed form xpath")
        else:
            print("Generalized form xpath is not valid, using non-generalized xpath\n")
    else:
        print("No form xpaths to generalize\n")
    
      
    #Generalize links
    
    for i in range(0, len(p1.links)):

        p1Epath.xpath_to_epath(p1.pagesrc,p1.links[i])
        
        p2Epath.xpath_to_epath(p2.pagesrc,p2.links[i])
        
        p3Epath = g.Learn([p1Epath,p2Epath])
        
        newxpath = p3Epath.epath_to_xpath()

        #Check the validity of the result
        if compareExtractions(newxpath,p1.links[i],p1.pagesrc) != NO_SIMILARITY:
             p3.links.append(newxpath)
        else:
            print("Generalized link xpath is not valid, using non-generalized xpath\n")
            p3.links.append(p1.links[i])

    if len(p1.links) == 0:
        print("No links to generalize\n")
    else:
        print("Processed html links\n")

    p1Epath = ExtractionPath()
    p2Epath = ExtractionPath()
    
    #Generalize outputs
    for i in range(0, len(p1.outputs)):
        
        h1 = p1.outputs[i]
        
        h2 = p2.outputs[i]
        
        #Get extraction paths to the start and end points
        p1Epath.xpath_to_epath(p1.pagesrc,h1.meetpoint)
        
        p2Epath.xpath_to_epath(p2.pagesrc,h2.meetpoint)
        
        #Generalize the start and end points
        p3Epath = g.Learn([p1Epath,p2Epath])
        
        genXpath = p3Epath.epath_to_xpath()

        print("xpath 1 before: "+h1.meetpoint+"\n")
        print("xpath 2 before: "+h2.meetpoint+"\n")
        print("xpath after : "+genXpath+"\n")

        #Currently, start and end xpaths are not used. 
        h3 = Highlight(genXpath,None,None,0,0)        

        #Check the validity of the result
        if compareExtractions(h3.meetpoint,h1.meetpoint,p1.pagesrc) != NO_SIMILARITY:
            p3.outputs.append(h3)
        else:
            print("Generalized highlight xpath is not valid, using non-generalized xpath\n")
            p3.outputs.append(h1)
    
    if len(p1.outputs) == 0:
        print("No outputs to generalize\n")
    else:
        print("Processed page outputs\n")
    
    print("Generalization Complete\n")
    
    return p3

if __name__ == '__main__':
    
    if len(sys.argv) == 3:
        GeneralizeQueries(sys.argv[1],sys.argv[2])
    else: #len(sys.argv) == 2:
        GeneralizeQueries(sys.argv[1],None)
    """else:
        print('USAGE: python QueryGeneralizer.py queryID qrmid')
        """
    pass    
    

