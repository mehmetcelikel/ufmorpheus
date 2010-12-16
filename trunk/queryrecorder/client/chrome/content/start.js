
var query = null; // global variable

/*
fuunction to get the arguments from the toolbar.js
*/
function initiate() {

	try	{
	
		window.moveToAlertPosition(); // move the popup to the middle of the window
		
		query = window.arguments[0]; // makes query a global variable
		
	}
	catch(e) {
		alert("Error initiate(): " + e.message);
	}
}
/*
Returns the variable "query" to toolbar.js with the information from the user
*/
function saveQuery() {

	var q = document.getElementById('query');	
	var r = document.getElementById('realm');	
	query.name = q.value;
	query.realm = r.value;
			
	return true;
}
