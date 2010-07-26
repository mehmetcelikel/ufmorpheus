/**
 * 
 */


import java.util.ArrayList;


import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;


/**
 * This class is build to connect to the DBpedia service using the Jena API
 * 
 * @author Clint P. George 
 *
 */
public class DBpediaConnector {

	// Constants 
	public static String NS_SKOS = "http://www.w3.org/2004/02/skos/core#";
	public static String DBPEDIA_SPARQL_SERVICE = "http://dbpedia.org/sparql";
	public static String SKOS_PREFIX = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>";
	public static String RDF_PREFIX = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
	public static String DBPEDIA_PROPERTY_PREFIX = "PREFIX property: <http://dbpedia.org/property/>";
	public static String DBPEDIA_RESOURCE_PREFIX = "PREFIX resource: <http://dbpedia.org/resource/>";
	public static String DBPEDIA_CATEGORY_PREFIX = "PREFIX category: <http://dbpedia.org/resource/Category:>";

	
	/**
	 * Builds a specific DBpedia category query 
	 * 
	 * @param term the category name  
	 */
	public Query buildCategoryQuery(String term){
		Query query = null;
		
		String qStr = RDF_PREFIX + " " + SKOS_PREFIX + " " +
            "SELECT ?subject " +
            "WHERE { <http://dbpedia.org/resource/" + term + "> skos:subject ?subject }";
		
		// Creating query object
		query = QueryFactory.create(qStr);		
		
		return query;
	}
	
	/**
	 * Builds a DBpedia category query for getting the super category  
	 * 
	 * @param category the category name  
	 */
	public Query buildDescribeQuery(String category){
		Query query = null;
		
		String qStr = RDF_PREFIX + " " + SKOS_PREFIX + " " +
		"DESCRIBE <http://dbpedia.org/resource/Category:"+ category +">";
		
		// Creating query object
		query = QueryFactory.create(qStr);		
		
		return query;
	}
	
	
	/**
	 * Builds a DBpedia category query for getting the super category  
	 * 
	 * @param term the category name  
	 */
	public Query buildBroaderQuery(String term){
		Query query = null;
		
		String qStr = RDF_PREFIX + " " + SKOS_PREFIX + " " +
            "SELECT ?broader " +
            "WHERE { <http://dbpedia.org/resource/Category:" + term + "> skos:broader ?broader }";
		
		// Creating query object
		query = QueryFactory.create(qStr);		
		
		return query;
	}
	
	/**
	 * Builds a DBpedia category query for getting the disambiguates category  
	 * 
	 * @param term the category name  
	 */
	public Query buildDissambiguatesQuery(String term){
		Query query = null;
		
		String qStr = RDF_PREFIX + " " + SKOS_PREFIX + " " + DBPEDIA_PROPERTY_PREFIX + " " +
		"SELECT ?disamb " +
        "WHERE { <http://dbpedia.org/resource/"+ term +"> property:disambiguates ?disamb }";
		
		// Creating query object
		query = QueryFactory.create(qStr);		
		
		return query;
	}
	
	/**
	 * Builds a DBpedia category query for getting the super category  
	 * 
	 * @param term the category name  
	 */
	public Query buildRedirectQuery(String term){
		Query query = null;
		
		String qStr = RDF_PREFIX + " " + SKOS_PREFIX + " " + DBPEDIA_PROPERTY_PREFIX + " " +
            "SELECT ?redirect " +
            "WHERE { <http://dbpedia.org/resource/" + term + "> property:redirect ?redirect }";
		
		// Creating query object
		query = QueryFactory.create(qStr);		
		
		return query;
	}
	
	
	/**
	 * Executes a SPARQL query 
	 * 
	 * @param SPARQL query object 
	 */
	public void executeQuery(Query query){
		
		ResultSet results = null;
		
		// Initializing query execution factory with the remote service
		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL_SERVICE, query);
		results = qexec.execSelect();
		
		ResultSetFormatter.out(System.out, results, query);

		qexec.close();

	}
	
	/**
	 * Executes the SPARQL query DESCRIBE and prints the output to standard out 
	 * 
	 * @param SPARQL query object 
	 */
	public void executeDescribe(Query query){
		
		Model mdl = null;
		
		// Initializing query execution factory with the remote service
		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL_SERVICE, query);
		mdl = qexec.execDescribe();
		
		mdl.write(System.out);

		qexec.close();

	}
	
	
	/**
	 * Gets broader categories for a given category name 
	 * 
	 * @param category category name  
	 */
	public ArrayList<String> getBroaderCategories(String category){
		
		ResultSet rs = null;
		ArrayList<String> broaderCat = new ArrayList<String>();
		Query query = this.buildBroaderQuery(category);
		
		// Initializing query execution factory with the remote service
		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL_SERVICE, query);
		rs = qexec.execSelect();
		
        while(rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            // Get broader - variable names do not include the '?' (or '$')
            RDFNode x = qs.get("broader") ;
            // Check the type of the result value
            if ( x.isURIResource()){
            	String cat = x.toString().trim().substring(x.toString().lastIndexOf("Category:") + 9);
            	if (!broaderCat.contains(cat))
            		broaderCat.add(cat);
            }
        }

        // closes the query execution object 
		qexec.close();
		
		return broaderCat;

	}
	
	/**
	 * Gets narrow categories for a given category name 
	 * 
	 * @param category category name 
	 * @return narrow categories  
	 */
	public ArrayList<String> getNarrowCategories(String category){
		
		Model mdl = null;
		Query query = this.buildDescribeQuery(category);
		ArrayList<String> narrowCat = new ArrayList<String>();
		
		// Initializing query execution factory with the remote service
		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL_SERVICE, query);
		mdl = qexec.execDescribe();

		// Filters the resources those have broader category as 'category'
		ResIterator resI = mdl.listSubjectsWithProperty(mdl.getProperty(NS_SKOS + "broader"));
		while(resI.hasNext()){
			String catName = resI.nextResource().getLocalName().trim(); // Gets the local name 
			if (catName != null && !catName.isEmpty()){
				
				// Ignores the nodes that have the same 'category' name 
				// because that node is for describing the 'category's  
				// broader categories and other RDF type specifications 
				if (!catName.equalsIgnoreCase(category))
					if (!narrowCat.contains(catName))
						narrowCat.add(catName);
			}
				
		}

		// closes the query execution object 
		qexec.close();
		
		return narrowCat;

	}
	
	
	
	
	/**
	 * Function main - This is code is written to 
	 * test the DBpedia connector 
	 * 
	 * @param args category name e.g. Basketball
	 */
	public static void main(String[] args) {

		if (args.length < 1){
			System.out.println("Invalid user input! Please enter a valid category");
			System.exit(0);
		}
		
		DBpediaConnector dpc = new DBpediaConnector();
		
		ArrayList <String> broaderCat = dpc.getBroaderCategories(args[0]);
		
		ArrayList <String> narrowCat = dpc.getNarrowCategories(args[0]);
		
		System.out.println("Broader categories for " + args[0] + "\n---------------------------------------------------------------------");
		for (String cat : broaderCat)
			System.out.println(cat);
		System.out.println("\n");
		
		System.out.println("Narrow categories for " + args[0] + "\n---------------------------------------------------------------------");
		for (String cat : narrowCat)
			System.out.println(cat);
	}

}
