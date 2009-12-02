"""
	Builds the QRE script and returns it 
"""

__authors__ = ['"Chris Shields" <gatorcas@ufl.edu>']

class ScriptBuilder():
	
	code = ""

	#accepts a qrm and generates the qre script from it
	def create(self,qrm, oldqrm1, oldqrm2):

            self.code = ""     
            self.ids = list()

            #Iterate over page list 
            for p in qrm.pageList:
                
                #Handle pages with form submits
                if p.isFormSubmit:
		    writeC(p.url)
                    #writeF(p.xpath,p.url,p.pagerefid, qrm)
                
                #write out page links
                if p.links != None:
                    for l in p.links:
                        writeL(l)
        
                if p.outputs != None: 
                    for o in p.outputs: 
                        writeH(o.meetpoint)

            return self.code
		
        def writeC(url): 
            self.code += "__C__\n{{ "+url+" }}\n\n"            
            pass
 
        def writeL(xpath):
            self.code += "__L__\n{{ "+xpath+" }}\n\n"
            pass

        def writeH(xpath):
            self.code += "__H__\n{{ "+ getid() + " }}\n\n"
            pass
  
        def writeF(xpath, url, pid, qrm):
            self.code += "__F__\n{{ "+xpath+" }}\n{{ "+url+" }}\n"
             
            from Loader import Loader
            
            inputList = Loader.GetPageInputs(pid, qrm, oldqrm1, oldqrm2)

            count = 1
            NAME = 0
            TYPE = 1
            VALUE = 2

	    #TODO: Value can be a default (which is determined here), a highlight or a blank if its a constant
            
            for i in inputList:
                self.code += "PARAM_"+count+"("+i[NAME]+","+i[TYPE]+"):"+i[VALUE]
                count += 1
            
		
if __name__ == "main":
	pass
