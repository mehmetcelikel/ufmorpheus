"""
	Represents a form input
"""

__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

class Input():

	def __init__ (self):

		self.name = ""
		self.type = ""
		self.highlightid = -1
		self.individualid = -1
		pass
	
def New(name, type, highlightid, individualid):
	input = Input()
	input.name =name
	input.type = type
	input.highlightid = highlightid
	input.individualid = individualid
	return input
		
if __name__ == "main":
	pass

