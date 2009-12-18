"""
This class runs the code
"""

__authors___ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

import lxml
from lxml import etree

import psycopg2
import sys

connect_string = "dbname='%(db)s' user='%(user)s' host='%(server)s' password='%(pwd)s'"
__connect_params = {'server': "babylon.cise.ufl.edu", 'user' : "morpheus3",\
										 'pwd' : "crimson03.sql", 'db' : "Morpheus3DB"}

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
	id = sys.argv[-1]	

	try:
		connection = psycopg2.connect(connect_string % __connect_params)
	except:
		print ("Connection to database failed")
		return None
	
	q = "SELECT code FROM qrm WHERE qrm.id = {id}" % {'id':id}
	
	cursor = connection.cursor()
	cursor.execute(q)
	result = cursor.fetchall()
	code = result[0][0]
	
	assert(len(code) > 0)
	
	root = etree.fromstring(code)

	action_list = []
	kv_hash = {}	
	kt_hash = {}
	user_hash = {}

	# Traversal of code script
	for child in root:
		if child.tag == 'actiondata':
			kv_hash, kt_hash = ActionDataParser(child)

		elif child.tag == 'userdata':
			user_hash = UserDataParser(child)

		elif child.tag == 'starturl':
			action_list.insert(0,URLAction(child))

		elif child.tag == 'sequence':
			for ao in child:
				if ao.tag == 'constantlink'
					action_list.append(URLAction(ao))

				elif ao.tag == 'link'
					action_list.append(LinkAction(ao))

				elif ao.tag == 'highlight'
					action_list.append(HighlightAction(ao))

				elif ao.tag == 'form'
					action_list.append(FormAction(ao))
	
	state = ActionState(action_list, kv_hash, kt_hash, user_hash)
	state.run()

	return state

