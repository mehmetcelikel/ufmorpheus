"""
	This class implements the action objects for all actions
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import itertools
import cookielib, urllib, urllib2


class ActionState:
	"""A Wrapper class for the action state

		It requires that you supply the (key,value) (key,type) and action tables

	"""
	def __init__(self, _action_list, _kv_hash, _kt_hash, _user_hash ):
		self.page = None # The current response page and dom source
		self.user_agent = None # Header for user agent and browser info
		self.cookie = CookieJar()# Cookie object
		self.values = None # Values for form evaluation
		
		self.kv_hash = _kv_hash.copy() # Make a copy of the hash table
		self.kt_hash = _kt_hash.copy() # Make a copy of the hash table
		self.user_hash = _user_hash.copy() # Copy hash table

		self.action_list = _action_list.copy() # The list of actions 
		#self.iter = action_list.__iter__() # The action iterator


	def run(self):
		""" This function call the do function on all the items im action_list"""
		map(lambda a: a.do(self), action_list) # State passed in is mutable

