"""
	This class implements the action objects for all actions
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import lxml
from lxml import etree
import re
import urllib, urllib2

class ActionObject:
	"""Base Class of all actions"""

	def do(self, state): abstract

def abstract():
    import inspect
    caller = inspect.getouterframes(inspect.currentframe())[1][3]
    raise NotImplementedError(caller + ' must be implemented in subclass')


class URLAction(ActionObject):

	def __init__(_xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does URLAction and returns the state"""
		url = xmlnode.text.strip() # Extract the url from <starturl>URL</starturl>
		request = urllib2.Request(url)
		response = urllib2.urlopen(request)
		# state.cookie = CookieJar() -- initialized in state
		state.cookie.extract_cookies(response,request)
		cookie_handler = urllib2.HTTPCookieProcessor( state.cookie )
		redirect_handler= HTTPRedirectHandler()
		opener = urllib2.build_opener(redirect_handler,cookie_handler)
		state.page = opener.open(request)


class LinkAction(ActionObject):


	def __init__(_xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does LinkAction and returns the state"""
		pass


class HighlightAction(ActionObject):


	def __init__(_xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does the HighlightAction and returns the state"""
		pass


class FormAction(ActionObject):


	def __init__(_xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does the FormAction and returns the state"""
		pass
