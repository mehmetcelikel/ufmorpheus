#!/usr/bin/env python

"""
Level is a class to hold all of the vertexes of one level
"""

__authors__ = ['"Christan Grant" <cgrant@cise.ufl.edu>']

import Vertex
import pdb

class Level:
	'''
	Extraction Level
	'''
	def __init__(self):
		self.vertex = []
		self.index = -1	# index of the extraction node in the vertex array
		#if extraction_node != None:
		#	self.vertex.append(extraction_node)
		#	self.index = 0

	def fromstring(self, level_string=""):
		"""
		Create a Level based on a string
		"""
		dict = None
		dict = eval(str(level_string))
	
		self.index = int(dict[0])

		for i in dict[1]:
			v = Vertex.Vertex()
			v.fromstring(str(i))
			self.vertex.append(v)

	def __str__(self):
		return "[ '"+str(self.index)+"', "+str(map(str,self.vertex)).replace('"','')+']'

	def addLeft(self, item=Vertex):
		"""
		Add an element to the left of the index
		"""
		self.index += 1
		self.vertex.insert(0, item)
		
	def addRight(self, item=Vertex):
		"""
		Add an element to the right of the index
		"""
		self.vertex.append(item)
		if(len(self.vertex) == 1):
			self.index = 0 #why?

	def getRight(self, rindex):
		return self.vertex[self.index+rindex]

	def getLeft(self, lindex):
		return self.vertex[self.index-lindex]

	def set_index(self, i):
		self.index = i

	def size(self):
		return len(self.vertex)

	def right(self):
		"""
		Returns the amount of right elements
		"""
		return self.size() - self.index 

	def left(self):
		"""
		Returns the amount of left elements
		"""
		return self.index

if __name__ == '__main__':
	#v = Vertex.Vertex('div')
	l = Level()
	#l = Level(Vertex.Vertex('div'))
	l.addRight(Vertex.Vertex('a'))
	l.addRight(Vertex.Vertex('a'))
	l.addRight(Vertex.Vertex('l'))

	l.addLeft(Vertex.Vertex('a'))
	l.addLeft(Vertex.Vertex('b'))
	l.addLeft(Vertex.Vertex('b'))
	l.addLeft(Vertex.Vertex('a'))
	l.addLeft(Vertex.Vertex('a'))
	l.addLeft(Vertex.Vertex('f'))

	print l

	l1 = Level()
	l1.fromstring(str(l))
	print l1

