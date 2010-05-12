_getmodels_request = \
'''<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetModels</a:Action>
    <a:MessageID>urn:uuid:%(guid)s</a:MessageID>
    <a:ReplyTo>
      <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>
    </a:ReplyTo>
    <a:To s:mustUnderstand="1">http://web-ngram.research.microsoft.com/Lookup.svc</a:To>
  </s:Header>
  <s:Body>
    <GetModels xmlns="http://schemas.microsoft.com/research/2009/10/webngram/frontend" />
  </s:Body>
</s:Envelope>''' # guid


_getconditionalprobability_request = \
'''<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetConditionalProbability</a:Action>
    <a:MessageID>urn:uuid:%(messageid)s</a:MessageID>
    <a:ReplyTo>
      <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>
    </a:ReplyTo>
    <a:To s:mustUnderstand="1">http://web-ngram.research.microsoft.com/Lookup.svc</a:To>
  </s:Header>
  <s:Body>
    <GetConditionalProbability xmlns="http://schemas.microsoft.com/research/2009/10/webngram/frontend">
      <authorizationToken>%(guid)s</authorizationToken>
      <modelUrn>%(modelUrn)s</modelUrn>
      <phrase>%(phrase)s</phrase>
    </GetConditionalProbability>
  </s:Body>
</s:Envelope>''' # messageid, guid, modelUrn, phrase


_GETCONDITIONALPROBABILITIES_REQUEST = \
'''<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetConditionalProbabilities</a:Action>
    <a:MessageID>urn:uuid:%s(messageid)</a:MessageID>
    <a:ReplyTo>
      <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>
    </a:ReplyTo>
    <a:To s:mustUnderstand="1">http://web-ngram.research.microsoft.com/Lookup.svc</a:To>
  </s:Header>
  <s:Body>
    <GetConditionalProbabilities xmlns="http://schemas.microsoft.com/research/2009/10/webngram/frontend">
      <authorizationToken>%(guid)s</authorizationToken>
      <modelUrn>urn:ngram:bing-title:jun09:4</modelUrn>
      <phrases xmlns:b="http://schemas.microsoft.com/2003/10/Serialization/Arrays" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
        <b:string>one two three</b:string>
        <b:string>one two three</b:string>
      </phrases>
    </GetConditionalProbabilities>
  </s:Body>
</s:Envelope>''' # messageid TODO - need to do some work to make this work


GETCONDITIONALPROBABILITIES_RESPONSE = \
'''<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetConditionalProbabilitiesResponse</a:Action>
    <a:RelatesTo>urn:uuid:##YOUR_MESSAGE_GUID##</a:RelatesTo>
  </s:Header>
  <s:Body>
    <GetConditionalProbabilitiesResponse xmlns="http://schemas.microsoft.com/research/2009/10/webngram/frontend">
      <GetConditionalProbabilitiesResult xmlns:b="http://schemas.microsoft.com/2003/10/Serialization/Arrays" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
        <b:float>-0.5097093</b:float>
        <b:float>-0.5097093</b:float>
      </GetConditionalProbabilitiesResult>
    </GetConditionalProbabilitiesResponse>
  </s:Body>
</s:Envelope>''' # TODO something smart to make this work


_getprobability_request = \
'''<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetProbability</a:Action>
    <a:MessageID>urn:uuid:%(messageid)s</a:MessageID>
    <a:ReplyTo>
      <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>
    </a:ReplyTo>
    <a:To s:mustUnderstand="1">http://web-ngram.research.microsoft.com/Lookup.svc</a:To>
  </s:Header>
  <s:Body>
    <GetProbability xmlns="http://schemas.microsoft.com/research/2009/10/webngram/frontend">
      <authorizationToken>%(guid)s</authorizationToken>
      <modelUrn>%(modelUrn)s</modelUrn>
      <phrase>%(phrase)s</phrase>
    </GetProbability>
  </s:Body>
</s:Envelope>''' # messageid guid modelUrn phrase


_getprobability_response = \
'''<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetProbabilityResponse</a:Action>
    <a:RelatesTo>urn:uuid:%(messageid)s</a:RelatesTo>
  </s:Header>
  <s:Body>
    <GetProbabilityResponse xmlns="http://schemas.microsoft.com/research/2009/10/webngram/frontend">
      <GetProbabilityResult>%(probability)s</GetProbabilityResult>
    </GetProbabilityResponse>
  </s:Body>
</s:Envelope>''' # messageid probability

GETPROBABILITIES_REQUEST_RESPONSE = \
'''<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetProbabilities</a:Action>
    <a:MessageID>urn:uuid:##YOUR_MESSAGE_GUID##</a:MessageID>
    <a:ReplyTo>
      <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>
    </a:ReplyTo>
    <a:To s:mustUnderstand="1">http://web-ngram.research.microsoft.com/Lookup.svc</a:To>
  </s:Header>
  <s:Body>
    <GetProbabilities xmlns="http://schemas.microsoft.com/research/2009/10/webngram/frontend">
      <authorizationToken>%(guid)s</authorizationToken>
      <modelUrn>urn:ngram:bing-title:jun09:4</modelUrn>
      <phrases xmlns:b="http://schemas.microsoft.com/2003/10/Serialization/Arrays" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
        <b:string>one two three</b:string>
        <b:string>one two three</b:string>
      </phrases>
    </GetProbabilities>
  </s:Body>
</s:Envelope>'''


GETPROBABILITIES_RESPONSE = \
'''<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://schemas.microsoft.com/research/2009/10/webngram/frontend/ILookupService/GetProbabilitiesResponse</a:Action>
    <a:RelatesTo>urn:uuid:##YOUR_MESSAGE_GUID##</a:RelatesTo>
  </s:Header>
  <s:Body>
    <GetProbabilitiesResponse xmlns="http://schemas.microsoft.com/research/2009/10/webngram/frontend">
      <GetProbabilitiesResult xmlns:b="http://schemas.microsoft.com/2003/10/Serialization/Arrays" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
        <b:float>-6.26909828</b:float>
        <b:float>-6.26909828</b:float>
      </GetProbabilitiesResult>
    </GetProbabilitiesResponse>
  </s:Body>
</s:Envelope>'''




import lxml
from lxml import etree

def getmodelsrequest(guid):
	""" Creates and returns the list of models """
	return _getmodels_request % {'guid':guid}


def getmodels(response_string):
	""" This model takes the string and returns a list of models from it"""
	root = etree.XML(response_string)
	model_list = root.xpath(r'//b:string', namespaces={'b': \
				'http://schemas.microsoft.com/2003/10/Serialization/Arrays'})
	
	return [model.text for model in model_list] 
	


def getcondprob(messageid, guid, modelUrn, phrase):
	return _getconditionalprobability_request % {'messageid' : messageid, \
				'guid' : guid, 'modelUrn': modelUrn, 'phrase' : phrase}


def getcondprobability(response_string):
	root = etree.XML(response_string)
	#print etree.tostring(root, pretty_print=True)
	p = root.xpath(r'//xmlns:GetConditionalProbabilityResult', 
				namespaces={'xmlns': \
				'http://schemas.microsoft.com/research/2009/10/webngram/frontend'})
	# TODO - also return the messageid
	return p[0].text
	

	
def getprob(messageid, guid, modelUrn, phrase):
	return _getprobability_request % {'messageid' : messageid, \
				'guid' : guid, 'modelUrn': modelUrn, 'phrase' : phrase}

	
def getprobability(response_string):
	root = etree.XML(response_string)
	#print etree.tostring(root, pretty_print=True)
	p = root.xpath(r'//xmlns:GetProbabilityResult', 
				namespaces={'xmlns': \
				'http://schemas.microsoft.com/research/2009/10/webngram/frontend'})
	# TODO - also return the messageid
	return p[0].text	
	
	
	
