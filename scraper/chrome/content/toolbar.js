/*
	----------------
	GLOBAL VARIABLES
	----------------
*/

var fileCounter = 0;
var dirCounter = 0;
var curDirName = null;

var form_defaults = null;

/*
	---------
	FUNCTIONS
	---------
*/

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
	Generate a partial id for a file based on the URL.
	Filters out all symbols and common substrings
*/
function MCR_GenId(){
	var id = gBrowser.selectedBrowser.contentDocument.URL;
	id = id.replace(/\?.*/g, "");
	id = id.replace(/\;.*/g, "");
	id = id.replace(/\,.*/g, "");
	id = id.replace(/\:/g, "");
	id = id.replace(/\./g, "dot");
	id = id.replace(/\//g, "");
	id = id.replace(/http/,"");
	id = id.replace(/www/,"");
	return id;
}


/*
	Enable or disable the functionality of this extension
*/
function MCR_ToggleOnOff() {
	var curEnabled = navigator.preference("extensions.mcr.enabled");
	navigator.preference("extensions.mcr.enabled", !curEnabled);
	/*
	TODO - Change the color of the on off button
	*/
}


/*
	Save data to disk in xml format
*/
function MCR_SaveData() {
	var data = MCR_GenPageInfo();
	MCR_SaveDataToFile(fileCounter + "-" + MCR_GenId(), data);
	fileCounter++;
}


/*
	This is the highlight
*/
function MCR_SaveSelected() {
	var data = MCR_GenSelectedInfo();
	MCR_SaveDataToFile(fileCounter + "-selection-" + MCR_GenId(), data);
	fileCounter++;
}


/*
	Save the page information, including information from forms
	Returns as text
	
	? Will this work to save the entire page sorce as opposed to input by input
	? Will the document be in a normalizes form if we use an input by input grabing (as performed below)
	? Will the document be in a normalized  form if we grab the whole page
	TODO - Add out a timestamp to the XML
	
	
*/
function MCR_GenPageInfo() {
	// get the html from the currently active document
	var curDoc = gBrowser.selectedBrowser.contentDocument;
	
	// get form elements from document
	var forms = curDoc.getElementsByTagName('form');
	var inps = curDoc.getElementsByTagName('input');
	var textareas = curDoc.getElementsByTagName('textarea');
	var texts = curDoc.getElementsByTagName('text');
	
	// Filter inputs so that only ones with type of text remain
	var inputs = new Array();
	for (var i=0; i<inps.length; i++) {
		/*What do undefined and '' mean? are they equivalent to text*/
		//if (inps[i].type == 'text' || inps[i].type == '' || inps[i].type == undefined) {
		//	inputs.push(inps[i]);
		//}
		if( inps[i].type != undefined && (inps[i].type.toLowerCase() == 'text' || 
				inps[i].type.toLowerCase() == 'hidden')
				){
			inputs.push(inps[i]);
		}
		/*
			Other possiblle input types:
				button
				checkbox
				file
				hidden
				image
				password
				radio
				reset
				submit
			*/
	}
	
	var result = new Array();
	var contentCount = 0;
	
	result.push("<xml>");
	
	// Note that URL need to be escaped
	result.push("<url>", escape(curDoc.URL), "</url>");
	
	// Handle elements of type <Input>
	for (var i=0; i<inputs.length; i++) {
		var itemid = MCR_Undef(inputs[i].id);
		var itemclass = MCR_Undef(inputs[i].class);
		var itemname = MCR_Undef(inputs[i].name);
		
		result.push("<input-field>");
		result.push("<id>", itemid, "</id>");
		result.push("<class>", itemclass, "</class>");
		result.push("<name>", itemname, "</name>");			
		
		var itemvalue = MCR_Undef(inputs[i].value);
		if (itemvalue != "") {
			contentCount++;
		}

		result.push("<value>", itemvalue, "</value>");
		result.push("</input-field>");
	}
	
	result.push("</xml>");
	return result.join("");
}


function MCR_SaveDataToFile(fname, data) {
	MCR_Log(fname);
	var dirPath = navigator.preference("extensions.mcr.savePath");
	
	if (curDirName != "" && curDirName != undefined && curDirName != null) {
		dirPath = dirPath + "\\" + curDirName;
	}
	
	var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
	file.initWithPath(dirPath);
	file.append(fname + ".xml");
	
	// Check for case of file existing
	if (file.exists()) {
		// TODO: Handle this case
		return
	}

	try {
		file.create(Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 420);
		var filestream = Components.classes["@mozilla.org/network/file-output-stream;1"]
			.createInstance(Components.interfaces.nsIFileOutputStream);
		filestream.init(file, 0x04|0x08|0x20, 420, 0);
		filestream.write(data, data.length);
		filestream.close();
	} catch (e) {MCR_Log("Failure saving file " + file.path);}
}


/*
	Generate the target information if there is a selection
	

	TODO - Add time stamp to XML
		 - Currently only a ';' separated list from the end of the selected node to the parent is being saved
		 - This solution should produce the complete xpath to the opening node containing the highlighted data.  Additionally, we store the character index offset of the begining of the highlighted item .  The initial index is the first index of the highlited item. The ending offset is one past the last actually included index.
	
*/
function MCR_GenSelectedInfo() {
	var sel = gBrowser.contentWindow.getSelection();
	var selStartNode = sel.anchorNode;
	var selEndNode = sel.focusNode;
	var selStartOffset = sel.anchorOffset;
	var selEndOffset = sel.focusOffset;
	
	var result = new Array();
	result.push("<xml><selection-start>");
	result.push("<offset>", selStartOffset, "</offset>");
	result.push("<path>");
	
	// Start node of the selection 
	var curNode = selStartNode;
	while (true) {
		result.push(curNode.nodeName,";");
		curNode = curNode.parentNode;
		if (curNode == undefined || curNode == null) {
			break;
		}
	}
	result.push("</path></selection-start>");
	
	// End node of the selection
	result.push("<selection-end>");
	result.push("<offset>", selEndOffset, "</offset>");
	if (selEndNode != undefined || selEndNode != null) {
		result.push("<path>");
		curNode = selEndNode
		while (true) {
			result.push(curNode.nodeName, ";");
			curNode = curNode.parentNode;
			if (curNode == undefined || curNode == null) {
				break;
			}
		}
		result.push("</path>");
	}
	result.push("</selection-end></xml>");
	
	return result.join("");
}

function SaveSelected() {
	//window.alert("SaveSelected()");
	var r = recordPopup(data, "selection");
	var data = GenSelectedInfo(r);
	MCR_SaveDataToFile(fileCounter + "-selection-" + MCR_GenId(), data);
	fileCounter++;
}

/**
* Saves data to a file
* @author cgrant
*/
function SaveData() {
	MCR_Log( this.nodeName );
	var data = "";
	if(this.nodeName.toLowerCase() == 'form'){
		
		try{
			var g = GetContexClasses();
	
			var inner_textarray = new Array(); // Used if innertext is available
			var labelarray = new Array(); // used for labels
		
			var inputs = this.elements;
			
			for (var i = 0; i != inputs.length; ++i){
				if (inputs[i].type == undefined)
					alert(inputs[i])	
				if( inputs[i].type.toLowerCase() == 'text' && 
					inputs[i].type.toLowerCase() == 'hidden'){
					// Don't remember why i needed this so I added the false statement
					continue;
				}
				else if (inputs[i].type.toLowerCase() == 'submit'){
					continue;
				}
				//else {	
					labelarray.push( inputs[i].getAttribute('name') );
							
					// The inner text
					if(inputs[i].selectedIndex === undefined ||
						inputs[i].selectedIndex == null){
						// This is not a selction drop down
					
						inner_textarray.push(inputs[i].value);
					}
					else{
						var selection_index = inputs[i].selectedIndex ;
						var theitem = inputs[i].options.item(selection_index);
						inner_textarray.push(theitem.innerHTML);
					}
				//}
			}
			// Remove array duplicate (for cases such as radio buttons)
			labelarray = RemoveDuplicates(labelarray);
		
			// Note: retvals is a call back
			var retVals = { param1: null, 
							param2: null, 
							param3: g, // Contex classes 
							param4: form_defaults, // for checking what was selected
							type: null, // The type, 'selection' or 'form'
							selectclass: null, 
							labelarray: labelarray, // all the name attributes fof the input array
							classarray:null, // returns an array of node names and the classes
							isAnswer: false,
							intext: inner_textarray, // This is all the possible inner text of a selection
													// if the item 1. has inter text and 2. its value may be '---' 
													// if no selection is available	
							xpath: GetXPath(this)
						}; 
			window.openDialog("chrome://mcr/content/popup.xul","Pete's Popup","chrome,modal",
				"dummy", // [0]
				"form", // [1] - this is not a slection
				retVals); // [2]

			// form element
			data = "<morpheus>\n";
			data += "  <type> form </type>\n";
			data += "  " + GetURLXML(this);
			data += "  " + GetTimestampXML();
			data += "  <xpath>"+GetXPath(this)+"</xpath>\n";
			data += "  <inputlist>" + escape(retVals.classarray) + "</inputlist>\n";
			data += "  <node class=\"form\">" + PrintNode(this) +"</node>\n";
			data += "  " + GetPageSource(this);
			data += "</morpheus>";
		}catch(e) {alert('Error Saving the data: '+ e.lineNumber+':' + e.message + '(' +e.fileName +')')}
	}
	else{
		// Link clicked
		data = "<morpheus>\n";
		data += "  <type> link </type>\n";
		data += "  " + GetURLXML(this);
		data += "  " + GetTimestampXML();
		data += "  <label>";
		data += "  " + escape(this.innerHTML);
		data += "  </label>\n";
		data += "  <node class=\"link\">"+PrintNode(this)+"</node>\n";
		data += "  <xpath>"+GetXPath(this)+"</xpath>\n";
		data += "  " + GetPageSource(this);
		data += "</morpheus>";
	}
	MCR_SaveDataToFile(fileCounter + "-" + MCR_GenId(), data);
	fileCounter++;
}

function PrintNode(fnode){
	// FIXME
	var str = "";
	str +=  "<" + fnode.localName + " ";
		
	var attribute_list = fnode.attributes;
	for(var i=0; i != attribute_list.length; ++i){
		str += attribute_list[i].name + "=\""+ attribute_list[i].value +"\" ";
	}
	str += ">";
	str += fnode.innerHTML;
	str += "</" + fnode.localName + ">";
	//alert(str);
	return escape(str);
}

/**
Returns: 
<xpath type="anchor"> $xpath_string_to_outermost_tag_of_anchor_element </xpath>
 <xpath type="focus"> $xpath_string_to_outermost_tag_of_focus_element </xpath>
<start> $index_of_the_first_character_selected_from_opening_node </start>
  <end> $index_from_opening_node_to_end_character </end>
  <selection> $html_string_of_highlighted_information </selection>
  
  Anchor and focus here are defined by: https://developer.mozilla.org/en/DOM/Selection#Methods
  * @author cgrant
  */
function GenSelectedInfo(r){
	var ipclass = r.selectclass;
	var root = gBrowser.selectedBrowser.contentDocument;
	var sel = gBrowser.contentWindow.getSelection();
	var anchor = sel.anchorNode;
	var offset = sel.focusNode;
	var selStartOffset = sel.anchorOffset;
	// Returns the number of characters that the selection's focus is offset within the focusNode.
	var selEndOffset = sel.focusOffset;
	
	var result = "<morpheus>\n";
	result += "  <type> highlight </type>\n";
	result += "  " + GetTimestampXML();
	result += "  " + GetURLXML(anchor);	


	var start, end;
	if(isFirst(anchor,offset,root)){
		// The anchor is indeed first
		start = anchor;
		end = offset;
	}
	else{
		// The highlight was done backwards
		start = offset;
		end = anchor;

		var tmp = selStartOffset;
		selStartOffset = selEndOffset;
		selEndOffset = tmp;
	}
	var meet_node = GetMeetpoint(start, end, root);
	var meetxpath = GetXPath(meet_node);

	result += "  <meetpoint> "+meetxpath+ "</meetpoint>\n";
	result += "  <xpath type=\"anchor\">"+GetXPath(start)+"</xpath>\n";
	result += "  <xpath type=\"focus\">"+GetXPath(end)+"</xpath>\n";
	result += "  <start>"+selStartOffset+"</start>\n";
	result += "  <end>"+selEndOffset+"</end>\n";
	
	result += "  <answer>"+r.isAnswer+"</answer>\n";
	result += "  <selection>"+ escape(sel.toString())+"</selection>\n";
	ipclass = trim(ipclass);
	result += "  <class>"+escape(ipclass)+"</class>\n";
	var pgsrc = GetPageSource(content.document.documentElement);
	result += "  " + pgsrc;
	result += "</morpheus>";
	return result;
}

/**
	Custom trim function to remove surrounding white space
*/
function trim(s){
		if(s == null)
			return "";
    return s.replace(/^\s*(.*?)\s*$/,"$1");  
}



/**
* Traverse the tree starting at root (third param) and if node1
* occurs first, then true is returned. otherwise false is returned
**/
function isFirst(node1, node2, root){
try{
	var i;
	var runner;
	var stack = new Array();
	stack.push(root);
	
	while(stack.length != 0){
		runner = stack.pop();
		if(runner == node1){
			return true;
		}
		else if(runner == node2){
			return false;
		}
		else if(runner.nodeName == "#text") {
			continue; // leaf node
		}
		else{
			var temp_stack = new Array();
			for(var i=0; runner.childNodes.item(i); i++) {
				temp_stack.push(runner.childNodes.item(i));
			}
			while(temp_stack.length != 0){
				stack.push(temp_stack.pop());
			}
		}
	}
}catch(err){
	alert(err.description);
}
	return false;
}

/**
* This function takes two nodes and finds the lowest common ancestor
* in the tree. If the nodes are the same it returns the parent
* This is a bad implementation of the LCA problem
**/
function GetMeetpoint(node1, node2, root){
try{
	var ret_node;
	if(node1 == node2){
		return node1.parentNode;
	}

	var n1 = new Array();
	var n2 = new Array();

	// We make the path of the node to the root an array
	var runner = node1;
	if(node1.nodeName == "#text"){
		runner == node1.parentNode;
	}
	while(runner != root){
		n1.push(runner);
		runner = runner.parentNode;
		if(runner == node2){
			return runner; // node2 is an ancestor of node2
		}
	}

	var runner2 = node2;
	if(node2.nodeName == "#text"){
		runner2 == node2.parentNode;
	}
	while(runner2 != root){
		n2.push(runner2);
		runner2 = runner2.parentNode;
		if(runner2 == node1){
			return runner2; // node1 is an ancestor of node2
		}
	}

	for(var i = 0; i < n1.length; ++i){
		for(var j = 0; j < n2.length; ++j){
			if(n1[i] == n2[j]){
				ret_node = n1[i];
				return ret_node;
			}
		}
	}
}catch(err){
	alert(err.description);
}
	// no solution found
	return root;

}

/**
  This funtion gets the current page source and returns the page source
  wrapped in the appropriate tagsd.  It gets the source from  starting from 
  the passed in node
  i.e. '<page> $page_source </page>' where $page_source is the actual page 
  source of the page
* @author cgrant
*/
function GetPageSource(node){

	try{
	var root = gBrowser.selectedBrowser.contentDocument;
	var d = node.ownerDocument;
	var html = d.documentElement;
	return "<page>%3Chtml%3E"+escape(html.innerHTML)
	       +"%3C/html%3E"
		   +"</page>\n";
	}catch(e) {alert('Error GetPageSource: '+ e.lineNumber+':' + e.message + '(' +e.fileName +')')}
}

/**
* This returns a time stamp element for the XML
*  i.e. '<time> 231231231 </time>
 * The format of the time is the current time in miliseconds
*    @author cgrant
  */
function GetTimestampXML(){
	var dateVar = new Date()
	return "<time>"+dateVar.getTime()+"</time>\n";
}

function GetURLXML(node){
	//var d = node.ownerDocument;
	var curDoc = gBrowser.selectedBrowser.contentDocument;
	return "<url>"+escape(curDoc.URL)+"</url>\n";
}

/**
 * This Produces an XPath statement.  It takes a node and recursivly finds its
* index among its like siblings  and produces the statement
* It returns an xpath string
  @author cgrant
 **/
function GetXPath(currNode){
	var path = "";
	
	// We are at the top of the html document return the empty string
	// This is the base case of the function
	if(currNode == undefined || currNode == null || currNode.ownerDocument == null ){
		return path;
	} else if(currNode.parentNode == undefined || currNode.parentNode == null || currNode == document.documentElement || currNode.nodeName.toLowerCase() == "HTML".toLowerCase()){
		// If this node has no parent just return it
		return "/"+currNode.nodeName;
	}
		
	// Find out if this node has siblings of the same name
	// Count only the ones before this one.
	var found_node = false;
	var count_index = 0;
	var index = 0;
	var _parent = currNode.parentNode;
	var children = _parent.childNodes;
	for(var i=0; i < children.length; ++i){
		if(children[i] == currNode){
			found_node = true;
			++count_index;
		}
		if(children[i].nodeName == currNode.nodeName){
			if(!found_node){
				++index;
			}
			++count_index;
		}
	}
	// Check to see if we need to add hte index of this element
	// i.e. either /p[3] or /p
	// if count_index == 1, 
	//   this means that this was the only node of this type
	//   do not add the [1] index to the end of it.
	if(currNode.nodeName.match("#text") == null || count_index != 1 ){
		path = "/"+currNode.nodeName+"["+(index+1)+"]";
	}
	else{
		path = "/"+currNode.nodeName;
	}

	//path = path.replace('#text','text()');	
	
	// Recursively call this function to get the information about the parent
	return GetXPath(currNode.parentNode) + path;
	
}

/**
* This adds listeners to the forms, <a> , and the entire DOM
* @author cgrant
*/
function AddListeners(doc){
	// Add listeners on forms
	var forms = doc.getElementsByTagName('form');
	for (var i = 0; i < forms.length; i++) {
		forms[i].addEventListener("submit", SaveData, false);
	}
	MCR_Log("Added form listeners");
	
	// Add listeners on <a>
	var a = doc.getElementsByTagName('a');
	for (var i = 0; i < a.length; i++) {
		a[i].addEventListener('click', SaveData, false);
	}
	MCR_Log("Added link listeners");
	
	try{
		// save the default values for all form elements so we know what changes
		form_defaults = null;
		form_defaults = new Array(); // a global variable
		
		for (var i = 0; i < forms.length; i++) {
			var temp = new FormDefault( );
			temp.name = forms[i].name;
			temp.xpath = GetXPath(forms[i]);
			
			for(var j = 0; j < forms[i].elements.length; ++j){
				temp.addName(forms[i].elements[j].name);
				temp.addValue(forms[i].elements[j].value );
				//MCR_Log("["forms[i].elements[j].name+"][" + forms[i].elements[j].value+"]");
			}
			form_defaults.push(temp);
		}
		MCR_Log("Saved form defaults");
	}catch(e) {
		alert("Error form_defaults(): " + e.lineNumber+':' + e.message + '(' +e.fileName +')');
	}
}

/* 
	Read in a definition file and display it in toolbar
	Returns the data from the read file
	
	TODO - Why is this here?  Is anybody using it?
*/
function MCR_ReadDefFile(fname) {
	//TEMP
	var fname = "read1";

	var dirName = navigator.preference("extensions.mcr.loadPath");
	var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
	file.initWithPath(dirName);
	file.append(fname + ".txt");

	var fdata = "";
	
	if (file.exists()) {
		var fstream = Components.classes["@mozilla.org/network/file-input-stream;1"]
			.createInstance(Components.interfaces.nsIFileInputStream);
		var sstream = Components.classes["@mozilla.org/scriptableinputstream;1"]
			.createInstance(Components.interfaces.nsIScriptableInputStream);
			
		try {
			fstream.init(file, -1, 0, 0);
			sstream.init(fstream); 
			var str = sstream.read(4096);
			while (str.length > 0) {
				fdata += str;
				str = sstream.read(4096);
			}
		} catch(e) {MCR_Log("Exception while reading file stream: " + exp,1);}
		
		sstream.close();
		fstream.close();
	}
	
	return fdata;
}


function recordPopup(data,isaselection){
	
	var g = GetContexClasses();
	var retVals = { param1: null, 
									param2: null, 
									param3: g, 
									param4: form_defaults, // for checking what was selected
									type: null, 
									selectclass: null,
									isAnswer: false 
								};
	window.openDialog("chrome://mcr/content/popup.xul","Pete's Popup","chrome,modal",
		"dummy", // [0]
		isaselection, // [1]
		retVals); // [2]
	
	//window.open(data);
	return retVals;
}

/**
* Returns an array of the context classes in the db. i.e. [Concert,venue,Musician]
*/
function GetContexClasses(){

	// Read from the file and create an array of words
	var dirName = navigator.preference("extensions.mcrdb.contextclass")	
	var fname = navigator.preference("extensions.mcrdb.contextclassfile");
	
	var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
	file.initWithPath(dirName);
	file.append(fname);	

	var fdata = new Array(); // This is the array of context classes
	if (file.exists()) {
		var fstream = Components.classes["@mozilla.org/network/file-input-stream;1"].createInstance(Components.interfaces.nsIFileInputStream);
		var sstream = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(Components.interfaces.nsIScriptableInputStream);
		
		try{
			fstream.init(file, -1, 0, 0);
			sstream.init(fstream);

			var full_file = sstream.read(sstream.available());
			fdata = full_file.split('\n');
		}catch(e) {MCR_Log("Exception while reading file stream: " + exp,1);}	
		sstream.close();
		fstream.close();
	}	
	
	if(fdata.length == 0){
		fdata.push("Event");
		fdata.push("Concert");
	}
	
	return fdata;	

}

/**
* Adds the new class entered into the text box
*/
function AddClass(){
	var query = document.getElementById("MCR-Queries");
	var newClass = "";
	if (query == "" || query == null || query == undefined) {
		return;
	}
	else{
		newClass = trim(query.value) + "\n";
		window.alert("New Class '" + query.value + "' created!");
	}
	
	
	// Read from the file and create an array of words
	var dirName = navigator.preference("extensions.mcrdb.contextclass")	
	var fname = navigator.preference("extensions.mcrdb.contextclassfile");
	
	var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
	file.initWithPath(dirName);
	file.append(fname);	
	
	var foStream = Components.classes["@mozilla.org/network/file-output-stream;1"].createInstance(Components.interfaces.nsIFileOutputStream);

	if ( file.exists() == false ) {
		alert("File does not exist, cannot add Class " + newClass);
	}

	foStream.init(file, 0x02 | 0x10, 00666, 0);
	foStream.write(newClass, newClass.length);

	foStream.close();	
}


/**
* This is a class used to keep track of the default inputs to a form
*/
function FormDefault( ){
	this.xpath = "";
	this.names = new Array();
	this.values = new Array();
	
	this.addName = function(x) {
		this.names.push(x);
	}
	
	this.addValue = function(x) {
		this.values.push(x);
	}
}


/**
* This function takes a DOM node
* and returns a deep copy of the 
* node but all 'tail' elements
* will be wrapped with the tag 
* from param 2
* @author Christan Grant
**/
function fixTails(node, tag){
	
	// To be a tail the size muse bt 
	if (node.childNodes.length < 1){
		return node;
	}

	// Check if this node has any text children
	// if so, check if the text inputs are blank
	// if not, recursively run fix tails on the
	// child
	for(var i = 1; i < node.childNodes.length; ++i){
		if(node.childNodes[i].nodeType == 3){
			// this is a tail node
			var val = node.childNodes[i].data;
			if(isWhitespaceOrEmpty(val)){
				continue;
			} 
			var txt = node.ownerDocument.createElement(tag);
			txt.nodeValue = val;
			node.childNodes[i] = txt;
		}
		else{
			node.childNodes[i] = fixTails(node.childNodes[i]);
		}
	}

	return node;
}

function isWhitespaceOrEmpty(text) {
   return !/[^\s]/.test(text);
}

function RemoveDuplicates(arr){
    //get sorted array as input and returns the same array without duplicates.
  var result=new Array();
  var lastValue="";
	for (var i=0; i<arr.length; i++){
 	  var curValue=arr[i];
 	  if (curValue != lastValue){
			result[result.length] = curValue;
		}
		lastValue=curValue;
  }
	return result;
}

/*
	Perform setup functions on load
	
	-  This method is called for instatiation and sets up shop
*/
function MCR_OnLoad() {
	var profile = Components.classes["@mozilla.org/file/directory_service;1"]
		.getService(Components.interfaces.nsIProperties)
		.get("ProfD", Components.interfaces.nsIFile);
	var profile_mcrdb = Components.classes["@mozilla.org/file/directory_service;1"]
		.getService(Components.interfaces.nsIProperties)
		.get("ProfD", Components.interfaces.nsIFile);
		
	try {
		// If the path does not contain MCR, then insert it
		if (profile.path.indexOf("MCR") == -1) {
			profile.append("MCR");
		}
		
		if(profile_mcrdb.path.indexOf("mcrdb") == -1){
			profile_mcrdb.append("mcrdb");
		}
		



		netscape.security.PrivilegeManager.enablePrivilege ('UniversalPreferencesWrite');
		navigator.preference("extensions.mcr.savePath", String(profile.path));
		navigator.preference("extensions.mcr.loadPath", String(profile.path));
		navigator.preference("extensions.mcrdb.contextclass", String(profile_mcrdb.path));
		
		// if directory doesn't exist, create
		if(!profile.exists() || !profile.isDirectory() ) {	
			profile.create(Components.interfaces.nsIFile.DIRECTORY_TYPE, 0777);
		}
		if(!profile_mcrdb.exists() || !profile_mcrdb.isDirectory() ) {	
			profile_mcrdb.create(Components.interfaces.nsIFile.DIRECTORY_TYPE, 0777);
		}
		
	} catch(e) {MCR_Log ("Exception while creating data folder");}
}


/*
	Installs listeners
	
	? Can we identiy the actual item that caused a change (we already know we can see form submission
	? Can we print out the page every time the DOM as a whole is changed.
		And maybe also do some deduplications?
	TODO - We need to add listeners including: 
			- onclick => anchor (<a href>)
			- onchange => <select>, 
			- others ???
*/
function MCR_AddListeners(doc) {
	var forms = doc.getElementsByTagName('form');
	
	for (var i = 0; i < forms.length; i++) {
		forms[i].addEventListener("submit", MCR_SaveData, false);
		MCR_Log("Added event listener for form: name=" + forms[i].name + "  id=" + forms[i].id + " on "  + doc.location.href,1);
	}
}


/*
	Utility function which takes in a string val,
	and returns "" instead of undefined
*/
function MCR_Undef(val) { 
	if(val == undefined || val == "undefined") {
		val = "";
	}
	return val;
}


/*
	Get the new query from textbox on toolbar
*/
function MCR_GetNewQuery() {
	var query = document.getElementById("MCR-Queries");
	if (query != "" && query != null && query != undefined) {
		curDirName = query.value;
		MCR_ResetFileCounter();
		window.alert("New query created!");
	}
}


/*
	Reset query to nothing
*/
function MCR_ResetQuery() {
	var query = document.getElementById("MCR-Queries");
	query.value = "";
	curDirName = null;
	MCR_ResetFileCounter();
	window.alert("Query name reset!");
}


/*
*/
function MCR_ResetFileCounter() {
	fileCounter = 0;
}

/*
	Load the file with different queries
	
	TODO - this loads a query from a 'manifest-like' file
		 - this should not be needed
*/
function MCR_LoadDef() {
	var file = mcr.readDefFile();
	var data = file.split('\n');
	
	var queries = document.getElementById("MCR-Queries");
	queries.removeAllItems();
	for (var i = 0; i < data.length; i++) {
		queries.appendItem(data[i]);
	}
}



/*
	--------------------------------
	INSTANTIATION AND INITIALIZATION
	--------------------------------
*/
MCR_OnLoad();

// Add listeners
//window.addEventListener("load", function (event) { 
//	gBrowser.addEventListener("load", function(event) { 
//		MCR_AddListeners(gBrowser.selectedBrowser.contentDocument); }, true); 
//	}, false);
window.addEventListener("load", function (event) { 
	gBrowser.addEventListener("load", function(event) { 
		if(navigator.preference("extensions.mcr.enabled")){
			AddListeners(gBrowser.selectedBrowser.contentDocument);
		} 
	}, true); 
}, false);
