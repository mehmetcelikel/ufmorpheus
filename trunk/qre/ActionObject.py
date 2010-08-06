"""
	This class implements the action objects for all actions
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import lxml
from lxml.html import parse, make_links_absolute, fromstring
from lxml import etree
import re
import urllib, urllib2
import pdb
import sys
sys.path.append("../generalizer")
import Loader
import Highlight
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
		state.page = parse(url).getroot()

		"""
		url = self.xmlnode.text.strip() # Extract the url from <starturl>URL</starturl>
		request = urllib2.Request(url)
		response = urllib2.urlopen(request)
		# state.cookie = CookieJar() -- initialized in state
		state.cookie.extract_cookies(response,request)
		cookie_handler = urllib2.HTTPCookieProcessor( state.cookie )
		redirect_handler = urllib2.HTTPRedirectHandler()
		opener = urllib2.build_opener(redirect_handler,cookie_handler)
		state.page = opener.open(request)
		"""

class LinkAction(ActionObject):


	def __init__(self,_xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does LinkAction and returns the state"""
                xpath = self.xmlnode.text.strip()
	
		#for some reason, tbody tags annoy lxml, must remove them
		xpath = removeTBodies(xpath)
		
		doc = fix_links(state, self.xmlnode.get('number'), etree.tostring(state.page),state.page.base_url)

		page = fromstring(doc)
	
		link_node = page.xpath(xpath)[0]

		#TODO: figure out what is going on with the sessionids 
		index = link_node.get('href').find(";jsessionid")
		url = ''
		if index >= 0:
			url = link_node.get('href')[:index]
		else:
			url = link_node.get('href')
		
		new_page = parse(url).getroot()

		state.page = new_page

		pass

def fix_links(state, seq, html, url):

	if state.resolved_seq != seq:
		state.resolved_seq = seq
		return make_links_absolute(html,url)
	else:
		return html

def removeTBodies(xpath):

	parts = xpath.split("/")
	plist = list()

	for p in parts:
		if p.startswith("tbody"):
			continue

		plist.append(p)				

	return "/".join(plist)
	
class HighlightAction(ActionObject):


	def __init__(self, _xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does the HighlightAction and returns the state"""

		#get xpath	
		txt = Highlight.getSelectionFromTimestamp(int(self.xmlnode.get('id')), state.page)

		#get key and insert html into data hash
		key = self.xmlnode.get('id')

		state.kv_hash[key] = txt.strip()
		
		pass


class FormAction(ActionObject):

	def __init__(self, _xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		
		base_url = ""

		#get the base url for this form
		for e in self.xmlnode.getchildren():
			if e.tag == 'url':
				base_url = e.text.strip()	
				break

		# FIXME: Is XPath correct??
		xpath = Loader.getFormXpath(base_url, int(self.xmlnode.get('number')))

		#fix the links in the page
		doc = fix_links(state, self.xmlnode.get('number'), etree.tostring(state.page), state.page.base_url)	

		print etree.tostring(state.page);
		print self.xmlnode.get('number');
		print state.page.base_url

		#TODO: Fix this so that it uses standard xpath
		"""
		for e in self.xmlnode.getchildren():
			if e.tag == 'xpath':
				xpath = e.text.strip()
				break
		"""

		page = fromstring(doc)
		
		#need to fix the xpath before we use it
		xpath = removeTBodies(xpath.lower())
		
		xpath = "//form" # XXX debugging; needs to be removed  
		
		#get form node
		form_node = page.xpath(xpath)[0]
	
		#TODO:figure out what is up with the sessionids
		index = form_node.get('action').find(";jsessionid")
		
		print etree.tostring(form_node)
		
		querystring = ''
		if index >= 0:
			querystring = form_node.get('action')[:index]
		else:
			querystring = form_node.get('action')
	
		#start with the list of inputs for this form
		inputs = list()
		querystring += "?"
		first = False

		#get the form node from the page
		form = self.xmlnode						

		#need to parse the page and get the dropdown values matching the names given below

		#iterate through the list and find the input elements		
		for e in self.xmlnode.getchildren():

			#for each input get the value from the actiondata hash
			if e.tag == 'param':
				if first == False:
					querystring += "&" # Better 
	
				v = state.kv_hash[ e.text ].strip()
				state.kv_hash[e.text] = ''
				input_name = e.get('name').strip()

				if e.get('type') == 'select':
					#need to parse form to get values from dropdowns if any
					node = form_node.xpath("//select[@name='"+input_name+"']")[0]
					option = node.xpath("//option[text()='"+v.upper()+"']")[0]
					
					#now that we've found the option, we need the value
					v = option.get('value')				

				querystring += input_name + "=" + v
		
		#need to escape the url, but don't have a working method yet
		#TODO FILL IN
	
		#submit form
		page = parse(querystring).getroot()
			
		#now that we've submitted and gotten back the page, we should set it 
		state.page = page
		pass
