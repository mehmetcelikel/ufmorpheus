"""
	This class implements the action objects for all actions
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import urllib
import urllib2


class ActionState:
	"""A Wrapper class for the action state

		It requires that you supply the (key,value) (key,type) and action tables

	"""
	def __init__(self, _kv_hash, _kt_hash, _action_hash ):
		self.page = None # The current response page and dom source
		self.user_agent = None # Header for user agent and browser info
		self.cookie = None # Cookie object
		self.values = None # Values for form evaluation
		
		self.kv_hash = _kv_hash.copy() # Make a copy of the hash table
		self.kt_hash = _kt_hash.copy() # Make a copy of the hash table
		self.action_hash = _action_hash.copy() # Copy hash table
	 
