"""
	Represents a single page references with its associated form inputs and any resulting outputs
"""
__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

class Page(object):

	def __init__(self):
		self.url = ""
		self.destinationUrl = ""
		self.timestamp = 0
		self.xpath = ""
		self.pagesrc = ""
		self.actionType = ""
		pass
		
def New(self, url, dest, time, xpath, src):
	p = Page()
        p.url = url
        p.destinationUrl = dest
        p.timestamp = time
        p.xpath = xpath
        p.pagesrc = src
	p.actionType = ""
	return p
		
if __name__ == "main":
	pass
	
