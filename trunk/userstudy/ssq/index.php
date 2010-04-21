#!/usr/local/bin/php
<?php
	$question = "";
	$questionid = -1;
	if($_GET['question'] == null){
		$question = "What|size|tires|does|a|97|Toyota|Camry|need|?";
		$questionid = 1;
	}
	else{
		$question = $_GET['question'];
		$questionid = $_GET['questionid'];
	}
?>
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
				var question = "<?php echo $question; ?>";
				var questionid = <?php echo $questionid; ?>;
			});
		</script>
	</head>
	<body>
		<form method="post" action="index.php">
			<h3> 
				Question <?php echo $questionid; ?>: <?php echo implode(' ', explode('|', $question)); ?> 
			</h3>
			<h1>
				<div id="question">
						<input type="hidden" name="question" value="<?php echo $question; ?>" />
						<input type="hidden" name="questionid" value="<?php echo $questionid; ?>" />
						<?php
							$counter = 0;
							foreach(explode('|', $question) as $term){
								echo "<div index='$counter' class='term' val='$term'>$term <input type='text' name='term$counter' size='10' class='catbox' /></div>";
								$counter += 1;
							}
						?>
				</div>
			</h1>
			<input type="submit" value="Merge Terms" id="mergeterms" />
		
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
			<div class="clear"></div>
			
			<div class="helpbox">
				<p> Drag the term into what you see as an input or our put of the question </p>
				<p> Note: Double click on a term to add its category and double click to remove the category input box </p>
			</div>
			
		</form>
	</body>
</html>