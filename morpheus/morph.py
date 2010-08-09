#!/usr/bin/python
import logging
import sys
import urllib

import lxml, lxml.etree, lxml.html
import psycopg2

__connect_string = "dbname='%(db)s' user='%(user)s' host='%(server)s' \
				password='%(pwd)s'"
__connect_params = {'server': "babylon.cise.ufl.edu", 'user' : \
				"morpheus3",'pwd' : "crimson03.sql", 'db' : "Morpheus3DB"}


def buildall(recompile=True):
	""" Check dependencies for end 2 end. Compile where needed. Update path """
	
	# Add all necessary directories
	if '../' not in sys.path:
		sys.path.append('../')

	error_building = False # Set true when an error occured during the build
	exit_status = 0 # Remains zero if no problem calling functions

	# Check nqparser imports
	try:
		import nqparser, nqparser.extractor
	except Exception as e:
		print e
		error_building = True

	# Check qre imports
	sys.path.append('../qre/')
	try:
		import qre, qre.qre
	except Exception as e:
		print e
		error_building = True
	
	# Check imports of the supporting cast
	try:
		import nltk, lxml, psycopg2
	except Exception as e:
		print e
		error_building = True

	# TODO -- Check to make sure all nltk dependencies are installed

	# TODO -- Check the db connection
	try:
		psycopg2.connect(__connect_string % __connect_params)
	except Exception as e:
		print e
		error_building = True

	# Compile ssqmatcher java code
	if recompile:
		exit_status = compile_ssqmatcher()

	return (error_building, exit_status)


def compile_ssqmatcher():
	""" Compiles the ssqmatcher code into a jarfile called ssqmatcher.jar """
	import subprocess

	cmd1 = ['ant','-buildfile', 'build_files.xml']
	cmd2 = ['ant','-buildfile', 'build_jar.xml']

	exit_status1 = subprocess.Popen(cmd1, cwd='../ssqmatcher/').wait()
	exit_status2 = subprocess.Popen(cmd2, cwd='../ssqmatcher/').wait()

	return exit_status1 + exit_status2
	

def makenquery(query):
	""" Takes a natural language query and turns it in to the natual query format
	
	This is done using the nqparser module.

	"""
	import nqparser
	import nqparser.extractor

	extractor = nqparser.extractor.TermExtractor()
	extractor.run(query, False)
	nqstring = extractor.getExportString()

	return nquerytojson(nqstring) # Before returning, turn it into JSON


def nquerytojson(nqstring):
	""" Takes the output of the nqparser and returns it in JSON format """
	import json
	d = {}
	
	# Split the string to get the key value pairs
	# Also, removes the trailng ';' from the format
	z = [j.split(':') for j in nqstring.strip(';').split(';')]
	
	# Add terms to the dictionary
	for t in z:
		if t[0] == 'NG':
			d[t[0]] = [j for j in t[1].split(',')]
		elif len(t) > 1:
			d[t[0]] = t[1]
		else:
			d[t[0]] = None
	
	#return json.dumps(d,indent=2) Another possible return to make it prettier
	return json.dumps(d)		 


def getssqmatches(nquery):
	""" Call the ssq matching code and return output as a string """
	import subprocess, tempfile, json
	tf = tempfile.TemporaryFile()
	cmd = ['java','-jar','ssqmatcher.jar', "%s" % nquery] # nquery in quotes

	subprocess.Popen(cmd, stdout=tf, cwd='../ssqmatcher/').wait()

	tf.seek(0) # Need to send the file back to the begining to read output

	return json.load(tf)


def getqrmcode(qrmid):
	import psycopg2
	
	__query = "SELECT code FROM qrm WHERE qrmid = %(id)s"
	
	conn = psycopg2.connect(__connect_string % __connect_params) # Assumed 2 work
	q = __query % {'id':qrmid}
	
	cursor = conn.cursor()
	cursor.execute(q)
	result = cursor.fetchall()
	code = urllib.unquote(result[0][0])
	
	assert(len(code) > 0) # Ensure text was returned

	return code


def get_qrmid_ssqpair(queryid):
	""" Gets the (qrmid,ssq) pair from query table """
	__query = "SELECT qrmid,ssq FROM query WHERE queryid = %(id)s"
	
	conn = psycopg2.connect(__connect_string % __connect_params) # Assumed 2 work
	q = __query % {'id':queryid}
	
	cursor = conn.cursor()
	cursor.execute(q)
	result = cursor.fetchall()
	if len(result) == 0:
		return (None,None)
	else:
		qrmid,ssq = result[0][0], urllib.unquote(result[0][1])

	return (qrmid,ssq)

def extract_ssq_term_map(queryid, divergence, class_map):
	""" This method extracts the best terms or each class from the class_map
			
			What is returned is a dictionary with the key being the classes
			and the value being the best terms.
	"""
	#term_map = {}	
	#for (k,v) in class_map.iteritems():
	#	term_map[k] = v[0][1]
	return dict([(k,v[0][1]) for (k,v) in class_map.iteritems()])


def make_query_ssq(qrm_ssq, term_map):
	""" Replaces the ssq inputs with new terms, returns new SSQ """
	root = lxml.etree.fromstring(qrm_ssq)
	input_list = root.find('input_list')

	for (the_class,the_term) in term_map.iteritems():
		node = input_list.xpath("//input[@dataclass='%s']"%the_class)
		node.text = the_term # Update the term for this class
	
	return lxml.etree.tostring(root)


def run_qre(ssq, qrmid):
	""" Runs the qre on the above arguments and returns the result """
	import qre
	import qre.qre
	return qre.qre.run(ssq,qrmid)


def run_morpheus(build=True, \
				query='A 1997 Toyota Camry V6 needs what tire size?'):
	""" This is the main method for running the morpheus program """

	buildall(build) # Build all java code if necessary
	
	print 
	print '-'*40
	nquery =  makenquery(query) # Make the nquery object
	
	print 
	print '-'*40
	ssqm = getssqmatches(nquery) # Returns a loaded json object
	
	print ssqm
	print 
	print '-'*40
	
	# TODO - Need to merge the created SSQ and run it against qrms
	# This only runs previous querys
	result_array = []
	for entry in ssqm['queryids']:
		term_map = extract_ssq_term_map(*entry) # Breaks down the tuple into parts
		qrmid,qrm_ssq = get_qrmid_ssqpair(entry[0]) # Pass in queryid, 
		query_ssq = make_query_ssq(qrm_ssq, term_map)
		if None in (qrmid,qrm_ssq):
			continue
		else:
			z = run_qre(ssq, qrmid)
			result_array.append(z)
	
	print 
	print '-'*40
	return result_array		
	


if __name__ == '__main__':
	import argparse
	import json, pdb

	parser = argparse.ArgumentParser(description="This module runs morpheus",
				add_help=True)
	parser.add_argument('--query', 
				default='A 1997 Toyota Camry V6 needs what tire size?')
	parser.add_argument('--build','-b',default=False,action='store_false',
				help='This is true if we should rebuild the java code')
	args = parser.parse_args()
	# Run morpheus!
	print run_morpheus(build=args.build, query=args.query)

