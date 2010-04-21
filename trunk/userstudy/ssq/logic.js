/*
	Javascript file made by Christan Grant for the morpheus user study
*/

function init(){
	// Give the input boxes auto complete power
	//$('.catbox').autocomplete(acdata);
	$("input.catbox").autocomplete("category.php", {
		selectFirst: true
	});
	
	// Hide input boxes by default
	$('.catbox').hide();
	
	// Toggle input box view on double click
	$('.term').dblclick(function () {
		$(this).find('.catbox').toggle();
	});
	
	// On single click, highligh term
	$('.term').click(function() {
		$(this).toggleClass('highlight');
	});
	
	// Merge terms when the button is clicked
	$('#mergeterms').click(function() {
		var new_terms = merge_terms();
		$('#questioninput').val(new_terms);
		return true;
	});
	
	// Originale container should be droppable
	$('#question').droppable({
		drop: function(event, ui) {},
		out: function(event, ui) {},
		hoverClass: function (event, ui) {
			$(this).addClass('activeDiv');
		}
	});
	
	// Make terms dragable
	$('.term').draggable({
		revert: 'invalid', // when not dropped, the item will revert back to its initial position
		snap: true,
		snapMode: 'inner',
		snapTolerance: '20'
	});
	
	// Logic for when the terms are dropped
	$('.droppable').droppable({
		drop: function(event, ui) {
			//var index = $(ui.draggable).attr('index');
			var io = $(this).attr('id'); // Input or output panel
			$(ui.draggable).attr('io', io);
		},
		out: function(event, ui) {},
		hoverClass: function (event, ui) {
			$(this).addClass('activeDiv');
		}
	});
	
	// When you click submit do this
	$('#submitbutton').click(function(){
		// Use the inner of this function to get location of each element
		$('.term').each( function(counter){
			// Add an input element so that each of these will be added to the final
			// form submit
			create_input('hidden', counter, $(this).attr('io')).appendTo('form');
		});
		
		alert($('form').serialize());
		//return false; // <= Prevents refresh
	});
}

/**
* This creates a new hidden input elements and returns it
*/
function create_input(the_type, the_name, the_value){
	return $("<input>", {
		type: the_type,
		name: the_name,
		val: the_value,
		class: 'extra_input'
	});
}

/**
* Combine adjacent terms into a single term.
*/
function merge_terms() {
	var term_array = ""; // This is the new array for merged terms
	var full_array = ""; // This is the new question
	$('#question .term').each( function(ind) {
		if( $(this).hasClass('highlight') ){
			//term_array.push({index: $(this).attr('index'), val:$(this).attr('val')});
			var val = $(this).attr('val');
			term_array += " "+val;
			term_array = $.trim(term_array);
		}
		else{
			var val = $(this).attr('val');
			if(term_array.length == 0){
				full_array += "|"+val;
			}
			else{
				// add all the elements of the term array to the full array
				full_array += "|"+term_array;
				// add this element to the current array
				full_array += "|"+val;
				term_array = "";
			}
		}
	});
	// Put the rest of the term array in the full array
	if(term_array != ""){
		full_array += "|"+term_array;
	}
	
	full_array = full_array.substring(1);// remove the first |		
	return full_array;
}
