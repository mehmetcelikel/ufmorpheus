"""
        Represents a single page references with its associated form inputs and any resulting outputs
"""
__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

from ActionType import ActionType
from Page import Page

class Form(Page, object):

        def __init__(self):
		super(Form,self).__init__()
                self.formInputs = list()
		self.actionType = ActionType.Form

                pass

def New(url, dest, time, xpath, src, method, inputList):
	f = Form()	
        f.url = url
        f.destinationUrl = dest
	f.method = method
        f.timestamp = time
        f.xpath = xpath
        f.pagesrc = src
        f.formInputs = inputList
	f.actionType = ActionType.Form
	return f
	pass

if __name__=="main":
	pass
