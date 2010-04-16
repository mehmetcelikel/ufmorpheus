/*
	Javascript file made by Christan Grant for the morpheus user study
*/

function init(){
	var acdata = 'Hello This is just some sample data so that I can let people use candom categories there are more automobiles and a ton of other categories for people to uses Automobiles Music Movies Airplanes Engines Car'.split(' ');
	
	// Give the input boxes auto complete power
	$('.catbox').autocomplete(acdata);
	
	// Hide input boxes by default
	$('.catbox').hide();
	
	$('.term').dblclick(function () {
		$(this).find('.catbox').toggle();
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
		class: 'extra_input',
		});
}


