"""
	Represents a highlight
"""

__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']
from ActionType import ActionType
from Page import Page
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
		
if __name__ == "main":
	pass
	
