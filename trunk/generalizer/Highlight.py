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
		self.isAnswer = False
		self.actionType = ActionType.Highlight
		pass
	
def New(mx, sx, ex, so, eo, url, pagesrc, timestamp):
	h = Highlight()
	h.startXpath = sx
	h.endXpath = ex
	h.meetpoint = mx
	h.startOffset = so
	h.endOffset = eo
	h.pagesrc = pagesrc
	h.timestamp = timestamp
	h.url = url
	h.isAnswer = False
	h.actionType = ActionType.Highlight
	return h
		
if __name__ == "main":
	pass
	
