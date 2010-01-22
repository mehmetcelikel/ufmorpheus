"""
	This class contains functioni that transform either user data or
	action data from xml nodes into hash tables.
"""

__authors__ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']



def ActionDataParser(xmlnode):
	"""Returns hash table pair.
			The first hash table pair is of the (key,value) pairs
			The second hash table pair is of the (key,type) pairs
			It does this by parsing children of the form:
				<info key="one" value="1" type="userinput"/>
	"""
	kv = {}
	for entry in xmlnode:
		assert(entry.tag == 'info') # sanity check
		kv[entry.get('key')] = entry.get('value')
	
	kt = {}
	for entry in xmlnode:
		assert(entry.tag == 'info') # sanity check
		kt[entry.get('key')] = entry.get('type')

	return (kv,kt) # return the pair of elements


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
		
