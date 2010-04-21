"""
	Represents a highlight
"""

__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']
import sys
from lxml import etree
from ActionType import ActionType
from Page import Page
import pdb
class Highlight(Page,object):

	def __init__ (self):
		super(Highlight,self).__init__()	
		self.startXpath = ""
		self.endXpath = ""
		self.meetpoint = ""
		self.startOffset = 0
		self.endOffset = 0
		self.classId = -1;
		self.isAnswer = False
		self.actionType = ActionType.Highlight
		pass

def New(meet, startxpath, endxpath, startos, endos, url, pagesrc, timestamp, classId):
	h = Highlight()
	h.startXpath = startxpath
	h.endXpath = endxpath
	h.meetpoint = meet
	h.startOffset = startos
	h.endOffset = endos
	h.pagesrc = pagesrc
	h.timestamp = timestamp
	h.url = url
	h.isAnswer = False
	h.classId = classId
	h.actionType = ActionType.Highlight
	return h

def getSelectionFromTimestamp(timestamp, page):
	
	q = "SELECT * FROM Highlight WHERE timestamp = %d" % timestamp
	
	return runHighlightQuery(q,page)
	
	
def getSelection(highlightid, page):

	q = """SELECT * FROM Highlight WHERE highlightid = %d""" % highlightid

	return runHighlightQuery(q)

def runHighlightQuery(q, page):
	
	import Loader

	results = Loader.executeQuery(q)

	if len(results) == 0:
		print('No results found')
		return

	#form a highlight out of the row
	h = Loader.getHighlight(results[0])

	return getHighlightText(h, page)

def removeTBodies(xpath):

        parts = xpath.split("/")
        plist = list()

        for p in parts:
                if p.startswith("tbody"):
                        continue

                plist.append(p)

        return "/".join(plist)

def getHighlightText(h, page):

	#this list keeps all the extracted text so far
	textList = list()

	#fix xpaths first
	h.startXpath = removeTBodies(h.startXpath)
	h.endXpath = removeTBodies(h.endXpath)

	#get the start and end nodes in the dom
	start = page.xpath(h.startXpath)[0]
	end = page.xpath(h.endXpath)[0]

	#before we call the helper, we need to remove any text() functions
	sxpath = ''
	expath = ''
	i = h.startXpath.find('/text()')
	if i >= 0:
		sxpath = h.startXpath[:i]
	else: sxpath = h.startXpath
	
	i = h.endXpath.find('/text()')
	if i >= 0:
		expath = h.endXpath[:i]
	else: expath = h.endXpath

	#get the tree for the html object
	tree = page.getroottree()

	#parses the dom to extract the highlighted text
	getSelectionHelper(tree, page, textList, tree.xpath(sxpath)[0], tree.xpath(expath)[0], h.startOffset, h.endOffset)

	str = ''.join(textList)
	str = str.replace("\r\n"," ")
	str = str.replace("\n"," ")
	str = str.replace("\t"," ")
	return str.strip()

def getSelectionHelper(tree, node, textList, startNode, endNode, startIndex, endIndex):

	startText = node.text_content()[startIndex:]
	endText = node.text_content()[:endIndex]
	path = tree.getpath(node)

	#if we have just found the beginning
	if startNode == node and startNode == endNode and len(textList) == 0:
		textList.append( node.text_content()[startIndex:endIndex] )
		return True

	elif startNode == node and len(textList) == 0:
		textList.append(startText)

	#if we have found the end, because the highlight direction was reversed
	elif endNode == node and len(textList) == 0:
		textList.append( node.text_content()[endIndex:] )

	#if we have found the start because the direction was reversed
	elif startNode == node and len(textList) > 0:
		if startNode != endNode:
			textList.append( node.text_content()[:startIndex] )
		return True
	elif endNode ==  node and len(textList) > 0:
		if startNode != endNode:
			textList.append(endText)
		return True
			
	#iterate over all the children for this node (we don't know how many there are)
	for n in node.getchildren():

		done = getSelectionHelper(tree, n, textList, startNode, endNode, startIndex, endIndex)					

		if done:
			return done
	
	return False
	
		
if __name__ == "__main__":	
	pass
	
