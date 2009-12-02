"""
	This class implements the action objects for all actions
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']


class ActionObject:
	"""Base Class of all actions"""

	def do(self, state, xmlnode):
		pass


class URLAction(ActionObject):

	def do(self, state, xmlnode):
		"""Does URLAction and returns the state"""
		pass


class LinkAction(ActionObject):

	def do(self, state, xmlnode):
		"""Does LinkAction and returns the state"""
		pass


class HighlightAction(ActionObject):

	def do(self, state, xmlnode):
		"""Does the HighlightAction and returns the state"""
		pass


class FormAction(ActionObject):

	def do(self, state, xmlnode):
		"""Does the FormAction and returns the state"""
		pass
