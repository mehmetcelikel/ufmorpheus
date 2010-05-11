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
def getModels(param1, param2):
	"""The get models thing"""
	results = _server.GetModels(param1, param2)

if __name__ == '__main__':
	import doctest
	doctest.testmod()
	print getModels('Christan', 'Grant')
