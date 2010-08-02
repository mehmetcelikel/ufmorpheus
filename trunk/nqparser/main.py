'''
Created on May 26, 2010

@author: Joir-dan
'''
import extractor as ext


def showChoices():
    print(
	  '''
	  1) Parse Query
	  2) Exit
	  ''');

def choice1():
    #verboseMode = True if raw_input("Verbose Mode: ").lower() == "true" else False
    query = raw_input("Question: ")
    extractor = ext.TermExtractor()
    extractor.run(query, verboseMode = True)
    extractor.showResults()
    print(extractor.getExportString())
    #ext.subjectExtractor(query);
    return;

if __name__=="__main__":

	choice = "0";

	while(choice != "2"):
	    showChoices();
	    choice = raw_input("Option #: ");
	 
	    if(choice == "1"):
		choice1();
	    else:
		print("Done");
	

        
