"""
	This class implements the action objects for all actions
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']


class ActionObject:
	"""Base Class of all actions"""

	def do(self, state):
		pass


class URLAction(ActionObject):

	def __init__(_xmlnode):
		self.xmlnode = _xmlnode

	def do(self, state):
		"""Does URLAction and returns the state"""
		pass


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
