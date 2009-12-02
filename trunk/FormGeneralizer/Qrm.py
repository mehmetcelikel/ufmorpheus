"""
	Represents a QRM
"""
__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

class Qrm():
	#TODO: refer to paper to get proper definition for this object
	def __init__ (self):
		self.pageList = list()
		self.id = -1
		self.isQRMID = True
		self.dataHash = dict()
		pass

	def pageLength(self):
		return len(self.pageList)
	
	
if __name__ == "main":
	pass
