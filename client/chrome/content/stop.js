/*
	----------------
	GLOBAL VARIABLES
	----------------
*/
	var query = null;
	var counter = null;
	var queryID = null;
	
/* 
	Will start the stop recording process and get arguments from toolbar.js
*/

function stopRecording() {
	try	{
	
		window.moveToAlertPosition(); // move the popup to the middle of the window
		
		query = window.arguments[0]; // makes query a global variable
		counter = window.arguments[1]; //gets the counter
		queryID = window.arguments[2]; //gets the counter
		
		//alert("Inside");
		addStatistics(query.name, query.realm, counter, queryID); 
		
		getInputs();
		
		//alert("Query name: " + query.name + " realm: " + query.realm + " counter: " + counter);
	}
	catch(e) {
		alert("Error initiate(): " + e.message);
	}
}
/*
	Adds query, realm, input counter to the pop up window
*/
function addStatistics(query, realm, counter, queryID) {
		
		p = document.getElementById("petepane");	

		var q = document.createElement("label");
		q.setAttribute('value','Query ID: ' + queryID);
		p.appendChild(q);
			
		var q = document.createElement("label");
		q.setAttribute('value','Query: ' + query);
		p.appendChild(q);
		
		var q = document.createElement("label");
		q.setAttribute('value','Realm: ' + realm);
		p.appendChild(q);
		
		var q = document.createElement("label");
		q.setAttribute('value','Inputs: ' + counter);
		p.appendChild(q);
		
}
/*
	Gets all inputs recorded by the application on this query
*/
function getInputs() {
	var data = "";
	try {
		data = "<morpheus query=\""+query.name+"\" realm=\""+query.realm+"\">";
		var i = 0;
	
		for(i=0;i<counter;i++) {
			data = data +


			readXMLInput(i);
		}
	    data = data + "</morpheus>";
		
		MCR_Log("----------------------------------new morpheus--------------------------------------------------------------------------------");
		MCR_Log(data);
	}
	catch(e) {
		alert("Error getting inputs: " + e);
	}
	
	return data;
	
}

/*
	read an xml file and return the data
*/
function readXMLInput(fileName) {
	try {
		var dirPath = navigator.preference("extensions.mcr.savePath");
		var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
		dirPath = dirPath + "\\" + queryID;
				
		file.initWithPath(dirPath);
		file.append(fileName + ".xml"); 
	
		
		var data = "";
		var fstream = Components.classes["@mozilla.org/network/file-input-stream;1"].
						createInstance(Components.interfaces.nsIFileInputStream);
		var cstream = Components.classes["@mozilla.org/intl/converter-input-stream;1"].
                        createInstance(Components.interfaces.nsIConverterInputStream);
		fstream.init(file, -1, 0, 0);
		cstream.init(fstream, "UTF-8", 0, 0); // you can use another encoding here if you wish

		let (str = {}) {
			let read = 0;
			do { 
				read = cstream.readString(0xffffffff, str); // read as much as we can and put it in str.value
				data += str.value;
			} while (read != 0);
		}
		cstream.close(); // this closes fstream
		
	
		//MCR_Log(data);
		
		//alert(data);
		
		return data;
		
		
	}
	catch(e) {
		alert("error reading xml: " + e);
	}
	
}
/*
	Send data to the server
	TODO: Block the application for reuse and restart
*/
function sendData() {
			
	var data = getInputs();
	
	
	var url = "http://localhost:8080/queryrecorderservice/rest/todo/post";
	
	var xmlhttp = new XMLHttpRequest();
		
		
	xmlhttp.overrideMimeType('text/plain');  
				
	xmlhttp.open('POST',url,true);
	
		
	
		
	xmlhttp.setRequestHeader( "Content-type", "text/plain");
	xmlhttp.setRequestHeader("Content-length", data.length);
	xmlhttp.setRequestHeader("Connection", "close");
	//alert("Inside");
	xmlhttp.send(data);
		
		
	//alert("Outside");

	
	return true;
}

/*
	Write the passed in text to the Firefox error console
*/
function MCR_Log(text) {
	var console = Components.classes["@mozilla.org/consoleservice;1"].getService(Components.interfaces.nsIConsoleService);
	if (navigator.preference("extensions.mcr.debug")) {
		console.logStringMessage(text);
	}
}
/*
$(document).ready(function() {
alert("called");
	stopRecording();
});
*/
