"""
	Builds the QRE script and returns it 
"""

__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

from lxml import etree
import pdb
import lxml.html
from ActionType import ActionType
class ScriptBuilder():
	
	code = ""

	#accepts a qrm and generates the qre script from it
	def create(self,qrm):
	
		previousAction = None

		#create root of document
		qre = etree.Element( 'qre' )		

		#add user data
		userData = etree.SubElement(qre, 'userdata')
		writeUserData(userData, qrm)	

		#add startUrl
		urlNode = etree.SubElement(qre, 'starturl')
		writeStartUrl(urlNode, qrm)
	
		#add sequence section
		sequence = etree.SubElement(qre, 'sequence')

		#keep track of order of operations		
		sequenceNumber = 0

		actionHash = dict()

		#write out ssq data
		#TODO: need to determine if appropriate info is in qrm or not
	
		#for the current qrm, we need  the data section
		for action in qrm.pageList:
			
			#user data section

			#hash table and ssq input section

			if previousAction != None and linkBetweenPages(previousAction, action) == False:
				writeUrlAction(sequence, action.url,sequenceNumber)
			
			#write out actions
			if action.actionType == ActionType.Form:
				writeFormAction(sequence, action,sequenceNumber)
			elif action.actionType == ActionType.Link:
				writeLinkAction(sequence, action, sequenceNumber)
			elif action.actionType == ActionType.Highlight:
				writeHighlightAction(sequence, action, sequenceNumber, actionHash)

			#save previous action for use in next iteration
			previousAction = action
			

		#add action data - now that we have built the hashtable of highlight and ssq info
		actionData = etree.SubElement(qre, 'actiondata')
		
		writeActionData(actionData,actionHash)
		
		qre = etree.tostring(qre, pretty_print=False)

		return qre
	
def writeActionData(xmlNode, actionHash):

	#attach the action data to the xml node
	for k in actionHash.keys():
		info = etree.SubElement(xmlNode, 'info')
		info.set('key',k)
		info.set('value',actionHash[k])
		xmlNode.append(info)

	pass	

	
def writeStartUrl(xmlNode, qrm):

	#get url of first page, and put it here
	url = qrm.pageList[0].url

	xmlNode.text = url
	pass

def writeUserData(xmlNode, qrm):
		
	#need to add user data to qrm, then write it out here
				
	pass
		
def writeUrlAction(xmlNode, url, sequenceNumber):

	urlnode = etree.SubElement(xmlNode, 'constantlink')

	urlnode.set("number",str(sequenceNumber))

	urlnode.text = url	
		
	xmlNode.append(urlnode)

	pass
	
def writeFormAction(xmlNode, action, sequenceNumber):

	#need to handle default input values

	form = etree.SubElement(xmlNode, 'form')
	form.set("number",str(sequenceNumber))
		
	#handle the xpath for this form
	xpath = etree.SubElement(form, 'xpath')
	xpath.text = action.xpath

	#handle the url for this form
	url = etree.SubElement(form, 'url')
	url.text = action.url

	#handle form method
	method = etree.SubElement(form, 'method')
	method.set("type",action.method)

	#obtain the form element from the page, we need it get info about its inputs
	formElemList = lxml.html.fromstring(action.pagesrc).xpath(action.xpath)
	
	#parameters
	for i in action.formInputs:

		value = ""

		foundIt = findInList(formElemList[0].inputs.keys(), i.name)
		
		input = None
	
		#if input is not found, then its probably either a querystring name value pair, or some other input
		if foundIt == False:
			#so now we need to parse the querystring for the current page
			value = parseQueryString(action.querystring, i.name)
		else:
			value = formElemList[0].inputs[i.name].value
			input = formElemList[0].inputs[i.name]

		if foundIt == True and input.type == 'button':
			continue

		param = etree.SubElement(form, 'param')
		param.set("name",i.name)
		param.set("type",i.type)
		
		if i.highlightid != None and i.highlightid != -1:
			param.text = "hl" + str(i.highlightid)
		elif i.individualid != None and i.individualid != -1:
			param.text = "in" + str(i.individualid)
		else:
			param.text = value
	
	pass

#for now this won't work because parsing of the querystring isn't working in the scraper or dobson properly
def parseQueryString(querystring, input):

	#find the input in the querystring and return its value
		
	return ''


def findInList(ls, i):

	for e in ls:
		if e == i:
			return True
	return False

def getDefaultValue(form, input):

	#in this situation, we need to parse the form and obtain the value for the given input. 

	formElemList = lxml.html.fromstring(form.pagesrc).xpath(form.xpath)

	inputElem = formElemList[0].inputs[input.name]

	return inputElem.value

	
def writeLinkAction(xmlNode, action, sequenceNumber):
	
	link = etree.SubElement(xmlNode, 'link')
	link.text = action.destinationUrl
 	xmlNode.set("number",str(sequenceNumber))
	pass

def writeHighlightAction(xmlNode, action, sequenceNumber, actionHash):

	id = str(action.timestamp.__hash__())

	#need to handle actiondata 

	hilite = etree.SubElement(xmlNode, 'highlight')
	hilite.text = action.meetpoint
	hilite.set("id",id)
	xmlNode.set("number",str(sequenceNumber))

	#add data to actionHash
	actionHash[id] = action.meetpoint		

	pass

def linkBetweenPages(p1, p2):

	if p1 == None or p2 == None: 
		return False

	import urllib

	url = urllib.quote(p2.url)

	url = url.replace("/","%2F")

	if hasattr(p1,'desintationUrl') and p1.destinationUrl.find(url) >= 0:
		return True

	if p1.actionType == ActionType.Form:
		return True

	return False;


if __name__ == "main":
	pass
