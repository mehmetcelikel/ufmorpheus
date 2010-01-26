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
	
	root = etree.fromstring(code)

	action_list = []
	kv_hash = {}	
	kt_hash = {}
	user_hash = {}
	# Traversal of code script
	for child in root:
		if child.tag == 'actiondata':
			kv_hash, kt_hash = DataParser.ActionDataParser(child)

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
	pdb.set_trace()

	state = ActionState.ActionState(action_list, kv_hash, kt_hash, user_hash)
	state.run()
	
	return state

if __name__ == '__main__':

	main(sys.argv)
	pass

