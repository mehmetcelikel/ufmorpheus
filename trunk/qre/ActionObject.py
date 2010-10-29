"""
	This class implements the action objects for all actions
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import json
import lxml
import lxml.html
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

		url = self.xmlnode.text.strip() # Extracts url from <starturl>URL</starturl>
		state.page = parse(url).getroot()


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


def fix_links(state, seq, html, url):

	if state.resolved_seq != seq:
		state.resolved_seq = seq
		return make_links_absolute(html,url)
	else:
		return html


def removeTBodies(xpath):
	return xpath.replace('tbody', '')


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


class APIAction(ActionObject):

	def __init__(self, _xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		base_url = ""
		for e in self.xmlnode.getchildren():
			if e.tag == 'url':
				base_url = e.text.strip()
				break
		
		request_data = {} # API params.
		
		for e in self.xmlnode.getchildren():
			if e.tag == 'param':
				v = state.kv_hash[ e.text ].strip() # Get the value key.
				state.kv_hash[e.text] = '' # FIXME why are we clearing this value??
				input_name = e.get('name').strip()
				request_data[input_name] = v # Add the parameter to the form data
		
		# TODO add verification functionality
		
		the_page = None
		new_base = None
		
		method = self.xmlnode.find('method').get('type')
		if method is None or method.lower().strip() == "get":
			action_url = base_url
			url_values = urllib.urlencode(request_data)
			full_url = action_url + '?' + url_values
			response = urllib2.urlopen(full_url)
		else:
			# Do the POST submit
			# We go where the action takes us
			action_url = base_url
			pdata = urllib.urlencode(request_data)
			req = urllib2.Request(action_url, pdata)
			response = urllib2.urlopen(req)

		the_page = response.read()
		new_base = response.geturl()
		
		# Next parse the new page
		page = None
		if self.xmlnode.find('method').get('response') == 'json':
			# TODO think of a way to process json responses
			pass
		elif self.xmlnode.find('method').get('response') in ('xml','html'):
			page = lxml.html.fromstring(the_page, base_url=new_base)
			xp = self.xmlnode.find('method').text
			xp = xp.lower().strip()
			xp_results = [ e.text for e in page.xpath(xp)]
	
		res = xp_results
		if self.xmlnode.find('method').get('operation'):
			var = self.xmlnode.find('method').get('var')
			res = doOperation(self.xmlnode.find('method').get('operation'),xp_results,var)
	
		# Place the result in the proper location
		resultid = self.xmlnode.find('method').get('result')
		# TODO how should we process api calls, do we can the first
		state.kv_hash[resultid] = str(res)
		
		state.page = page


def doOperation(op, list, var):
	""" Perform operation on the list """

	def make_sort_func(x):
		""" Function to do sort """
		if x == 'integer':
			return int
		elif x == 'decimal':
			return float
		else:
			return str
	
	func = make_sort_func(var)
	
	if op == 'max':
		return max(list, key=func)
	elif op == 'min':
		return min(list, key=func)
	elif op == 'sort asc':
		return sorted(list, key=func)
	elif op == 'sort desc':
		return sorted(list, key=func, reverse=True)
	else:
		print 'Error: Bad input operation in doOpertion'
		return list


class FormAction(ActionObject):

	def __init__(self, _xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		base_url = ""

		# Get the base url for this form
		for e in self.xmlnode.getchildren():
			if e.tag == 'url':
				base_url = e.text.strip()	
				break

		# FIXME: Is XPath correct? -- need to use the qre xpath
		xpath = Loader.getFormXpath(base_url, int(self.xmlnode.get('number')))
		#xpath = self.xmlnode.find('xpath'). <
		#xpath = self.xmlnode.find('xpath').text

		# Fix the links in the page
		doc = fix_links(state, self.xmlnode.get('number'), etree.tostring(state.page), state.page.base_url)	


		#TODO: Fix this so that it uses standard xpath
		"""
		for e in self.xmlnode.getchildren():
			if e.tag == 'xpath':
				xpath = e.text.strip()
				break
		"""

		page = fromstring(doc)
		
		# Need to fix the xpath before we use it. We assume nobody uses <tbody>
		xpath = removeTBodies(xpath.lower())
		
		# Get form node
		form_node = None
		try:
			form_node = page.xpath(xpath)[0]
		except:
			# Error with xpath, back off to general xpath
			form_node = page.xpath('//form')[0]
		
		inputs = list()
		request_data = {}
		first = False

		#get the form node from the page
		form = self.xmlnode

		# Need to parse the page and get the dropdown values matching the names given below
		# Iterate through the list and find the input elements		
		for e in self.xmlnode.getchildren():

			#for each input get the value from the actiondata hash
			if e.tag == 'param':
				v = state.kv_hash[ e.text ].strip()
				state.kv_hash[e.text] = '' # FIXME why are we clearing this??
				input_name = e.get('name').strip()

				if e.get('type') == 'select':
					#need to parse form to get values from dropdowns if any
					node = form_node.xpath("//select[@name='"+input_name+"']")[0]
					option = node.xpath("//option[text()='"+v.upper()+"']")[0]
					
					#now that we've found the option, we need the value
					v = option.get('value')

				request_data[input_name] = v # Add the parameter to the form data
		
		#need to escape the url, but don't have a working method yet

		the_page = None
		new_base = None

		#submit form
		method = form.find('method').get('type')
		if method is None or method.lower().strip() == "get":
			# We go where the action takes us
			action_url = form_node.get('action')
			url_values = urllib.urlencode(request_data)
			full_url = action_url + '?' + url_values
			response = urllib2.urlopen(full_url)
			the_page = response.read()
			new_base = response.geturl()
		else:
			# Do the POST submit
			# We go where the action takes us
			action_url = form_node.get('action')
			pdata = urllib.urlencode(request_data)
			req = urllib2.Request(action_url, pdata)
			response = urllib2.urlopen(req)
			the_page = response.read()
			new_base = response.geturl()
		
		page = lxml.html.fromstring(the_page, base_url=new_base) # TODO -- Check this parses bad html
		#page = parse(querystring).getroot()
			
		#now that we've submitted and gotten back the page, we should set it 
		state.page = page
