#!/usr/bin/python

import lxml, lxml.etree
import psycopg2


def ssqbuilder(queryid):
	""" Takes a query id and returns the ssq representation """
	ssqinfo = get_ssq_info(queryid)

	# TODO - Add logic here for building the ssq stuff

	put_ssq_script(queryid, ssqstring) # The ssqstring is the prettyprinted xml 
	
	return ssqstring


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
	
