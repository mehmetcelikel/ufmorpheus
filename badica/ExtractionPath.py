#!/usr/bin/env python
"""
	This class allows the creation of an extraction path that may be used to 
	work with the batica algorithm.
	
"""
__authors__ = ['"Christan Grant" <cgrant@cise.ufl.edu>']

import Level
import Vertex
import pdb

class ExtractionPath(object):
	def __init__ (self,H=7, L=3, R=3, html=None, xpath=None):
		self.level = [] # Array of Levels
		self.H = H
		self.L = L
		self.R = R
		if xpath != None and xpath != None:
			self.xpath_to_epath(html,xpath)
	
	def fromstring(self, epath=None):
		"""
		Load an epath given a string
		"""
		lines = epath.split('\n')
		d = eval(lines[0]) # example {'H': 5, 'L': '5', 'R': 6}
		self.H = d['H']
		self.L = d['L']
		self.R = d['R']

		for i in lines[1:]:
			l = Level.Level()
			l.fromstring(str(i))
			self.level.append(l)

	def __str__(self):
		ret = "{'H': "+ str(self.H)+", 'L': "+str(self.L)+", 'R': "+str(self.R)+'}\n'
		for i in map(str,self.level):
			ret += i
			ret += '\n'
		return ret.rstrip()

	def height(self):
		return len(self.level)

	def left(self):
		"""
		Returns the value of the greatest value of the left of the extraction 
		nodes in the level array
		"""
		return reduce(lambda x, y: max(x.left(), y.left()), self.level)

	def right(self):
		"""
		Returns the value of the greatest value of the right of the extraction
		nodes in the level array
		"""
		return reduce(lambda x, y: max(x.right(), y.right()), self.level)

	def level(self, i=0):
		return self.level[i] # of type Level


	def epath_to_xpath(self):
		"""
		Make the path into an xpath.  Returns the xpath representation
		"""
		xp = "/"
		k = self.height()
		#for i in range(k):
		krange = range(k)
		krange.reverse()
		for i in krange:
			t = self.level[i]
			# need to subtract 1 from t.right() because we want the index
			# and t.right() returns the size
			xp += "/*" + self.cond(t.getRight(t.right()-1))
			rrange = range(t.right()-1)
			rrange.reverse()
			for j in rrange:
				xp += "/preceding-sibling::*[1]" + self.cond(t.getRight(j))
	
			if t.left() > 0:
				xp += "[preceding-sibling::*[1]" + self.cond(t.getLeft(1))
				lrange = range(2,t.left()+1)
				for j in lrange:
					xp += "/preceding-sibling::*[1]" + self.cond(t.getLeft(j))
				xp += "]"
		return xp

	def cond(self, v=Vertex):
		xc = ""
		if v.hasTag():
			xc += "[local-name()='"+str(v.getTag())+"']"
		if 'f' in v.label:
			xc += "[not (preceding-sibling::*)]"
		if 'l' in v.label:
			xc += "[not (following-sibling::*)]"
		return xc
	
	def xpath_to_epath(self, url_or_html='', path=''):
		"""
		Creates the extraction path given an xpath and the html source as a 
		string.
		url_or_html - should be wither a full url or the HTML src
		This method returns itself
		"""
		import copy
		import lxml
		import lxml.html
		#import lxml.etree
		from lxml.html.ElementSoup import parse
		import urllib2

		if url_or_html.startswith("http://") or url_or_html.startswith("www")
				or url_or_html.lower().startswith("c:"): 
			root = lxml.html.parse(url_or_html).getroot()
			node = root.xpath(path)
		else:
			root = lxml.html.fromstring(url_or_html)
			node = root.xpath(path)
		
		height = 0
		if node == []:
			runner = None
		else:
			runner = node[0]
		
		while height < self.H and runner != None:
			v = "" # just let the object exist
			while (hasattr(runner,'is_text') or hasattr(runner,'is_tail')) and (runner.is_text or runner.is_tail):
				tmp = copy.deepcopy(runner.getparent())
				del runner
				runner = tmp
		
			v = Vertex.Vertex(runner.tag)
			#else:
			#	v = Vertex.Vertex(runner.getparent().tag)
			# check following siblings
			if runner.getnext() == None:
				v.addLabel('l')
			# check preceding siblings
			if runner.getprevious() == None:
				v.addLabel('f')
			# Add this extraction node to the current level
			lev = Level.Level()
			lev.addRight(v)
			
			# iterate following siblings
			# add them to lev
			# the last vertex added needs the label 'l' for last
			# l is only there if it is actually first
			r = 1
			rrunner = runner.getnext()
			while rrunner != None and r < self.R:
				rv = Vertex.Vertex(rrunner.tag)
				rrunner = rrunner.getnext()
				if rrunner == None:
					rv.addLabel('l')
				r += 1
				lev.addRight(rv)
			
			# iterate preceding siblings
			# add them to lev
			# the last vertex added needs the label 'f' because it is
			# the first label foing left
			# the f is only there if it is actually the first
			l = 1
			lrunner = runner.getprevious()
			while lrunner != None and l < self.L:
				lv = Vertex.Vertex(lrunner.tag)
				lrunner = lrunner.getprevious()
				if lrunner == None:
					lv.addLabel('f')
				l += 1
				lev.addLeft(lv)

			self.level.append(lev)
			height += 1
			runner = runner.getparent()

		return self


if __name__ == '__main__':
	pdb.set_trace()
	ep = ExtractionPath(2,1,1,'http://www.ticketstub.com/search.php?q=marlins',"//*[local-name()='li']/*[not (following-sibling::*)]")
	#ep = ExtractionPath(3,7,7,'http://www.gatorzone.com/story.php?id=16113','//body/div[3]/div/div/b')
	#ep = ExtractionPath(3,7,7,"http://www.gatorzone.com/story.php?id=16113","//body/div[3]")
	print ep.epath_to_xpath()

	#ep1 = ExtractionPath()
	print ep

