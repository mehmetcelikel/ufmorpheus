'''
Created on Dec 1, 2010

@author: suketukvakharia
'''
import argparse
from lxml import etree
import DataParser
import ActionObject
import urllib
import urllib2
import lxml
import lxml.html
from lxml.html import parse, make_links_absolute, fromstring
from operator import itemgetter









if __name__ == '__main__':
    
    #start the log file
    file = open('log.txt','a')
    
    
    
    parser = argparse.ArgumentParser(description="A loader for the generice qrefile",
                add_help=True)
    
    parser.add_argument("--qrefile", "-qf", required=True)
   
    args = parser.parse_args()

    #open the file up for reading
    code = open(args.qrefile,'r').read()
    
    
    
    print code
    
    assert(len(code) > 0) # Ensure text was returned

    root = etree.fromstring(code)
    
    action_list = []
    kv_hash = {}    
    kt_hash = {}
    kclass_hash = {}
    kcontext_hash = {}
    user_hash = {}
    constants = {}
    
    
    
    for child in root:
        
        
        if child.tag == 'actiondata':
           kv_hash, kt_hash, kclass_hash, kcontext_hash = DataParser.ActionDataParser(child)
           
        elif child.tag == 'userdata':
            user_hash = DataParser.UserDataParser(child)
            
        elif child.tag == 'sequence':
            for ao in child:
                if ao.tag == 'constantlink':
                    action_list.append(ActionObject.URLAction(ao))
                
                elif ao.tag == 'api':
                    
                    # load the base URL
                    base_url = ""
                    for e in ao.getchildren():
                        if e.tag == 'url':
                            base_url = e.text.strip()
                            break
                        
                    
                    request_data = {} # API params.
                    
                    
                    
                    file.write('\n\nParameters:\n')
                    
                    for e in ao.getchildren():
                        #List the Parameters first
                        
                        if e.tag == 'param':
                            
                            
                            type = kt_hash[ e.text ].strip() # Get the type
                            
                            # check if this was a parameter or userinput
                            if type == "userinput":
                                
                                # ask the user to input the value
                                input_name = e.get('name').strip()
                                userinput = raw_input("Please enter the " + input_name + '\n')
                                
                                request_data[input_name] = userinput # Add the parameter to the form data
                                file.write('\t' + input_name + ':\t\t' + userinput+'\n')
                                
                            elif type == "constant":
                            
                                v = kv_hash[ e.text ].strip() # Get the value key.
                                #state.kv_hash[e.text] = '' # FIXME why are we clearing this value??
                                input_name = e.get('name').strip()
                                request_data[input_name] = v # Add the parameter to the form data
                                file.write('\t' + input_name + ':\t\t' + v + '\n')
                                
                    
                    
                    the_page = None
                    new_base = None
                    
                    method = ao.find('method').get('type')
                    
                    if method is None or method.lower().strip() == "get":
                        action_url = base_url
                        url_values = urllib.urlencode(request_data)
                        full_url = action_url + '?' + url_values
                        response = urllib2.urlopen(full_url)
                    else:
                        # Do the POST submit
                        # We go where the action takes us
                        action_url = base_url
                        pdata = urllib.urlencode(request_data)
                        req = urllib2.Request(action_url, pdata)
                        response = urllib2.urlopen(req)
    
                    
                    the_page = response.read()
                    new_base = response.geturl()
                    print the_page

                    page = None
                    if ao.find('method').get('response') == 'json':
                        # TODO think of a way to process json responses
                        pass
                    elif ao.find('method').get('response') in ('xml','html'):
                        page = lxml.html.fromstring(the_page, base_url=new_base)
                        xp = ao.find('method').find('dataKey').text
                        xp = xp.lower().strip()
                        
                        dataElements = []
                        # get the dataElement paths
                        for e in ao.find('method').getchildren():
                            if e.tag == 'dataElement':
                                dataElements.append(e.text.lower().strip())
                        
                        xp_results = []
                        
                        xp_results = []
                        
                        def caster(x,type):
                            "casts according to what is in type"
                            if type == 'integer':
                                return int (x)
                            elif type == 'decimal':
                                return float(x)
                            else:
                                return x
                        
                        
                        def doOperation(op, list, var, sortkey):
                            """ Perform operation on the list """
                        
                            
                            func = itemgetter(sortkey)
                            
                            if op == 'max':
                                return max(list, key=func)
                            elif op == 'min':
                                return min(list, key=func)
                            elif op == 'sort asc':
                                return sorted(list, key=func)
                            elif op == 'sort desc':
                                return sorted(list, key=func, reverse=True)
                            else:
                                print 'Error: Bad input operation in doOpertion'
                                return list
                        
                        for e in page.xpath(xp):
                            xp_results.append( {ao.find('method').find('dataKey').get('name'):caster(e.text,ao.find('method').get('var'))})
                        
                        
                        for e in ao.find('method').getchildren():
                            if e.tag == 'dataElement':
                                dataElement= e.text.lower().strip()
                                for xp_result, f in zip(xp_results,page.xpath(dataElement)):
                                    xp_result[e.get('name')] = f.text
                            
                        #for e in page.xpath(xp), g in page.xpath(dataElement for dataElement in dataElements):
                            
                
                    res = xp_results
                    if ao.find('method').get('operation'):
                        var = ao.find('method').get('var')
                        res = doOperation(ao.find('method').get('operation'),xp_results,var,ao.find('method').find('dataKey').get('name'))
                    
                    #print the raw output
                    file.write('\n Raw xml output:\n\t' + the_page+ '\n\n')
                    
                    #print each result
                    if ao.find('method').get('operation') == "sort desc" or ao.find('method').get('operation') == "sort asc":
                        for e in res:
                            file.write('\nNewResult:\n')
                            for item in e:
                                file.write('\t' + item + ':\t\t' + str(e[item]) + '\n')
                    


    file.close()
                                
                                
                    
                   
                    
                    
                    
