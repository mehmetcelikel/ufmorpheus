"""
	An input or output of a qrm
"""
import pdb
class SsqElement():

	def __init__ (self):
		
		self.type=""
		self.classid = -1	
		self.contextid = -1
		
	def __init__ (self, inOrOut, classid, contextid):
		
		self.type = inOrOut
		self.contextid = contextid
		self.classid = classid

	#if the ssq passed in does not have the same values for all SsqElement members, then return false otherwise true
	def compare(self, ssq):
		if ssq.type != self.type or ssq.contextid != self.contextid or ssq.classid != self.classid:
			return False

		return True
	
if __name__ == "main":
	pass
		
