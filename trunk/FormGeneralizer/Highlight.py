"""
	Represents a highlight
"""

__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

class Highlight():

	def __init__ (self):
	
		self.startXpath = ""
		self.endXpath = ""
		self.meetpoint = ""
		self.startOffset = 0
		self.endOffset = 0
		pass
	
	def __init__ (self, mx, sx, ex, so, eo):
		
		self.startXpath = sx
		self.endXpath = ex
		self.meetpoint = mx
		self.startOffset = so
		self.endOffset = eo
		pass
		
if __name__ == "main":
	pass
	