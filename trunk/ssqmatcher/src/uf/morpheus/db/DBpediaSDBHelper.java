/**
 * 
 */
package uf.morpheus.db;


import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.Store;

import uf.morpheus.meta.Constants;
import uf.morpheus.meta.MessageLogger;

/**
 * This class is a helper class to build the category library 
 * from the DBpedia RDF triples. It uses Jena API and  
 * DBpedia SPARQL query service. 
 * 
 *
 */

public class DBpediaSDBHelper {

	/**
	 * Sub class to handle the broader nodes in the DBpedia
	 * 
	 */
	public class BTNode implements Comparable<BTNode> {

		public Resource Category = null;
		public String BroaderCategory = null;
		public int BroaderLevel = 0;

		public BTNode(Resource category, String broaderCategory, int level) {
			this.Category = category;
			this.BroaderCategory = broaderCategory;
			this.BroaderLevel = level;
		}

		@Override
		public int compareTo(BTNode o) {
			return this.Category.toString().compareTo(o.Category.toString());
		}

	}

	// Class members 
	private int categoryClassCount = 0;
	private int axiomsCount = 0;
	private MessageLogger msg = MessageLogger.getInstance();
	private long treeHeight = 0; // This should be set to zero in every hierarchy creation 
	
	private Model model = null; 
	private Property subClassPty = null;
	private Property typePty = null; 
	private Resource owlClass = null; 
	private Resource rootCategory = null;
	private DBpediaConnector dbpC = null;
	private Store store = null;


	/**
	 * Constructor 
	 */
	
	public DBpediaSDBHelper(){
		dbpC = new DBpediaConnector();
	}
	
	
	/**
	 * Creates the DBpedia category library based 
	 * on the given ontology URI and physical path  
	 * 
	 * @param categories the category names   
	 * @param broadenLimit limit to which the category 
	 * 		  to be retrieved from DBpedia   
	 */
	public void createCategoryHierarchy(
			ArrayList <String> categories, 
			int broadenLimit, 
			boolean bfs, 
			boolean addNarrowCategories) 
	{
		
		try {
			long starttime = System.currentTimeMillis();

			// Gets the SDB store 
			store = SDBHelper.getStore();
			
			// Gets the default model 
			// Note: the triples will be stored in the triples db 
	    	model = SDBHelper.getDBModel(store);
			
			categoryClassCount = 0;
			axiomsCount = 0;
			treeHeight = 0;
			
			// Adds the root category 
			subClassPty = model.createProperty(Constants.NS_RDFS, "subClassOf");
			typePty = model.createProperty(Constants.NS_RDF, "type"); // Creates the type  
			owlClass = model.createResource(Constants.NS_OWL + "Class"); // Creates the OWL class rs  
			rootCategory = addCategory(Constants.DBPEDIA_ROOT_CLASS_NAME);
			
			
			for (String category : categories){
				if (bfs)
					addBFSCategoryHierarchicalClasses(category, broadenLimit, addNarrowCategories);
				else 
					addDFSCategoryHierarchicalClasses(category, broadenLimit, addNarrowCategories);
			}

			// Adds the tree height 
			addCategoryHierarchyHeight();

			StringBuilder sb = new StringBuilder();
			sb.append("REPORT\n---------------------------------------------------------\n");
			sb.append("The ontology class hierarchy has been created!\n");
			sb.append("Total number of category classes added: " + categoryClassCount + "\n");
			sb.append("Total number of axioms added: " + axiomsCount + "\n");
			sb.append("Execution time: " + (System.currentTimeMillis() - starttime)/1000 + "s.");
			msg.logger.info(sb.toString());
			
			
			// Closes the connections 
			model.close();
			SDBHelper.closeStore();
			
		} catch (Exception e) {
			msg.logger.severe("Exception: " + e.getMessage());
		}
	}
	
	/**
	 * Adds the category hierachy's tree height 
	 * 
	 * @param rootC ontology root class   
	 * @throws OWLOntologyChangeException 
	 * 
	 */
	private void addCategoryHierarchyHeight() {
		Property th = model.createProperty(Constants.NS_PROPERTIES,
				Constants.DBPEDIA_PROPERTY_TREE_HEIGHT);

		// Adds the tree Height to the root class
		rootCategory.addProperty(th, String.valueOf(treeHeight));

		axiomsCount++;

		msg.logger.info("Added the ontology tree height:" + treeHeight);
	}
	
	
	

	/**
	 * Adds the DBpedia categories to the OWL Ontology   
	 * 
	 * @param category the category name  
	 * @param broadenLimit limit to which the category 
	 * 		  to be retrieved from DBpedia   
	 */
	private void addBFSCategoryHierarchicalClasses(
			String category, 
			int broadenLimit, 
			boolean addNarrowCategories) {

		// Firstly, add the category 
		Resource categoryC = addCategory(category);
		
		// Adds narrow categories
		if (addNarrowCategories) {
			msg.logger.info("Adding narrow categories for " + category + "\n");
			
			ArrayList<String> narrowCat = dbpC.getNarrowCategories(category);
			Resource narrowC = null;

			for (String cat : narrowCat) {
				narrowC = addCategory(cat);
				addSubClassProperty(narrowC, categoryC); // Adds the sub class axiom  
			}
		}
		
		// Forms hierarchy 
		msg.logger.info("\nAdding broader categories (BFS)\n");
		
		ArrayList <String> broaderCat = dbpC.getBroaderCategories(category);
		Queue<BTNode> broaderQ = new PriorityQueue<BTNode>();
		for (String cat : broaderCat)
			broaderQ.add(new BTNode(categoryC, cat, 1));
		
		Resource broaderC = null;
		BTNode broaderNode = null;
		
		while(!broaderQ.isEmpty()){
			broaderNode = broaderQ.remove();
			
			if (broaderNode.BroaderLevel > broadenLimit){
				treeHeight = broaderNode.BroaderLevel;
				addSubClassProperty(broaderNode.Category, rootCategory); // Adds the sub class axiom  
				continue;
			}

			// Checks whether there exists an 
			if (isExists(broaderNode.BroaderCategory)){
				broaderC = getCategory(broaderNode.BroaderCategory);
			}
			// Adds the broader category 
			else {
				broaderC = addCategory(broaderNode.BroaderCategory);
				ArrayList <String> broaderCatSet = dbpC.getBroaderCategories(broaderNode.BroaderCategory);	
				
				if (broaderCatSet.size() == 0){
					addSubClassProperty(broaderC, rootCategory); // Adds the sub class axiom  
					if (treeHeight < broaderNode.BroaderLevel) // This code to find the maximum height of the tree
						treeHeight = broaderNode.BroaderLevel;
				}
				else {
					for (String broaderCatStr : broaderCatSet)
						broaderQ.add(new BTNode(broaderC, broaderCatStr, broaderNode.BroaderLevel + 1));
				}
			}
			addSubClassProperty(broaderNode.Category, broaderC); // Adds the sub class axiom
		}

	}
	
	/**
	 * Adds the DBpedia categories to the OWL Ontology   
	 * 
	 * @param category the category name  
	 * @param broadenLimit limit to which the category 
	 * 		  to be retrieved from DBpedia   
	 */
	private void addDFSCategoryHierarchicalClasses(
			String category, 
			int broadenLimit, 
			boolean addNarrowCategories) {

		// Firstly, add the category 
		Resource categoryC = addCategory(category);
		
		// Adds narrow categories
		if (addNarrowCategories) {
			msg.logger.info("Adding narrow categories for " + category + "\n");
			
			ArrayList<String> narrowCat = dbpC.getNarrowCategories(category);
			Resource narrowC = null;

			for (String cat : narrowCat) {
				narrowC = addCategory(cat);
				addSubClassProperty(narrowC, categoryC); // Adds the sub class axiom  
			}
		}
		
		// Forms the hierarchy 
		msg.logger.info("\nAdding broader categories (DFS)\n");
		
		ArrayList <String> broaderCat = dbpC.getBroaderCategories(category);
		Stack<BTNode> broaderS = new Stack<BTNode>();
		for (String cat : broaderCat)
			broaderS.add(new BTNode(categoryC, cat, 1));
		
		Resource broaderC = null;
		BTNode broaderNode = null;
		
		while(!broaderS.isEmpty()){
			broaderNode = broaderS.pop();
			
			if (broaderNode.BroaderLevel > broadenLimit){
				treeHeight = broaderNode.BroaderLevel;
				addSubClassProperty(broaderNode.Category, rootCategory); // Adds the sub class axiom  
				continue;
			}

			// Checks whether there exists an 
			if (isExists(broaderNode.BroaderCategory)){
				broaderC = getCategory(broaderNode.BroaderCategory);
			}
			// Adds the broader category 
			else {
				broaderC = addCategory(broaderNode.BroaderCategory);
				ArrayList <String> broaderCatSet = dbpC.getBroaderCategories(broaderNode.BroaderCategory);	
				
				if (broaderCatSet.size() == 0){
					addSubClassProperty(broaderC, rootCategory); // Adds the sub class axiom  
					if (treeHeight < broaderNode.BroaderLevel) // This code to find the maximum height of the tree
						treeHeight = broaderNode.BroaderLevel;
				}
				else {
					for (String broaderCatStr : broaderCatSet)
						broaderS.add(new BTNode(broaderC, broaderCatStr, broaderNode.BroaderLevel + 1));
				}
			}
			addSubClassProperty(broaderNode.Category, broaderC); // Adds the sub class axiom
		}

	}

	
	
	/**
	 * Adds resource to the RDF ontology
	 * 
	 * @param category
	 *            resource name
	 */
	private Resource addCategory(String name) {

		Resource oClass = null;

		try {

			oClass = model.createResource(Constants.NS_CLASSES + name);
			oClass.addProperty(typePty, owlClass); // Adds the triple for the
													// type OWLClass
			categoryClassCount++;

			msg.logger.info("Added the category: " + name + "\n");

		} catch (Exception e) {

			msg.logger.severe("Exception in adding a new category : "
					+ e.getMessage());
			System.exit(1);
		}

		return oClass;
	}
	
	/**
	 * Adds sub class property     
	 * 
	 */
	private void addSubClassProperty(Resource category, Resource superCategory){
		
		try {
			
			category.addProperty(subClassPty, superCategory); // Adds the sub class axiom
			axiomsCount++;
			
			msg.logger.info("Added the axiom: " + category + " <-- " + superCategory);
		
		} catch (Exception e) {
			msg.logger.severe("Exception in adding sub class property: " + e.getMessage());
			System.exit(1);
		}

	}
	
	
	/**
	 * Gets an existing resource from the db      
	 * 
	 *      @param category  resource name 
	 */
	private Resource getCategory(String name){
		Resource oClass = null;
		oClass = model.getResource(Constants.NS_CLASSES + name);
		return oClass;
	}
	
	
	/**
	 * Checks whether the category exists in the db  
	 * 
	 */
	private boolean isExists(String name){
		
		return SDBHelper.containsOWLClass(Constants.NS_CLASSES + name, store);
		
		// Note if we use the named model the method 
		// containsClass(className, model) should be called 
		// return SDBHelper.containsClass(Constants.NS_CLASSES + name, mdl);
		
	}




	/**
	 * Function main - This is code is written to 
	 * test the creation of DBpedia category library 
	 * 
	 * @param categories in a comma separated format 
	 * @param broaden limit (for option hierarchy)
	 * @param BFS(1) or DFS(2) (for option hierarchy)
	 * @param add narrow categories (1) and do not add (2) (for option hierarchy) 
	 * 		  e.g. Basketball 2 1 1 
	 */
	public static void main(String args[]) 
	{
		if (args.length < 4) {
			System.out.println("Invalid user input!");
			System.exit(0);
		} 

		DBpediaSDBHelper cl = new DBpediaSDBHelper();

		String[] arr = args[0].trim().split(",");
		ArrayList<String> categories = new ArrayList<String>();
		for (String category : arr)
			categories.add(category.trim());

		System.out.println("Total categories: " + categories.size());

		cl.createCategoryHierarchy(categories, 
				Integer.parseInt(args[1]), // broaden limit 
				(args[2].equalsIgnoreCase("1")) ? true : false,  // BFS or DFS 
				(args[3].equalsIgnoreCase("1")) ? true : false); // add narrow cat or not 
	
	} 


}
