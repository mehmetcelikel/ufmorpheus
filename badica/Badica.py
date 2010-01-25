#!/usr/bin/env python

__authors__ = ['"Christan Grant" <cgrant@cise.ufl.edu>']

import ExtractionPath
import Level
import Vertex
import pdb

class Badica(object):
	def __init__(self):
		pass


	def Learn(self, pn):
		"""
		pn is an Array of extraction paths
		"""
		p = pn[0]
		for i in range(1,len(pn)):
			p = self.GenPath(p, pn[i])
		return p


	def GenPath(self, p1=ExtractionPath, p2=ExtractionPath):
		k = min(p1.height(), p2.height())
		# Need to extract 1 because we want to work with indexes
		i, i1, i2 = k-1, p1.height()-1, p2.height()-1
		p = []
		while i >= 0:
			ti = self.GenLevel(p1.level[i1], p2.level[i2])
			p.insert(0, ti)
			i-=1
			i1-=1
			i2-=1

		# Return an Actual extraction path
		e =  ExtractionPath.ExtractionPath()
		e.level = p
		e.H = k
		# New L and R are the size of the greatest R and Ls
		e.L = max(map(lambda x: x.left(), e.level))
		e.R = max(map(lambda x: x.right(), e.level))

		return e


	def GenLevel(self, t1=Level, t2=Level):
		l = min(t1.left(),t2.left())
		r = min(t1.right(),t2.right())
		t = Level.Level() # initial index is at 0 so first insert must be
					 # extraction node
		for i in range(0,r):
			lev = self.GenVertex( t1.getRight(i), t2.getRight(i) )
			t.addRight(lev)
		
		for i in range(1,l):
			lev = self.GenVertex( t1.getLeft(i), t2.getLeft(i) )
			t.addLeft( lev )

		return t 


	def GenVertex(self, v1=Vertex, v2=Vertex):
		v = Vertex.Vertex()
		v.intersect(v1, v2)
		return v


if __name__ == '__main__':
	import ExtractionPath
	#ep1 = ExtractionPath.ExtractionPath(3,7,7,"http://www.gatorzone.com/story.php?id=16113","//html/body/div[3]/div/div/b")
	ep1 = ExtractionPath.ExtractionPath(3,7,7,"http://money.cnn.com/magazines/fortune/bestcompanies/2010/snapshots/8.html","/html/body/div/div/div/div/div")
	print ep1
	print ep1.epath_to_xpath()
	print
	#ep2 = ExtractionPath.ExtractionPath(4,5,5,"http://www.gatorzone.com/story.php?id=16116","//html/body/div[3]/div/div/b")
	ep2 = ExtractionPath.ExtractionPath(4,5,5,"http://money.cnn.com/magazines/fortune/bestcompanies/2010/snapshots/8.html","/html/body/div/div/div/div/div")
	print ep2
	print ep2.epath_to_xpath()
	print
	badica = Badica()
	ep3 = badica.Learn([ep1,ep2])
	print ep3
	print ep3.epath_to_xpath()

