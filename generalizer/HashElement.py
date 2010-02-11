"""
	This class holds the data associated with a single hash element
"""

class HashElement:
		
	def __init__(self,value,type,context,dataclass):
		self.value = value
		self.type = type
		self.context = context
		self.dataclass = dataclass
			
		pass

	def hash(self):	
		return self.__hash__()

if __name__ == "__main__":
	pass

		
