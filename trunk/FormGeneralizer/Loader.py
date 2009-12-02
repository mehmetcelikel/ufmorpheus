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
sys.path.append('../badica')
from ExtractionPath import ExtractionPath
from Qrm import Qrm
from Page import Page
from Highlight import Highlight
def GetQRMFromDb(qrmid):
	
	#Perform retrieval of QRM data
	rowArray = retrieveRecordsFromQRM(qrmid)
	
	return getqrm(rowArray, qrmid, True)

#loads list of inputs for the given pagereference id
def GetPageInputs(pid):
    
        NAME = 3
  
        TYPE = 4

        query = "SELECT * FROM input WHERE pagerefid = "+pid

        inputrows = executeQuery(query)

        inputList = list()

        #TODO: Implement determination of constant, default or highlight
	
	#get all highlights for comparison of strings
	highlights = getHighlights(qrm)

	#get query input texts 
	querys = getQueryText(q1,q2)	
		
	#iterate over all inputs and check highlights and query for matches, 
	#if a highlight matches then assign an id and record the highlight in question
	#if a part of the quer matches, then record that
	#if nothing is found, then record the value as-is and assume its a default
        for r in rows:
      	      

	      inputList.append([r[NAME],r[TYPE],r[val]])

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

		currentItem = qrm.condensedList[i]

                id = currentItem.url+str(currentItem.timestamp)

		urlHash[id] = i

		#Check if subsequent pages are really the same page
		#if i < length - 1 and currentItem.url == qrm.condensedList[i+1].url:
		#	qrm.condensedList = combine(currentItem, qrm.condensedList, i+1, urlHash[id])
                #        length = len(qrm.condensedList)

		#This is the time that the previous page was seen in the list, assuming the user saw a page
		#and then went to other pages, then clicked "back" until he returned to the same page.
		timestampOfPreviousPage = qrm.condensedList[urlHash[id]].timestamp
	
		#Check if the page has been visited before and if there are no outputs intervening
		if urlHash[id] != None and lastOutputTimestamp < timestampOfPreviousPage and lastOutputTimestamp != -1:
			
			index = urlHash[id]

			qrm.condensedList = exciseRange(index, i-1, qrm.condensedList)
			
		#Update the last output found
		if len(currentItem.outputs) > 0:
				
			lastOutputTimestamp = currentItem.timestamp
		
	qrm.id = qid

	qrm.isQRMID = isQRMID

	#need to get phrases from db for query that will be stored in the data has for this qrm
	return q

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
	
	query = ""
	"""
	#get query phrase words - Gets the Phrase and the Context for the given query
	if isQRMID:
		query = "select class.name,contextname from class, context, qrmhas where qrmhas.qrmid = "+qid
	        query += " and qrmhas.classid = class.classid and qrmhas.contextid = context.contextid"
	        query += " ORDER BY contextname"
	else:
		query = "select phrasestring,contextname from phrase, individual, queryhas, phrasebelongstocontext,context where queryhas.queryid = "+qid
		query += " and queryhas.individualid = individual.individualid and individual.phraseid = phrase.phraseid 
		query += " and phrasebelongstocontext.phraseid = phrase.phraseid and phrasebelongstocontext.contextid = context.contextid
		query += " ORDER BY contextname"

	#run query
	phrases = executeQuery(query)
	"""
	
	#Keep condensing until everything, including the answers has been condensed
	while len(ans) > 0:

		if len(pps) > 0 and pps[0][ppsTime] < outs[0][outsTime]:
			
			pageRow = pps.pop(0)
			
			p = getPage(pageRow,False)
			
			condensedList.append(p)
			
		else:
			outputRow = outs.pop(0)
							
			p = getPage(outputRow,True)
			
			#Store the highlight in the dataHash so that we may retrieve it later when determining reusage of highlighted data
			#qrm.dataHash[p.outputs[0].timestamp] = p.outputs[0]

			#If the current highlight is an answer then set the flag in the page
			if outputRow[HILITE_HLID] == ans[0][ANSWER_HLID]:
				
				p.isAnswer = True
				
				#Remove the answer 
				ans.pop(0)
		
			condensedList.append(p)

	qrm.pageList = condensedList

	return qrm

#Forms a page object out of the 
def getPage(row,isout):

	pUrl = 1
	hUrl = 6
	xpath = 7
	ppagesrc = 6
	hpagesrc = 8
	htimestamp = 7
	ptimestamp = 8
	dest = 9
	beginoffset = 1
	endoffset = 2
	meetpoint = 11
	startxpath = 4
	endxpath = 5
	
	#If its a highlight you need start xpath and end xpath
	#also offsets
	p = Page()
		
	if isout == True:
		h = Highlight(lower(row[meetpoint]),lower(row[startxpath]),lower(row[endxpath]),row[beginoffset],row[endoffset])
		
		p.outputs.append(h)
		
		p.url = row[hUrl]
		
		p.pagesrc = row[hpagesrc]
		
		p.timestamp = row[htimestamp]	
		
	else:
		lowerRowXpath = lower(row[xpath])
		
		if row[ppagesrc] != None:
			node = lxml.html.document_fromstring(row[ppagesrc])
		else:
			node = lxml.html.parse(row[url])

		extractedNodes = node.xpath(lowerRowXpath)

		if len(extractedNodes) == 0:
			return None
		
		if extractedNodes[0].tag == "form":
			p.xpath = lowerRowXpath
			p.isFormSubmit = True
		else:
			p.links.append(lowerRowXpath)
			p.isFormSubmit = False
		
		p.url = row[pUrl]
		
		p.destinationUrl = row[dest]
		
		p.pagesrc = row[ppagesrc]
		
		p.timestamp = row[ptimestamp]	
	
	return p

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
	
	return get("qrmid = "+qrmid)
	
def retrieveRecordsFromQuery(qid):
	
	return get("queryid = "+str(qid))

def get(cond):
	
	#Gets the set of PageReferences joined with their inputs and outputs (highlights)
	query = "SELECT * FROM page,pagereference WHERE";
	query = query + " page.pageid = pagereference.pageid"
	query = query + " AND pagereference."+cond
	query = query + " ORDER BY pagereference.timestamp"
	
	pageReferencesRows = executeQuery(query) 

	query = "SELECT * FROM highlight WHERE"
	query = query + " highlight."+cond
	query = query + " ORDER BY timestamp"
	
	outputHighlights = executeQuery(query)
	
	query = "SELECT * FROM answer,highlight WHERE"
	query = query + " highlight.answerid = answer.answerid"
	query = query + " AND highlight."+cond
	
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
