"""
	This class contains functioni that transform either user data or
	action data from xml nodes into hash tables.
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import pdb

def ActionDataParser(xmlnode):
	"""Returns hash table quartet.
			The first hash table pair is of the (key,value) pairs
			The second hash table pair is of the (key,type) pairs
			It does this by parsing children of the form:
				<info key="one" value="1" type="userinput" dataclass="classA" context="contextB"/>
	"""
	kv = {}
	for entry in xmlnode:
		assert(entry.tag == 'info') # sanity check
		kv[entry.get('key')] = entry.get('value')
	
	kt = {}
	for entry in xmlnode:
		assert(entry.tag == 'info') # sanity check
		kt[entry.get('key')] = entry.get('type')

	kcl = {}	
	for entry in xmlnode:
		assert(entry.tag == 'info')
		kcl[entry.get('key')] = entry.get('dataclass')
	
	kco = {}
	for entry in xmlnode:
		assert(entry.tag == 'info')
		kco[entry.get('key')] = entry.get('context')

	return (kv,kt,kcl,kco) # return the quartet of elements


def UserDataParser(xmlnode):
	"""Returns a hash table with the key value pairs.

			Does this by parsing all the children who are of the form:
			<info key="two" value="2" />
	"""
	hash = {}
	for entry in xmlnode:
		assert(entry.tag == 'info') # sanity check
		hash[entry.get('key')] = entry.get('value')

	return hash
		
