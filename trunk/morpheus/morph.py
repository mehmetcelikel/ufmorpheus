#!/usr/bin/python
import logging


def buildall(recompile=True):
	import sys
	
	# Add all necessary directories
	if '../' not in sys.path:
		sys.path.append('../')

	error_building = False # Set true when an error occured during the build
	exit_status = 0 # Remains zero if no problem calling functions

	# Check nqparser imports
	try:
		import nqparser
		import nqparser.extractor
	except Exception as e:
		print e
		error_building = True

	# Check qre imports
	try:
		import qre
		import qre.qre
	except Exception as e:
		print e
		error_building = True
	
	# Check imports of the supporting cast
	try:
		import nltk
		import lxml
		import psycopg2
	except Exception as e:
		print e
		error_building = True

	# TODO -- Check to make sure all nltk dependencies are installed

	# Compile ssqmatcher java code
	if recompile == True:
		exit_status = compile_ssqmatcher()

	return (error_building, exit_status)


def compile_ssqmatcher():
	""" Compiles the ssqmatcher code into a jarfile called ssqmatcher.jar """
	import subprocess

	cmd1 = ['ant','-buildfile', 'build_files.xml']
	cmd2 = ['ant','-buildfile', 'build_jar.xml']

	exit_status1 = subproces.Popen(cmd1, cwd='../ssqmatcher/').wait()
	exit_status2 = subproces.Popen(cmd2, cwd='../ssqmatcher/').wait()

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
	# also removes the trailng ';' from the format
	z = [j.split(':') for j in nqstring.strip(';').split(';')]
	
	# Add terms to the dictionary
	for t in z:
		if t[0] == 'NG':
			d[t[0]] = [j for j in t[1].split(',')]
		elif len(t) > 1:
			d[t[0]] = t[1]
		else:
			d[t[0]] = None
	
	#return json.dumps(d,indent=2)		 
	return json.dumps(d)		 


def getssqmatches(nquery):
	""" Call the ssq matching code and return output as a string """
	import subprocess, tempfile
	tf = tempfile.TemporaryFile()
	cmd = ['java','-jar','ssqmatcher.jar', '"%s"' % nquery] # nquery in quotes
	
	#print ' '.join(cmd)

	subprocess.Popen(cmd, stdout=tf, cwd='../ssqmatcher/').wait()

	tf.seek(0) # need to send the file back to the begining to read output

	return ' '.join([t for t in tf.readlines()])


if __name__ == '__main__':
	import argparse
	print buildall(False)
	nquery =  makenquery('What is the the tire size for a 1997 Toyota Camry')
	ssqmatches = getssqmatches(nquery)
	print ssqmatches

	# TODO - use the new ssqs to run the QRE
