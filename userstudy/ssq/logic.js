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
		//$('.catbox').slideToggle('slow');
		$(this).find('.catbox').slideToggle('fast');
	});
	
	// Make terms dragable
	$('.term').draggable({
		revert: 'invalid', // when not dropped, the item will revert back to its initial position
	});
	
	// Logic for when the terms are dropped
	$('.droppable').droppable({
		drop: function(event, ui) {
			$(this).find('p').html('Dropped!');
			//$(this).addClass('ui-state-highlight')
		},
		out: function(event, ui) {
			$(this).find('p').html('Removed');
		},
		activeClass: 'activeDiv'
	});
		
}


