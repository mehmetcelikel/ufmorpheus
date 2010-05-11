
__author__ = ['"Christan Grant" <cgrant@cise.ufl.edu>' ]

import lxml, nltk, psycopg2, urllib2
from lxml import etree, html


def chunker(sentence):
	"""Transform the sentence into a list of terms (chuncks) """
	return sentence.split()


def query_map(terms, root):
	"""Takes an lets of terms and an XML root node and map to the input id 
	
	Returns a list of id terms lists. These represent possible sentences.
	ex: ['Where', 'is', 'Toyota', 'Owner'] --> 
					[{ 2: 'Acura'}, {2: 'Honda', 3: 'Passenger'}]
			
	"""
	print lxml.html.tostring(root, pretty_print = True)



#######################################
# Utility Functions
########################################

_host = "babylon.cise.ufl.edu"
_user = "morpheus3"
_pwd = "crimson03.sql"
_db = "Morpheus3DB"

def fetch_query(qrmid):
	"""Fetches a (query,qrm) pair from the database given the a qrmid
	
	query --> is the question string
	qrm is the etree root node for the document
	
	"""
	assert(int(qrmid))
	
	conn = None
	try:
		conn_str = "dbname='%s' user='%s' host='%s' password='%s'" % \
								(_db, _user, _host, _pwd)
		conn = psycopg2.connect(conn_str)
	except:
		print('Database connection Error')
		return None
	assert(conn != None)
	
	cursor = conn.cursor()
	try:
		_query = 'SELECT querystring, code  FROM query NATURAL JOIN qrm WHERE ' \
								'query.qrmid = qrm.qrmid AND query.qrmid = %d LIMIT 1' % qrmid

		cursor.execute(_query)
		conn.commit()
		result = cursor.fetchall()
		assert(len(result) > 0 and len(result[0]) > 0)
		
		query_string, code = result[0][0], urllib2.unquote(result[0][1])
		return [query_string, lxml.etree.fromstring(code)]

	except Exception as error:
		print str(error), ' :: fetch_query'



if __name__ == '__main__':
	import sys
	if len(sys.argv) > 1 and sys.argv[1].isdigit():
		qrmidarray = [int(sys.argv[1])]
	else:
		qrmidarray = [59,60,75]
	
	for qid in qrmidarray:
		sentence, qrm = fetch_query(qid)
		sentence = chunker(sentence)
		query_map(sentence, qrm)



