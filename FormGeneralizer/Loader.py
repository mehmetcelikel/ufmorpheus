"""
	The loader is responsible for reading the necessary data out of the database and formatting it for use by the 
	FormGeneralizer
"""
__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

import psycopg2
import sys
import pdb
import lxml
import lxml.html
import Highlight
sys.path.append('../badica')
from ExtractionPath import ExtractionPath
from Qrm import Qrm
from Page import Page
import Highlight
from SsqElement import SsqElement
from Form import Form
from Link import Link
from ActionType import ActionType

def GetQRMFromDb(qrmid):
	
	#Perform retrieval of QRM data
	rowArray = retrieveRecordsFromQRM(qrmid)
	
	return getqrm(rowArray, qrmid, True)

#loads list of inputs for the given pagereference id
def GetPageInputs(pid):
    
        NAME = 3
  
	HILITE = 4

	SSQINPUT = 5	

        inputList = list()

	query = "SELECT * FROM input WHERE pid="+str(pid)

	forminputs = executeQuery(query)

	for r in forminputs:
	
		type = ""

		if r[HILITE] != none:	
			type="Highlight"	
		elif r[SSQINPUT] != none:
			type="Constant"
		else:
			type="Default"
	
		inputList.append(Input.New(r[NAME], type, r[HILITE], r[SSQINPUT]))

        return inputList

def getQueryText(q1, q2):

	s = "SELECT querystring FROM query,qrm WHERE query.queryid = qrm.queryid AND qrm.qrmid = "
	s2 = "SELECT querystring FROM query WHERE query.queryid = "
	r1 = None
	r2 = None

	if q1.isQRMID:
		r1 = executeQuery(s + q1.id)[0]
	else:
		r1 = executeQuery(s2 + q1.id)[0]
	
	if q2.isQRMID:	
		r2 = executeQuery(s + q2.id)[0]
	else:
		r2 = executeQuery(s2 + q2.id)[0]

	return [r1,r2]			
	
#performs insertion of qrm db
def InsertQrm(qrm, code):

        #insert new qrm into database	
        pass

#This function calls the retrieveRecords function and parses them into the proper format
#that can be used by the other generalization code
def GetQRMFromQuery (queryID):

	rowArray = retrieveRecordsFromQuery(queryID)
	
	return getqrm(rowArray, queryID, False)
	
#This function gets page references and page outputs and returns a query object	
def getqrm(rowArray, qid, isQRMID):
	
	pageRows = rowArray[0]
	
	outputRows = rowArray[1]
	
	answerRows = rowArray[2]

	if pageRows == None or len(pageRows) == 0:
		return None
	
	if outputRows == None or len(outputRows) == 0:
		return None
	
	if answerRows == None or len(answerRows) == 0:
		return None

	#List of page refs, highlights, and answers combined
	qrm = getCondensedQrm(pageRows, outputRows, answerRows)
	
	length = len(qrm.pageList)
	
	lastOutputTimestamp = -1
	
	urlHash = dict()
	
	#Iterate over whole sequence of user interaction
	for i in range(0, length-1):

		currentItem = qrm.pageList[i]

                id = currentItem.url+str(currentItem.timestamp)

		urlHash[id] = i

		#This is the time that the previous page was seen in the list, assuming the user saw a page
		#and then went to other pages, then clicked "back" until he returned to the same page.
		timestampOfPreviousPage = qrm.pageList[urlHash[id]].timestamp
	
		#Check if the page has been visited before and if there are no outputs intervening
		if urlHash[id] != None and lastOutputTimestamp < timestampOfPreviousPage and lastOutputTimestamp != -1:
			
			index = urlHash[id]

			qrm.pageList = exciseRange(index, i-1, qrm.pageList)
			
		#Update the last output found
		if currentItem.actionType == ActionType.Highlight:
				
			lastOutputTimestamp = currentItem.timestamp
		
	qrm.id = qid

	qrm.isQRMID = isQRMID
	
	#if this is a true qrm, then get its input/output classes
	if qrm.isQRMID:
		ssqClasses = executeQuery("SELECT io,classid,contextid FROM qrmHas WHERE qrmHas.qrmid = "+str(qid))
	else:
		classQuery = "SELECT io, individual.classid,contextid FROM queryhas, individual, phrase, phrasebelongstocontext WHERE queryhas.queryid="+str(qid)
		classQuery += " AND individual.individualid=queryhas.individualid AND individual.phraseid = phrase.phraseid"
		classQuery += " AND phrase.phraseid = phrasebelongstocontext.phraseid"

		ssqClasses = executeQuery(classQuery)

	qrm.ssqClasses = extractSsqElements(ssqClasses)
		
	return qrm

#get the class data into ssqelement objects and return a list of them
def extractSsqElements(classes):

	IO = 0
	CLASS = 1
	CONTEXT = 2
	elems = list()
	
	for classRow in classes:	
		elems.append( SsqElement( classRow[IO], classRow[CLASS], classRow[CONTEXT] ) )
			
	return elems

def exciseRange(i,j,a):
	
	#Remove items from i to j-1 in the list 'a'
	for k in range(i,j-1):
		a.pop(i)
	
	return a
	
#Combine the pages, highlights and answers into a single list
def getCondensedQrm(pps, outs, ans):

#Need to handle different case of qrmid vs queryid

#Need to assign id's to input phrases, who0 who1 ,where0 what0 etc.

#Need to determine usage location in some form, and then extract the class of the form input
#and assign it to the ssq input

	condensedList = list()
	
	HILITE_HLID = 0
	ANSWER_HLID = 4
	ppsTime = 8
	outsTime = 7

	qrm = Qrm()

	#Keep condensing until everything, including the answers has been condensed
	while len(ans) > 0:

		if len(pps) > 0 and pps[0][ppsTime] < outs[0][outsTime]:
			
			pageRow = pps.pop(0)
			
			p = getPageOrLink(pageRow)
			
			condensedList.append(p)
			
		else:
			outputRow = outs.pop(0)
							
			h = getHighlight(outputRow)
			
			#If the current highlight is an answer then set the flag in the page
			if outputRow[HILITE_HLID] == ans[0][ANSWER_HLID]:
				
				h.isAnswer = True
				
				#Remove the answer 
				ans.pop(0)
		
			condensedList.append(h)

	qrm.pageList = condensedList

	return qrm

#forms a highlight object from the db row
def getHighlight(row):
	beginoffset = 1
	endoffset = 2
	meetpoint = 11
	startxpath = 4
	endxpath = 5
	hpagesrc = 8
	htimestamp = 7
	hUrl = 6

        h = Highlight.New(lower(row[meetpoint]),lower(row[startxpath]),lower(row[endxpath]),row[beginoffset],row[endoffset], row[hUrl], row[hpagesrc], row[htimestamp])

	return h

#Forms a page object out of the 
def getPageOrLink(row):

	pid = 0
	pUrl = 1
	xpath = 7
	ppagesrc = 6
	ptimestamp = 8
	dest = 9
	
	#If its a highlight you need start xpath and end xpath
	#also offsets
		
	lowerRowXpath = lower(row[xpath])

	#Debug: This is a hack, it is temporary until christan changes the scraper to handle the missing '/html/body' part
	if lowerRowXpath.startswith("/html/body") == False:
		lowerRowXpath = "/" + lowerRowXpath

	if row[ppagesrc] != None:
		node = lxml.html.document_fromstring(row[ppagesrc])
	else:
		node = lxml.html.parse(row[url])

	extractedNodes = node.xpath(lowerRowXpath)

	pdb.set_trace()

	if len(extractedNodes) == 0:
		return None

	temp = Page()
		
	if extractedNodes[0].tag == "form":
		f = Form()
		f.xpath = lowerRowXpath
		temp = f
		f.formInputs = GetPageInputs(row[pid])
	else:
		l = Link()
		l.xpath = lowerRowXpath
		temp = l

	temp.url = row[pUrl]
	
	temp.destinationUrl = row[dest]
	
	temp.pagesrc = row[ppagesrc]
	
	temp.timestamp = row[ptimestamp]	
	
	return temp

#Converts the argument to lower case
def lower(x):
	clist = list()
	x = x.strip()
	for i in range(0, len(x)):
	
		if ord(x[i]) >= 65 and ord(x[i]) <= 90:
			clist.append(chr(ord(x[i])+32))
		else:
			clist.append(x[i])
	
	newx = "".join(clist)
	
	newx = newx.replace("#text","text()")
	
	return newx
	
#This takes two pages and combines them into a single page
def combine(r, plist, nindex, plistIndex):

	#Copy all outputs from s into r
	for o in r.outputs:
		plist[nindex].outputs.append(o)
		
	#Copy all links from s into r
	for l in r.links:
		plist[nindex].links.append(l)

        plist.remove(r)

	return plist
	
#Looks for a default value for the given tag, identified by the id. If no value is found then an empty string
#is returned

def getDefaultValue(src,id):
	
	#Get index of id
	index = src.find(id, 0, len(src))
	
	endOfTagIndex = src.find(">",index,len(src))
	
	startOfTagIndex = -1 
	
	for i in range(index, 0):
		
		if i == "<":
			startOfTagIndex = i
			break
	
	if startOfTagIndex == -1:
		return ""
	
	defaultValue = list()
	
	newIndex = -1

	while true:
	
		valueIndex = src.find("value",startOfTagIndex,endOfTagIndex)
		
		if valueIndex == -1:
			return ""
			
		newIndex = valueIndex + 1
		
		if src[newIndex] == "=" or src[newIndex+1] == "=":
			break
		else:
			startOfTagIndex = newIndex
			
				
	while src[newIndex] != "\"":
		newIndex = newIndex + 1
	
	newIndex = newIndex + 1
	
	while src[newIndex] != "\"":
		defaultValue.append(src[newIndex])
		newIndex = newIndex + 1
	
	return "".join(defaultValue)

#This function performs the selection on the database to get the records

def retrieveRecordsFromQRM(qrmid):
	
	return get("qrmid",qrmid)
	
def retrieveRecordsFromQuery(qid):
	
	return get("queryid",str(qid))

def get(field, value):
	
	#Gets the set of PageReferences joined with their inputs and outputs (highlights)
	query = "SELECT * FROM page,pagereference WHERE";
	query = query + " page.pageid = pagereference.pageid"
	query = query + " AND pagereference."+field+"="+value
	query = query + " ORDER BY pagereference.timestamp"
	
	pageReferencesRows = executeQuery(query) 

	query = "SELECT * FROM highlight WHERE"
	query = query + " highlight."+field+"="+value
	query = query + " ORDER BY timestamp"
	
	outputHighlights = executeQuery(query)
	
	query = "SELECT * FROM answer,highlight WHERE"
	query = query + " highlight.answerid = answer.answerid"
	query = query + " AND highlight."+field+"="+value
	
	answers = executeQuery(query)
	
	return [pageReferencesRows,outputHighlights,answers]	

def executeQuery(q):
	
	#Database parameters
	server = "babylon.cise.ufl.edu"
	user = "morpheus3"
	pwd = "crimson03.sql"
	db = "Morpheus3DB"
	
	try:
		#create a connection using the psycopg2 library
		connection = psycopg2.connect("dbname='"+db+"' user='"+user+"' host='"+server+"' password='"+pwd+"'")
	except:
		print("Connection to database failed.")
		return None
	
	#obtain the cursor for use in executing our query
	cursor = connection.cursor()
	
	#Execute query using cursor
	cursor.execute(q)
	
	#Retrieve result set
	result = cursor.fetchall()
	
	return result
		
if __name__ == "main":
	pass
