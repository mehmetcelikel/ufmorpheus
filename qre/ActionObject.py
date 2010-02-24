"""
	This class implements the action objects for all actions
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import lxml
from lxml import etree
import re
import urllib, urllib2
import pdb

class ActionObject:
	"""Base Class of all actions"""

	def do(self, state): abstract

def abstract():
    import inspect
    caller = inspect.getouterframes(inspect.currentframe())[1][3]
    raise NotImplementedError(caller + ' must be implemented in subclass')


class URLAction(ActionObject):

	def __init__(self,_xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does URLAction and returns the state"""

		url = self.xmlnode.text.strip() # Extract the url from <starturl>URL</starturl>
		request = urllib2.Request(url)
		response = urllib2.urlopen(request)
		# state.cookie = CookieJar() -- initialized in state
		state.cookie.extract_cookies(response,request)
		cookie_handler = urllib2.HTTPCookieProcessor( state.cookie )
		redirect_handler = urllib2.HTTPRedirectHandler()
		opener = urllib2.build_opener(redirect_handler,cookie_handler)
		state.page = opener.open(request)


class LinkAction(ActionObject):


	def __init__(self,_xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does LinkAction and returns the state"""
                url = self.xmlnode.text.strip()
                request = urllib2.Request(url)
                response = urllib2.urlopen(request)
                # state.cookie = CookieJar() -- initialized in state
                state.cookie.extract_cookies(response,request)
                cookie_handler = urllib2.HTTPCookieProcessor( state.cookie )
                redirect_handler = urllib2.HTTPRedirectHandler()
                opener = urllib2.build_opener(redirect_handler,cookie_handler)
                state.page = opener.open(request)

		pass


class HighlightAction(ActionObject):


	def __init__(self, _xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does the HighlightAction and returns the state"""
		
		pdb.set_trace()

		#get the page
		page = lxml.html.fromstring(state.page)
	
		#get xpath	
		xpath = self.xmlnode.text

		#extract the page snippet
		htmlSnippet = page.xpath(xpath)

		#get key and insert html into data hash
		key = self.xmlnode['key']

		state.kv_hash[key] = htmlSnippet
		
		pass


class FormAction(ActionObject):

	def __init__(self, _xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):

		#start with the list of inputs for this form
		inputs = list()

		querystring = ""
		
		#get the base url for this form
		for e in self.xmlnode.getchildren():
			if e.tag == 'url':
				querystring = e.text.strip()	
				break

		querystring += "?"
		first = False
		
		#iterate through the list and find the input elements		
		for e in self.xmlnode.getchildren():

			#for each input get the value from the actiondata hash
			if e.tag == 'param':
				if first == False:
					querystring += "&"
	
				v = state.kv_hash[ e.text ]
					
				querystring += e.get('name').strip() + "=" + v.strip()

		pdb.set_trace()
		querystring = urllib2.quote(querystring)
		#perform form submission 
		result = urllib2.urlopen( querystring )		

		print(result)
		pass
