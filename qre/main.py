"""
This class runs the code
"""

__authors___ = ['"Christan Earl Grant" <cgrant@cise.ufl.edu>']

def main():
"""Run the Executor script
	
	Iterate once over the script xml
		if node is userdate
			Create the hash table using UserDataParser
		else if node is action data
			Create Hash table pair using ActionDataParser
		else if is the starturl
		 create action object for it and make it first element in the action_list
		else it must be sequence
			Interate through the children and populate the action object list
	
	Initialize state with the created data structures
	Call state.run()
"""
	pass

