var retVals = null; // used to get argument from toolbar.js

function setInput(){
	// This creates the popup window
	// for highlight - it simple adds a drop down selector
	// for form submision - it adds a series of labels and dropdown for each input 
	try{
		window.moveToAlertPosition(); // move the popup to the middle of the window
		
		// TODO - Switch p for a temp
		p = document.getElementById("petepane");
		
		if(window.arguments[1] == "selection"){
			retVals = window.arguments[2]; // retVals is now a Global variable
			retVals.type = "selection";
			
			var q = document.createElement("label");
			q.setAttribute('value','Highlight Class');
			p.appendChild(q);
			var z = makeDropDown();
			p.appendChild(z);
			
			// Answer Checking
			var c = document.createElement("checkbox");
			c.setAttribute('label','Answer?');
			c.setAttribute('checked','false');
			p.appendChild(c);
		}
		else if(window.arguments[1] == "form"){
			// This is a form submission or a link click
			// Assume that this is not a link click
			retVals = window.arguments[2]; // retVals is now a Global variable
			retVals.type = "form";
		
			var form_index = GetFormIndex();

			var unique_names = new Array();
			// copy the class label array
			for (var ix=0; ix !=retVals.labelarray.length; ix++){
				unique_names.push(retVals.labelarray[ix]);
			}
			unique_names = RemoveDuplicates(unique_names);
			
			MCR_Log("retVals.labelarray: " +retVals.labelarray);
			MCR_Log("retVals.param4[form_index].values: " +retVals.param4[form_index].values);
			MCR_Log("retVals.param4[form_index].names: " +retVals.param4[form_index].names);
			MCR_Log("unique_names: " +unique_names);
		
			var duplicate_step = 0; // Increment if there are duplicate names
			for(var ind = 0; ind != retVals.labelarray.length; ++ind){
				// Increment to value instead of name

				var q = document.createElement("label");
				
				var ind = Math.floor(ind);
				if(retVals.intext[ind+duplicate_step] == ""){
					// This was not a selection (such as a drop down
					// Add whatever was in the 'name' field

					// Check if this index has multiple instances (e.g. radiobutton)
					// If there are multiple instances increment value
					var name_count = GetFormNameCount(form_index, retVals.labelarray[ind]);

					MCR_Log("name_count: " + name_count);

					var diff = true;
					if (name_count == 1){
						if(retVals.param4[form_index].values[ind+duplicate_step]==retVals.labelarray[ind]){
							diff = false;
						}
						else if(retVals.param4[form_index].values[ind+duplicate_step]==""){
							diff = false;
						}		
						else{
							diff = true;
						}
					}
					else{
						// if the name_count is greater than 1
						// we assume that it is changed
												
					}
					
					if(diff){
						q.setAttribute('value',""+retVals.labelarray[ind]);
						p.appendChild(q);
						var z = makeDropDown();
						z.setAttribute('name',""+retVals.labelarray[ind]);
						p.appendChild(z);
					}
					else{
						q.setAttribute('value',"*"+retVals.labelarray[ind]);
						p.appendChild(q);
						var z = makeDropDown();
						z.setAttribute('name',"*"+retVals.labelarray[ind]);
						p.appendChild(z);
					}
					
				}
				else{
					// This input was probable a drop down
					// Add what was in the innertext of the selected item
					
					
					var diff = true;
					if(retVals.param4[form_index].values[ind+duplicate_step]==retVals.intext[ind+duplicate_step]){
						diff = false;
					}
					else if(retVals.param4[form_index].values[ind+duplicate_step]==""){
						diff = false;
					}
					else{
						diff = true;
					}
					
					if(diff){
						q.setAttribute('value',""+retVals.intext[ind+duplicate_step]);
						p.appendChild(q);
						var z = makeDropDown();
						z.setAttribute('name',""+retVals.intext[ind+duplicate_step]);
						p.appendChild(z);
					}
					else{
						q.setAttribute('value',"*"+retVals.intext[ind+duplicate_step]);
						p.appendChild(q);
						var z = makeDropDown();
						z.setAttribute('name',"*"+retVals.intext[ind+duplicate_step]);
						p.appendChild(z);
					}
				}
				
				MCR_Log("-------------------------------");
				MCR_Log("ind: " + ind);
				MCR_Log("duplicate_step: " + duplicate_step);
				

				// Make sure this is not the last element
				// Check if the next item is the same 'name' as current
				// if so, increment the index
				while( (ind+1) != retVals.labelarray.length
					&& (retVals.labelarray[ind] == retVals.param4[form_index].names[ind+duplicate_step+1])
					){
					++duplicate_step;
				}
				
				
				MCR_Log("-------------------------------");
				MCR_Log("ind: " + ind);
				MCR_Log("duplicate_step: " + duplicate_step);
				
			
				// 	TODO - for each item in p
				// if the item is a label and contains the '*' add it pete pane
				// then additionally add the next item (drop down)
				// 
				// at the end add all the items in the temp variable p into pete pane
			}
		}
	}catch(e) {
		alert("Error setInput(): " + e.message);
	}
}

function makeDropDown(){
	// Get all the context classes from the file (if available)
	try{
		var m = document.createElement("menulist");
		var mp = document.createElement("menupopup");
		
		for(var index = 0; index != retVals.param3.length-1; index++){
			var mi = document.createElement("menuitem");
			mi.setAttribute('label',retVals.param3[index]);
			
			mp.appendChild(mi);
		}
		m.appendChild(mp);
		return m;
	
	}catch(e) {
		alert("Error makeDropDown(): " + e.message);
	}
}

function SaveDialog(){
	try{
		if(retVals.type == "selection"){
			var d = document.getElementsByAttribute('selected',true);
			var attr = d.item(0).getAttributeNode('label');
			retVals.selectclass = attr.nodeValue;
		
			// Check if the check box would be true -------
			var e = document.getElementsByTagName('checkbox');	
			if(e.item(0).getAttributeNode('checked').nodeValue == 'true'){
				retVals.isAnswer = true;
			}
			else{
				retVals.isAnswer = false;
			}
		}
		else if(retVals.type == "form"){
			// Need to read what was selected
			var labelclasses = new Array();
		
			var d = document.getElementsByTagName('label');

			var lc_index = 0; // label class index
			for(var i = 0; i != d.length; ++i){
				var attr = d.item(i).getAttribute('value');
				
				// put in the name from the label class
				var c_name = retVals.labelarray[lc_index++];
				c_name = trim(c_name);
				labelclasses.push(c_name);
				
				var menulist = document.getElementsByAttribute('name',attr);
				var menupop = menulist.item(0).childNodes;
				var nme_list = menupop.item(0).childNodes;
				for(var j = 0; j != nme_list.length; ++j){
					if(nme_list.item(j).getAttribute('selected') == "true"){
						labelclasses.push(trim(nme_list.item(j).getAttribute('label')));
						break;
					}
				}
				MCR_Log("224labelclasses: " +labelclasses);
			}
			retVals.classarray = labelclasses;
		}
		
	}catch(e) {
		alert("Error SaveDialog(): " + e.message);
	}
	
	return true;
}

function GetFormIndex(){
	var current = retVals.xpath;
	try{
		for(var i = 0; i != retVals.param4.length; ++i){
			if (current == retVals.param4[i].xpath){
				return i;
			}
		}
	}catch(e){
		alert("GetFormIndex(): "+e.message);
	}
	
	return 0; // the first one (zero) is just a best guess
}

function GetFormNameCount(form_index, inputname){
	// Takes the form index and an input name and returns the about of times
	// the input name appears in the from
	var dup_count = 0;
	try{
		for (var i=0; i != retVals.param4[form_index].names.length; ++i){
			if(retVals.param4[form_index].names[i] == inputname){
				++dup_count;
			}
		}
	}catch(e){
		alert("GetFormNameCount(): "+e.message);
	}
	return dup_count;
}

function GetFormNameIndexes(inputname){
	// Takes a name and returns the indexes where this name appears
	var names = new Array();
	try{
	
		for (var i=0; i != retVals.param4[form_index].names.length; ++i){
			if(retVals.param4[form_index].names[i] == inputname){
				names.push(i);
			}
		}
	
	}catch(e){
		alert("GetFormNameCount(): "+e.message);
	}
	return names;
}


