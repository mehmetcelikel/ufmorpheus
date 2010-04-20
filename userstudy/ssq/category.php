#!/usr/local/bin/php

<?php
	$MAX_RESULTS = 7;
	$conn  = pg_connect('user=morpheus3 host=babylon.cise.ufl.edu dbname=sdb password=crimson03.sql');
	
	if (!$conn) { 
		echo "Connection failed";
		
		exit;
	}
	
	if ($_GET['q'] != null){
		$q = $_GET['q'];
		
		$query = 'SELECT distinct lex FROM nodes WHERE lex ILIKE '.
								'\'http://zion.cise.ufl.edu/ontology/classes#'.$q.'%\'';
		
		$result = pg_query($conn, $query);
		$counter = 0;
		while($row = pg_fetch_array($result, NULL, PGSQL_NUM)){
			
			print substr(strrchr($row[0], "#"), 1);
			print "\n";
			$counter += 1;
			if ($counter == MAX_RESULTS) break;
		}

		
	}
	else{
		// Do nothing or return error
	}

?>
