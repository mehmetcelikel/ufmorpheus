#!/usr/bin/env python

"""
The Vertex represents the actual extractionnode
"""

__authors__ = ['"Christan Grant" <cgrant@cise.ufl.edu>']

import pdb

class Vertex(object):
	"""
	Vertex in a  Extraction Path tree
	"""
	def __init__(self,tag=None):
		self.label = set()
		if tag != None:
			self.label.add(tag)

	def fromstring(self, vertex_string):
		"""
		Creates the Vertex object based on a string
		i.e. "['f','l','a']
		"""
		self.label = set()
		v = eval(vertex_string)
		
		if v is 0:
			v = []	
		
		for i in v:
			self.label.add(i)

	def __str__(self):
		return str(map(str,self.label))

	def addLabel(self, l):
		self.label.add(l)

	def discardLabel(self, l):
		self.label.discard(l)
	
	def hasTag(self):
		"""
		Returns true if a tag besides 'l' and 'f' exists
		"""
		if len(self.label.difference('l','f')) > 0:
			return True
		else:
			return False

	def getTag(self):
		"""
		Returns the tag which is not 'l' of 'f'
		This only assumes there is only one tag besides 'l' or 'f'
		"""
		return self.label.difference('l','f').copy().pop()
 
	def intersect(self, v1=None, v2=None):
		"""
		This sets itself to the intersection of the two parameters
		"""
		self.label = v1.label & v2.label # intersection on the labels
		return self


if __name__ == '__main__':
	v = Vertex('f')
	v.addLabel('l')
	v.addLabel('div')
	print v

	v1 = Vertex()
	v1.fromstring(str(v))
	print v1
	
