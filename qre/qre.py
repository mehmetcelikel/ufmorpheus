#!/usr/bin/python
"""
This class runs the code
"""

__authors__ = "Christan Earl Grant"

import ActionState,ActionObject,DataParser

import lxml, psycopg2
from lxml import etree

import sys, urllib

import pdb

__connect_string = "dbname='%(db)s' user='%(user)s' host='%(server)s' \
				password='%(pwd)s'"
__connect_params = {'server': "babylon.cise.ufl.edu", 'user' : "morpheus3",'pwd' : "crimson03.sql", 'db' : "Morpheus3DB"}
__code_query = "SELECT code FROM qrm WHERE qrmid = %(id)s"
id_test = 85
ssq_test = """<ssq>
		
        <realm>Automotive</realm>
        <query>What size tires should I get for a 1997 Toyota Camry V6?</query>

        <input_list>
                <input type="what" dataclass="Year">
               	1997 
                </input>
                <input type="what" dataclass="Manufacturer">
                Toyota
                </input>
                <input type="what" dataclass="Model">
                Camry V6
                </input>
        </input_list>

        <output_list>
                <output type="what" dataclass="size">
                        <modifier value="tires"/>
                </output>
        </output_list>

</ssq>
"""


def run(ssq=ssq_test, id=id_test):
	"""Run the Executor script
	Get code xml from the DB given the id	
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

	try:
		connection = psycopg2.connect(__connect_string % __connect_params)
	except:
		print ("Connection to database failed")
		return None
	
	q = __code_query % {'id':id}
	
	cursor = connection.cursor()
	cursor.execute(q)
	result = cursor.fetchall()
	code = urllib.unquote(result[0][0])

	assert(len(code) > 0) # Ensure text was returned

	root = etree.fromstring(code)

	action_list = []
	kv_hash = {}	
	kt_hash = {}
	kclass_hash = {}
	kcontext_hash = {}
	user_hash = {}
	
	# Traversal of code script
	# TODO before building script add a preprocessing step to add profile data.
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
				
				elif ao.tag == 'api':
					action_list.append(ActionObject.APIAction(ao))

	#parse the ssq to populate the value hashes with the user input	
	txt = ssq
	if read_ssq_text(txt,kv_hash,kclass_hash, kcontext_hash, kt_hash) == False:	
		return
	
	state = ActionState.ActionState(action_list, kv_hash, kt_hash, kclass_hash, kcontext_hash, user_hash)
	
	state.run()

	result = ' '
	for k in state.kv_hash.keys():
		if state.kv_hash[k] != '':
			result += ' '
			result += state.kv_hash[k] 

	return result.strip()


#populate the value hash from a string
def read_ssq_text(xmlstring, valueHash, classHash, contextHash, typeHash):

	# TODO -- include user profile into the execution environment 
	#valueHash["2:0"] = "32608"
	#classHash["2:0"] = "Size"
	#contextHash["2:0"] = "What"
	 

	tree = etree.fromstring(xmlstring)
	total = 0
	#any highlights present need to be counted first
	for k in valueHash.keys():
		if valueHash[k] != '':total+=1

	#find the input list and load the values into the kv_hash
	for e in tree.getchildren():

		if e.tag == 'input_list':
			for input in e.getchildren():
				total += loadValueIntoHash(input, valueHash, classHash, contextHash, typeHash)

	print 'Compares total:%d and len(valueHash.keys()):%d' % (total,len(valueHash.keys()))
	print valueHash
	print classHash
	print contextHash
	print typeHash

	if total == len(valueHash.keys()):
		return True
	
	return True	
	print('The given ssq does not match this qrm\'s ssq, aborting')
	return False


#parse the xml node and load its values into the appropriate spots
#in the value hash
def loadValueIntoHash(xml, valueHash, classHash, contextHash, typeHash):

	found = 0
	#for the given xml node, we must find the matching key
	for entry in valueHash.keys():
		cls = classHash[entry]
		context = contextHash[entry]
	
		#if we have found the appropriate key, then assign this ssq input's value 
		#to the data hash at the current key
		if cls.lower() == xml.get('dataclass').lower() and context.lower() == xml.get('type').lower():
			valueHash[entry] = '' if not xml.text else xml.text.strip()
			found+=1

	return found


if __name__ == '__main__':
	import argparse
	parser = argparse.ArgumentParser(description=''' This module does 
				the execution of the execution of QRMs as specified by its 
				parameters''', add_help=True, 
				epilog='contact cgrant@cise.ufl.edu for details')
	parser.add_argument('--qrmid','-q',type=int)
	parser.add_argument('--ssq','-s')
	parser.add_argument('--ssqf','-sf', help='File location of ssq' )
	parser.add_argument('--debug','-d',default=False, action='store_true')
	args = parser.parse_args()

	if args.debug == True:
		#pdb.set_trace()
		print run(ssq_test,id_test)
	elif (args.ssq != None or args.ssqf != None) and \
				args.qrmid != None:
			if args.ssqf == None:
				print run(args.ssq,args.qrmid)
			else:
				ssq_text = open(args.ssqf, 'r').read()
				print ssq_text
				print run(ssq_text,args.qrmid)
			
	else:
		parser.print_usage()
				

