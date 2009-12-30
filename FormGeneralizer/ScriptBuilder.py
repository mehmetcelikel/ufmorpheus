"""
	Builds the QRE script and returns it 
"""

__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

from lxml import etree
import pdb
from ActionType import ActionType
class ScriptBuilder():
	
	code = ""

	#accepts a qrm and generates the qre script from it
	def create(self,qrm):
	
		previousAction = None

		#create root of document
		qre = etree.Element( 'qre' )		
		#qre = etree.ElementTree(document)

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

		return qre
	
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

	#parameters
	for i in action.formInputs:
		param = etree.SubElement(form, 'param')
		param.set("name",i.name)
		param.set("type",i.type)
		if i.highlightid != None:
			param.text = "hl" + i.highlightid
		elif i.individualid != None:
			param.text = "in" + i.individualid
		else:
			param.text = i.defaultValue
	
	pass

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
