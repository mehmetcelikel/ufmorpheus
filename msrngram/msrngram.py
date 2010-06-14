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

def getguid(file_name='guid.rc'):
	"""Pulls GUID from a local file from parameter. 

	The GUID is on first line of the file.
		
	"""
	f = open(file_name)
	g = f.readline()
	f.close()
	return g.strip()


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
	import doctest
	doctest.testmod()
	#print GetModels()
	#print GetProbability('2312312312','urn:ngram:bing-title:jun09:4', 'christan grant')
