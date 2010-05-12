#!/usr/bin/python

import SOAPpy
from SOAPpy import WSDL

WSDLFILE = 'http://web-ngram.research.microsoft.com/Lookup.svc/mex?wsdl'

def getguid(file_name='guid.rc'):
	"""Pulls GUID from a local file from parameter. 

	The GUID is on first line of the file.
		
	"""
	f = open(file_name)
	g = f.readline()
	f.close()
	return g.strip()


_server = WSDL.Proxy(WSDLFILE)
def getModels(param1, param2='urn:ngram:bing-body:jun09:3'):
	"""The get models thing"""
	param1 = getguid()
	results = _server.GetModels(param1, param2)


def test():
	print _server.methods.keys()

if __name__ == '__main__':
	#import doctest
	#doctest.testmod()
	test()
	print getModels('Christan', 'Grant')
