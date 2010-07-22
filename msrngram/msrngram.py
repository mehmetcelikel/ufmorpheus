#!/usr/bin/python

import urllib2

import payload



#######################################
#### Needed Constants
#######################################

CONTENT_TYPE = 'application/soap+xml; charset=utf-8'
USER_AGENT = r'Mozilla/4.0 (compatible; MSIE 5.5; Windows NT)'

WSDLFILE = r'http://web-ngram.research.microsoft.com/Lookup.svc/mex?wsdl'
LOOKUP_URL = r'http://web-ngram.research.microsoft.com/Lookup.svc'

GETMODELSURL = r'http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetModels'
GETCONDITIONALPROBABILITYURL = r'http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetConditionalProbability'
GETPROBABILITYURL = r'http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetProbability'


#######################################
#### Helper Functions
#######################################

def getguid():
	"""Pulls GUID from a local file from parameter. 

	The GUID is on first line of the file.
		
	"""
	return urllib2.urlopen('http://www.cise.ufl.edu/~cgrant/guid.rc').read().strip()

#######################################
#### Main Functions
#######################################

def GetModels():
	"""Gets the available models and returns a list
	
	>>> GetModels()
	['urn:ngram:bing-anchor:jun09:1', 'urn:ngram:bing-anchor:jun09:2', 'urn:ngram:bing-anchor:jun09:3', 'urn:ngram:bing-anchor:jun09:4', 'urn:ngram:bing-body:jun09:1', 'urn:ngram:bing-body:jun09:2', 'urn:ngram:bing-body:jun09:3', 'urn:ngram:bing-title:jun09:1', 'urn:ngram:bing-title:jun09:2', 'urn:ngram:bing-title:jun09:3', 'urn:ngram:bing-title:jun09:4']
	
	"""
	guid = getguid()
	data = payload.getmodelsrequest(guid)
	headers = { 'SOAPAction': GETMODELSURL,
				'Content-type' : CONTENT_TYPE,
				'User-Agent' : USER_AGENT}
	
	request = urllib2.Request(LOOKUP_URL, data, headers)
	response = urllib2.urlopen(request).read()
	
	return payload.getmodels(response)


def GetConditionalProbability(messageid, modelUrn, phrase):
	"""Gets the Conditional Probability from the parameter
	
	>>> GetConditionalProbability('2312312312','urn:ngram:bing-title:jun09:4', 'christan grant')
	'-3.91797137'
	
	"""
	guid = getguid()
	data = payload.getcondprob(messageid, guid, modelUrn, phrase)
	headers = { 'SOAPAction': GETCONDITIONALPROBABILITYURL,
				'Content-type' : CONTENT_TYPE,
				'User-Agent' : USER_AGENT}
	
	request = urllib2.Request(LOOKUP_URL, data, headers)
	response = urllib2.urlopen(request).read()
	
	return payload.getcondprobability(response)


def GetProbability(messageid, modelUrn, phrase):
	"""Gets the Probability from the parameter
	
	>>> GetProbability('2312312312','urn:ngram:bing-title:jun09:4', 'christan grant')
	'-9.032675'
	
	"""
	guid = getguid()
	data = payload.getprob(messageid, guid, modelUrn, phrase)
	headers = { 'SOAPAction': GETPROBABILITYURL,
				'Content-type' : CONTENT_TYPE,
				'User-Agent' : USER_AGENT}
	
	request = urllib2.Request(LOOKUP_URL, data, headers)
	response = urllib2.urlopen(request).read()
	
	return payload.getprobability(response)

	

if __name__ == '__main__':
	#import doctest
	#doctest.testmod()
	#print GetModels()
	#print GetProbability('2312312312','urn:ngram:bing-title:jun09:4', 'christan grant')
	#print getguid()
	import argparse, random
	parser = argparse.ArgumentParser(description='''
				This obtains the probability of using the msrcorpus.  If class alone is
				provided then this calculates p(''', add_help=True)
	parser.add_argument('--msgid', default=str(random.randint(10000,100000)),
				help='message id for transactions')
	parser.add_argument('--term', default=None,
				help='Finds the probability of this term')
	parser.add_argument('--context', default=None,
				help='Conditions the class on this context')
	parser.add_argument('--model', default='urn:ngram:bing-body:jun09:3', 
				help='Provide the model that will be used')
	parser.add_argument('--listmodels', type=bool, default=False, 
				help='If set true, this will query list the models available')

	args = parser.parse_args()
	
	if args.listmodels == True:
		print GetModels()
	elif args.term == None:
		parser.print_usage()
	elif args.term != None and args.context == None:
		print GetProbability(args.msgid, args.model, args.term)
	else:
		print GetConditionalProbability(args.msgid, args.model, args.context+' '+args.term)
	
