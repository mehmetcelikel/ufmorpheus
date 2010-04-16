#!/usr/local/bin/php
<html>
	<head>
		<script type="text/javascript" src="http://code.jquery.com/jquery-latest.js"></script>
		<script type="text/javascript" src="http://dev.jquery.com/view/trunk/plugins/autocomplete/jquery.autocomplete.js"></script>
		<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.2/jquery-ui.js"></script>
		<link type="text/css" rel="stylesheet" href="style.css" />
		<script type="text/javascript" rel="javascript" src="logic.js"> </script>
		<script>
			$(document).ready(function(){
				init();
			});
		</script>
	</head>
	<body>
		<form method="post">
			
			<h3> Question 1: What size tires does a 97 Toyota Camery need? </h3>
			<h1>
				<div id="question">
						<input type="hidden" name="questionid" value="1" />
						<div index="0" class="term">What <input type="text" name="term0" size="10" class="catbox" /></div> 
						<div index="1" class="term">size <input type="text" name="term1" size="10" class="catbox" /> </div> 
						<div index="2" class="term">tires <input type="text" name="term2" size="10" class="catbox" /></div> 
						<div index="3" class="term">does <input type="text" name="term3" size="10" class="catbox" /></div> 
						<div index="4" class="term">a <input type="text" name="term4" size="10" class="catbox" /></div> 
						<div index="5" class="term">97 <input type="text" name="term5" size="10" class="catbox" /></div> 
						<div index="6" class="term">Toyota <input type="text" name="term6" size="10" class="catbox" /></div> 
						<div index="7" class="term">Camry <input type="text" name="term7" size="10" class="catbox" /></div> 
						<div index="8" class="term">need? <input type="text" name="term8" size="10" class="catbox" /></div> 
				
				</div>
			</h1>
		
			<div id="drop-panels">
		
				<div id="input-panel" class="droppable">
					<p class="panel-name">Input Terms</p>
				</div>
		
				<div id="output-panel" class="droppable">
					<p class="panel-name">Output Terms</p>
				</div>
		
				<div class="clear"></div>
			</div>
			<input type="submit" name="submitbutton" id="submitbutton" value="Next=>" />
		</form>
	</body>
</html>
