#!/usr/bin/python

import lxml
import psycopg2


def etree_with_text(tag, text, **kwargs):
        """ Small function to construct an etree with a text field """
	etree = lxml.etree.Element(tag, **kwargs)
	etree.text = text
	return etree

def ssqbuilder(queryid):
	""" Takes a query id and returns the ssq representation """
	ssqinfo_list = get_ssq_info(queryid)
	
	## Used test case found at: http://pastebin.com/i1MKxmfM
	##ssqinfo_list = [{'contextname': 'What', 'realm': 'Automotive', 'name': 'Sedan', 'querystring': 'Where can I buy an engine for a Toyota Camry V6?', 'individualid': 1055, 'queryid': 595, 'io': 'input', 'phrasestring': 'Toyota Camry V6'}, {'contextname': 'What', 'realm': 'Automotive', 'name': 'Manufacturer', 'querystring': 'Where can I buy an engine for a Toyota Camry V6?', 'individualid': 1056, 'queryid': 595, 'io': 'input', 'phrasestring': 'Toyota'}, {'contextname': 'What', 'realm': 'Automotive', 'name': 'Model', 'querystring': 'Where can I buy an engine for a Toyota Camry V6?', 'individualid': 1057, 'queryid': 595, 'io': 'input', 'phrasestring': 'Camry V6'}, {'contextname': 'What', 'realm': 'Automotive', 'name': 'Part', 'querystring': 'Where can I buy an engine for a Toyota Camry V6?', 'individualid': 1058, 'queryid': 595, 'io': 'output', 'phrasestring': 'engine'}]

	## Results of test case:
	##<ssq>
	##  <realm>Automotive</realm>
	##  <query>Where can I buy an engine for a Toyota Camry V6?</query>
	##  <input_list>
	##    <input dataclass="Sedan" type="What">Toyota Camry V6</input>
	##    <input dataclass="Manufacturer" type="What">Toyota</input>
	##    <input dataclass="Model" type="What">Camry V6</input>
	##  </input_list>
	##  <output_list>
	##    <output dataclass="Part" type="What">engine</output>
	##  </output_list>
	##</ssq>

	
	# Realm and query should be the same for all
	# Construct ssq
	ssq_etree = lxml.etree.Element('ssq')
	ssq_etree.append(etree_with_text(tag='realm', text=ssqinfo_list[0]['realm']))
	ssq_etree.append(etree_with_text(tag='query', text=ssqinfo_list[0]['querystring']))

	input_etree = lxml.etree.Element('input_list')
	output_etree = lxml.etree.Element('output_list')
	for ssq in ssqinfo_list:
		if ssq['io'].lower() == 'input':
			input_etree.append(etree_with_text('input', ssq['phrasestring'],
							   **{'type':ssq['contextname'], 'dataclass':ssq['name']}))
		elif ssq['io'].lower() == 'output':
			output_etree.append(etree_with_text('output', ssq['phrasestring'],
							   **{'type':ssq['contextname'], 'dataclass':ssq['name']}))
		else:
			pass # Throw an exception?

	ssq_etree.append(input_etree)
	ssq_etree.append(output_etree)

	# Unsure what to do with individualid and queryid
	
	put_ssq_script(queryid, ssqstring) # The ssqstring is the prettyprinted xml 
	
	return lxml.etree.tostring(ssq_etree, pretty_print=True)


__connect_string = "dbname='%(db)s' user='%(user)s' host='%(server)s'	password='%(pwd)s'"
__connect_params = {'server': "babylon.cise.ufl.edu", 'user' :"morpheus3",\
				'pwd' : "crimson03.sql", 'db' : "Morpheus3DB"}


def get_ssq_info(queryid):
	""" Takes the queryid and does a query to get all necessary ssq info """
	try:
		connection = psycopg2.connect(__connect_string % __connect_params)
	except Exception as e:
		print e
		return None

	cursor = connection.cursor();

	cursor.execute("select Q.queryid, Q.querystring, R.realm, QH.individualid, \
				QH.io, P.phrasestring , C.name, T.contextname \
				from query Q  inner join queryhas QH  on QH.queryid = Q.queryid and Q.queryid = (%s) \
			  inner join individual I on I.individualid = QH.individualid \
			  inner join phrase P on P.phraseid = I.phraseid \
			  left join class C on C.classid = I.classid \
			  left join realm R on Q.realmid = R.realmid \
			  left join context T on T.contextid = C.contextid ", (queryid,));

	rowarray = []

	# This builds an array of dictionaries. Each dictionary key is the column
	#  name and the value is the value.  Dictionaries represent rows.

	# The desctiption attribute described: http://initd.org/psycopg/docs/cursor
	for row in cursor:
		d = {}
		for (r,desc) in zip(row, cursor.description):
			d[desc[0]] = r
		rowarray.append(d)

	return rowarray


def put_ssq_script(queryid, ssqstring):
	""" Inserts ssqstring to the query table and returns True on success """
	try:
		connection = psycopg2.connect(__connect_string % __connect_params)
	except Exception as e:
		print e
		return None

	cursor = connection.cursor();
	
	query = """UPDATE query SET ssq = "%s" where queryid = %s""" % \
				(ssqstring, queryid)
	cursor.execute(query)
	cursor.commit()

	if cursor.rowcont == 1:
		return True
	else:
		return False
	
