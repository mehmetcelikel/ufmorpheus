"""
This class runs the code
"""

__authors___ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import lxml
from lxml import etree
import pdb
import ActionState,ActionObject,DataParser
import sys
import pdb
import psycopg2
import urllib

__connect_string = "dbname='%(db)s' user='%(user)s' host='%(server)s' password='%(pwd)s'"
__connect_params = {'server': "babylon.cise.ufl.edu", 'user' : "morpheus3",'pwd' : "crimson03.sql", 'db' : "Morpheus3DB"}
__code_query = "SELECT code FROM qrm WHERE qrmid = %(id)s"

kv_hash = {}
kt_hash = {}
kclass_hash = {}
kcontext_hash = {}

def main(argv):
	
	"""Run the Executor script
	
	Iterate once over the script xml
		if node is userdate
			Create the hash table using UserDataParser
		else if node is action data
			Create Hash table pair using ActionDataParser
		else if is the starturl
		 create action object for it and make it first element in the action_list
		else it must be sequence
			Interate through the children and populate the action object list
	
	Initialize state with the created data structures
	Call state.run()
	"""
	id = sys.argv[-1] # The last value (which should be a number) is the id of the code script	

	try:
		connection = psycopg2.connect(__connect_string % __connect_params)
	except:
		print ("Connection to database failed")
		return None
	
	q = __code_query % {'id':id}
	
	cursor = connection.cursor()
	cursor.execute(q)
	result = cursor.fetchall()
	code = urllib.unquote( result[0][0] )

	assert(len(code) > 0) # ensure text was returned

	pdb.set_trace()	

	root = etree.fromstring(code)

	action_list = []
	kv_hash = {}	
	kt_hash = {}
	kclass_hash = {}
	kcontext_hash = {}
	user_hash = {}
	# Traversal of code script
	for child in root:
		if child.tag == 'actiondata':
			kv_hash, kt_hash, kclass_hash, kcontext_hash = DataParser.ActionDataParser(child)

		elif child.tag == 'userdata':
			user_hash = DataParser.UserDataParser(child)

		elif child.tag == 'starturl':
			action_list.insert(0,ActionObject.URLAction(child))

		elif child.tag == 'sequence':
			for ao in child:
				if ao.tag == 'constantlink':
					action_list.append(ActionObject.URLAction(ao))

				elif ao.tag == 'link':
					action_list.append(ActionObject.LinkAction(ao))

				elif ao.tag == 'highlight':
					action_list.append(ActionObject.HighlightAction(ao))

				elif ao.tag == 'form':
					action_list.append(ActionObject.FormAction(ao))

	#parse the ssq to populate the value hashes with the user input	
	if read_ssq(sys.argv[-2]) == False:
		return
	
	state = ActionState.ActionState(action_list, kv_hash, kt_hash, kclass_hash, kcontext_hash, user_hash)
	state.run()
	
	return state

#read in the ssq from an xml file or xml string
def read_ssq(xml):

	file = open(xml,'r')

	text = file.read()
	pdb.set_trace()
	file.close()

	return read_ssq_text(text)

#populate the value hash from a string
def read_ssq_text(xmlstring):

	tree = etree.fromstring(xmlstring)
	
	#find the input list and load the values into the kv_hash
	for e in tree.getchildren():
	
		if e.tag == 'input_list':
			for input in e.getchildren():
				if loadValueIntoHash(input) == False:	
					print('The given ssq does not match this qrm\'s ssq, aborting')
					return False
	return True

#parse the xml node and load its values into the appropriate spots
#in the value hash
def loadValueIntoHash(xml):
	
	#for the given xml node, we must find the matching key
	for entry in kv_hash.keys():
		cls = kclass_hash[entry]
		context = kcontext_hash[entry]
	
		#if we have found the appropriate key, then assign this ssq input's value 
		#to the data hash at the current key
		if cls == xml['dataclass'] and context == xml['context']:
			kv_hash[entry] = xml.text
			return True

	#if we reach this point then we haven't found a match
	return False			

if __name__ == '__main__':

	main(sys.argv)
	pass

