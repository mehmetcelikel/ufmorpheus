"""
	Represents a single page references with its associated form inputs and any resulting outputs
"""
__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

class Page():
	def __init__ (self):

		self.url = ""
		self.destinationUrl = ""
		self.timestamp = 0
		self.outputs = list()
		self.xpath = ""
		self.links = list()
		self.pagesrc = ""
		self.isFormSubmit = False
		
		pass

	#Parseform accepts a string and parses it into url, inputs, and outputs#
	"""def parseForm(self,form):
	
		idIndex = 2#Index of id in input tuple
		
		inputIndex = 3
		
		urlIndex = 0
		
		postUrlIndex = 7
		
		queryStringIndex = 2
		
		#get URL
		self.url = form[urlIndex]

		#get post url
		self.destinationUrl = form[postUrlIndex]
		
		#get querystring
		self.queryString = form[queryStringIndex]
		
		#get inputs
		inputs = subparts[inputIndex].split('|')
		
		for i in inputs:
			inputParts = i.split(',')
			self.inputs[inputParts[id]] = inputParts;
		
		#get outputs
		outputs = subparts[inputIndex].split('|')
		
		for o in outputs:
			outputParts = o.split(',')
			self.outputs[outputParts[id]] = outputParts
		
		pass
	"""
	
	def ToString(self):
		
		#Build a string of all the inputs, outputs and the url
		str = list()
		
		str.append(self.url)
		
		inlist = list()
		
		for i in self.inputs:
			inlist.append(','.join(self.inputs))
		
		str.append('|'.join(inlist))
		
		outlist = list()
		
		for o in self.outputs:
			outlist.append(','.join(self.inputs))
		
		str.append('|'.join(outlist))
		
		str = "\n".join(str)
		
		return str
		
if __name__ == "main":
	pass
	
