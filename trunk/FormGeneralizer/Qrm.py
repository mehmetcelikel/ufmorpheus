"""
	Represents a QRM
"""
__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']
import pdb
class Qrm():
	#TODO: refer to paper to get proper definition for this object
	def __init__ (self):
		self.pageList = list()
		self.id = -1
		self.isQRMID = True
		self.dataHash = dict()
		self.ssqClasses = list()
		
		pass

	def pageLength(self):
		return len(self.pageList)

	foundMatch = False

	#check all the ssq elements in this qrm against those of the other qrm, if they all match. Then return true, otherwise return false
	def matchSsqClasses(self, comparedQrm):	
		for ssqElem in comparedQrm.ssqClasses:

			foundMatch = False

			for thisQrmSsqElem in self.ssqClasses:	
				if thisQrmSsqElem.compare(ssqElem):
					foundMatch = True
					break
	
			if foundMatch == False:
				return False

		return True		

if __name__ == "main":
	pass
