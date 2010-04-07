/*
	Javascript file made by Christan Grant for the morpheus user study
*/

function init(){
	YUI().use('dd-drag', function(Y) {
		// Selector of the node to make draggable
		var dd = new Y.DD.Drag({
			node: '#demo'
		});
	});

	YUI().use('dd-constrain', function(Y) {
	});

}
