"""
	Represents a link click action on a web page
"""
from ActionType import ActionType
from Page import Page

class Link(Page,object):

	def __init__(self):
		super(Link, self).__init__()		
		self.actionType = ActionType.Link
		pass

def New(url, destinationUrl, xpath, pagesrc, timestamp):
	#call parent constructor
	l = Link()
	l.url = url
	l.destinationUrl = destinationUrl
	l.xpath = xpath
	l.pagesrc = pagesrc
	l.timestamp = timestamp
	l.actionType = ActionType.Link
	return l
	pass

if __name__ == "main":
	pass
				

		
