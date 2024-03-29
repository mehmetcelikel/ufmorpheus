"""
	This class implements the action objects for all actions
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import itertools
import cookielib, urllib, urllib2
import copy


class ActionState:
	"""A Wrapper class for the action state

		It requires that you supply the (key,value) (key,type) and action tables

	"""
	def __init__(self, _action_list, _kv_hash, _kt_hash, _kclass_hash, _kcontext_hash, _user_hash ):
		self.page = None # The current response page and dom source
		self.user_agent = None # Header for user agent and browser info
		self.cookie = cookielib.CookieJar()# Cookie object
		self.values = None # Values for form evaluation
		
		self.kv_hash = _kv_hash.copy() # Make a copy of the hash table
		self.kt_hash = _kt_hash.copy() # Make a copy of the hash table		
		self.kclass_hash = _kclass_hash.copy() # Make a copy of the hash table
		self.kcontext_hash = _kcontext_hash.copy() # Make a copy of the hash table
		self.user_hash = _user_hash.copy() # Copy hash table

		self.action_list = copy.copy(_action_list) # The list of actions 
		#self.iter = action_list.__iter__() # The action iterator
		self.resolved_seq = -1

	def run(self):
		""" This function call the do function on all the items im action_list"""
		map(lambda a: a.do(self), self.action_list) # State passed in is mutable

