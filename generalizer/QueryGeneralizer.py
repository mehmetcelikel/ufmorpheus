"""
    QueryGeneralizer is responsible for generalizing two paths for answering a given query. It generalizes matching pages 
    using the FormGeneralizer
"""
__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

import pdb
import sys
from ActionType import ActionType
import Loader
from Qrm import Qrm
import lxml
import lxml.html    
sys.path.append('../qre')
sys.path.append('../badica')
from ExtractionPath import ExtractionPath
from Badica import Badica
import Highlight
import Form
import Link
from Page import Page
from ScriptBuilder import ScriptBuilder

#This method accepts two queries which have been determined to be similar 
def GeneralizeQueries(qrmid, queryid):

#Build Tree
    #Select all pRefs for each query sorted by timestamp

    qrm1 = Qrm()
    qrm2 = Qrm()    
        
    if qrmid != None:
        qrm1 = Loader.GetQRMFromDb(qrmid) # XXX <-- Bug, no pagereference.qrmid exists
    else:
        qrm1 = Loader.GetQRMFromQuery(queryid)

    qrm2 = Loader.GetQRMFromQuery(queryid)

    if qrm1 == None or qrm2 == None:
        print("Query data not found")
        return      
    
    #Check queries for appropriateness of generalization
    # TODO -- Need to deal with two queries of different lengths
    if qrm1.pageLength() != qrm2.pageLength():
       print("Entities containing differing page lengths cannot be generalized")    
       return
   
    # FIXME -- This assumtion is too restrictive, need to be able to handle all cases
    if qrm1.matchSsqClasses(qrm2) == False:
       print("Both entities must have matching SSQ input/output classes")
       return

    #Need to ensure the usages of the SSQ inputs and highlights is the same between both entities
     
    #Normalize each query separately
    #qrm1 = normalize(qrm1)
    
    #qrm2 = normalize(qrm2)
    
    #Generalization
    #generalize all matching pages (same page, same form)
    #Whichever tree has fewer nodes, replace all matching nodes with their generalized versions
    #remaining nodes are left alone

    genQrm = generalizeQRMS(qrm1,qrm2)

    #Script generation

    scriptGenerator = ScriptBuilder()
	
    #Generate the actually text of the script, this is to be stored in the 'code' field in the qrm table
    code = scriptGenerator.create(genQrm)
    
    #print('\n******************* QRE SCRIPT\n')

    #print(code)

    #insert code with new qrm into database 
    print('Inserting the new qrm into the database...')
    
    id = Loader.insertQrm(genQrm.realmId, code, queryid)

    print('done')

    print('New Qrm ID='+str(id))

#Rules:
#A page with a highlight must be retained in canonical set
#A page that links to a page in the above set must be retained
def normalize(q):
    
    savedPages = list()

    for y in range(q.pageLength()-1,-1,-1):

        p = q.pageList[y]
        
        #If page has highlights save it
        if p.actionType == ActionType.Highlight:
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

    if p1.actionType == ActionType.Form:
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

        gp = generalizeActions(qS.pageList[i],qL.pageList[i])

        #set the page url  
        gp.url = qS.pageList[i].url

        #set the desintation url 
        gp.destinationUrl = qS.pageList[i].destinationUrl

        #Check if generalizing failed - Stop generalizing if so
        #Temporary approach
        if gp == None:
            print("Generalization of page: "+gp.url+" failed, exiting....\n")
            return None

        qS.pageList[i] = gp

    qS.realmId = q1.realmId    

    return qS

def generalizeActions(p1, p2):

    NO_SIMILARITY = 0

    p3 = Page()
    
    g = Badica()
 
    p1Epath = ExtractionPath()
    
    p2Epath = ExtractionPath()
    
    #Check for validity of comparison
    if p1.actionType != p2.actionType:    
        return None
    
    print("Generalizing page: "+ p1.url +" content*************************\n")

    #Generalize form xpaths
    if p1.actionType == ActionType.Form:

        p1Epath.xpath_to_epath(p1.pagesrc,p1.xpath)
        
        p2Epath.xpath_to_epath(p2.pagesrc,p2.xpath)
        
        p3Epath = g.Learn([p1Epath, p2Epath])

        #what about form inputs?

        method = getFormMethod(p1)

        p3 = Form.New(p1.url, p1.destinationUrl, "", "", p1.pagesrc, method, p1.formInputs)

        #we use the input list from the first page, because they should be the same

        p3.xpath = p3Epath.epath_to_xpath()

        #Check the validity of the result
        if compareExtractions(p3.xpath,p1.xpath,p1.pagesrc) != NO_SIMILARITY:
            print("Processed form xpath")
        else:
            print("Generalized form xpath is not valid, using non-generalized xpath\n")
            p3.xpath = p1.xpath

    elif p1.actionType == ActionType.Link:    

        print('Processing link click action')

        p1Epath.xpath_to_epath(p1.pagesrc,p1.xpath)
        
        p2Epath.xpath_to_epath(p2.pagesrc,p2.xpath)
        
        p3Epath = g.Learn([p1Epath,p2Epath])
        
        newxpath = p3Epath.epath_to_xpath()

        p3 = Link.New(p1.url, p1.destinationUrl, "", "", "")

        #Check the validity of the result
        if compareExtractions(newxpath,p1.xpath,p1.pagesrc) != NO_SIMILARITY:
            p3.xpath = newxpath
        else:
            print("Generalized link xpath is not valid, using non-generalized xpath\n")
            p3.xpath = p1.xpath

        print("Done with link\n")

    elif p1.actionType == ActionType.Highlight:

        print('Processing highlight action')
        p1Epath = ExtractionPath()
        p2Epath = ExtractionPath()
    
       	#Get extraction paths to the start and end points
       	p1Epath.xpath_to_epath(p1.pagesrc,p1.meetpoint)
        
        p2Epath.xpath_to_epath(p2.pagesrc,p2.meetpoint)
        
        #Generalize the start and end points
        p3Epath = g.Learn([p1Epath,p2Epath])
        
        genXpath = p3Epath.epath_to_xpath()

        print("xpath 1 before: "+p1.meetpoint+"\n")
        print("xpath 2 before: "+p2.meetpoint+"\n")
        print("xpath after: "+genXpath+"\n")

        #Currently, start and end xpaths are not used. 
        genh = Highlight.New(genXpath,None,None,0,0,p1.url,None,p1.timestamp,p1.classId)        

        #Check the validity of the result
        if compareExtractions(genh.meetpoint,p1.meetpoint,p1.pagesrc) != NO_SIMILARITY:
            p3 = genh
        else:
            print("Generalized highlight xpath is not valid, using non-generalized xpath\n")
            p3 = p1
    
        print("Processed highlight action\n")
    
    print("Generalization Complete\n")
    
    return p3

def getFormMethod(page):

    root = lxml.html.fromstring(page.pagesrc)

    formNode = root.xpath(page.xpath)

    method = formNode[0].get('method')

    if method == None:
	return 'GET'

    return method
	

if __name__ == '__main__':
    
    if len(sys.argv) == 3:
        GeneralizeQueries(sys.argv[1],sys.argv[2])
    elif len(sys.argv) == 2:
	GeneralizeQueries(None, sys.argv[1])	
    else:
        print('To generalize a query\nUSAGE: python QueryGeneralizer.py qrmid queryid\n')
	print('To generalize a query with itself\nUSAGE: python QueryGeneralizer.py queryid\n')

    pass    
    

