/**
 * 
 */
package uf.morpheus.db;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;

import uf.morpheus.meta.Constants;
import uf.morpheus.meta.MessageLogger;

/**
 * This class is a helper class to build the persistent category class (OWL) library from
 * the DBpedia RDF triples. It uses Jena API and DBpedia SPARQL query service.
 * 
 */

public class DBpediaJDBHelper {

	/**
	 * Sub class to handle the broader nodes in the DBpedia
	 * 
	 */
	public class BTNode implements Comparable<BTNode> {

		public OntClass Category = null;
		public String BroaderCategory = null;
		public int BroaderLevel = 0;

		public BTNode(OntClass category, String broaderCategory, int level) {
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
	public static final String NS_RDF = "http://www.w3.org/2000/01/rdf-schema#";
	private String source = "test";
	private String NS = source + "#";
	private int categoryClassCount = 0;
	private int axiomsCount = 0;
	private MessageLogger msg = MessageLogger.getInstance();
	private long treeHeight = 0; // This should be set to zero in every
									// hierarchy creation
	private OntModel model = null;
	private PersistentOntology po = null;
	private Property subClassPty = null;
	

	public DBpediaJDBHelper(String ontoURI) {
		
		msg.disableConsoleHandler();
		
		source = ontoURI;
		NS = ontoURI + "#";
		
		po = new PersistentOntology();
		model = po.getOntModel(source); 
		
		subClassPty = model.createProperty(NS_RDF, "subClassOf");
	}

	public void clean() {
		po.clean();
		model.close();
	}

	/**
	 * Creates the DBpedia category library based on the given ontology URI and
	 * physical path
	 * 
	 */
	public void createCategoryHierarchy(
	ArrayList<String> categories, 
	int broadenLimit, 
	boolean bfs,
	boolean addNarrowCategories) {

		try {
			long starttime = System.currentTimeMillis();

			categoryClassCount = 0;
			axiomsCount = 0;
			treeHeight = 0;

			// Adds the root category
			OntClass rootC = addCategory(Constants.DBPEDIA_ROOT_CLASS_NAME);

			for (String category : categories) {
				if (bfs)
					addBFSCategoryHierarchicalClasses(
							category, 
							rootC,
							broadenLimit, 
							addNarrowCategories);
				else 
					addDFSCategoryHierarchicalClasses(
							category, 
							rootC,
							broadenLimit, 
							addNarrowCategories);
			}
			// Adds the tree height
			addCategoryHierarchyHeight(rootC);

			StringBuilder sb = new StringBuilder();
			sb.append("REPORT\n---------------------------------------------------------\n");
			sb.append("The ontology class hierarchy has been created!\n");
			sb.append("Total number of category classes added: "
					+ categoryClassCount + "\n");
			sb.append("Total number of axioms added: " + axiomsCount + "\n");
			sb.append("Execution time: " + (System.currentTimeMillis() - starttime) / 1000 + "s.");
			msg.logger.info(sb.toString());

		} catch (Exception e) {
			msg.logger.severe("Exception: " + e.getMessage());
		}
	}

	/**
	 * Adds the category hierachy's tree height
	 * 
	 * @param rootC
	 *            ontology root class
	 * 
	 */
	private void addCategoryHierarchyHeight(OntClass rootC) {

		Property treeHeightProperty = model.createProperty(NS, Constants.DBPEDIA_PROPERTY_TREE_HEIGHT);

		rootC.addProperty(treeHeightProperty, String.valueOf(treeHeight));
		axiomsCount++; // Increments the axioms count
		
		msg.logger.info("Added the ontology tree height:" + treeHeight);
	}

	/**
	 * Adds the DBpedia categories to the OWL Ontology
	 */
	private void addBFSCategoryHierarchicalClasses(
			String category,
			OntClass rootCategory, 
			int broadenLimit, 
			boolean addNarrowCategories) {

		OntClass categoryC = addCategory(category); // Firstly, add the category
		DBpediaConnector dbpC = new DBpediaConnector(); // The helper class to interact with DBpedia 

		// ADDS THE NARROW CATEGORIES 
		msg.logger.info("Adding narrow categories for " + category + "\n");
		if (addNarrowCategories) {
			ArrayList<String> narrowCat = dbpC.getNarrowCategories(category);

			OntClass narrowC = null;
			for (String cat : narrowCat) {
				
				narrowC = model.getOntClass(NS + cat);
				if (narrowC == null) // Checks whether that class exists in the ontology
					narrowC = addCategory(cat);
				
				addSubClassProperty(narrowC, categoryC); // Adds the sub class axiom
			
			}
		}
		msg.logger.info("DONE!\n");
		
		
		// ADDS THE BROADER CATEGORIES 
		msg.logger.info("Adding broader categories (BFS)" + "\n");
		ArrayList<String> broaderCat = dbpC.getBroaderCategories(category);
		Queue<BTNode> broaderQ = new PriorityQueue<BTNode>();
		for (String cat : broaderCat)
			broaderQ.add(new BTNode(categoryC, cat, 1));

		OntClass broaderC = null;
		BTNode broaderNode = null;

		while (!broaderQ.isEmpty()) {
			broaderNode = broaderQ.remove();

			if (broaderNode.BroaderLevel > broadenLimit) {
				treeHeight = broaderNode.BroaderLevel;
				addSubClassProperty(broaderNode.Category, rootCategory); // Adds the sub class axiom
				continue;
			}

			// Checks whether that class exists in the ontology
			broaderC = model.getOntClass(NS + broaderNode.BroaderCategory);
			if (broaderC == null) {
				
				broaderC = addCategory(broaderNode.BroaderCategory); // Adds the broader category
				ArrayList<String> broaderCatSet = dbpC.getBroaderCategories(broaderNode.BroaderCategory);

				if (broaderCatSet.size() == 0) {
					addSubClassProperty(broaderC, rootCategory); // Adds the sub class axiom

					// This code to find the maximum height of the tree
					if (treeHeight < broaderNode.BroaderLevel)
						treeHeight = broaderNode.BroaderLevel;
				} 
				else {
					for (String broaderCatStr : broaderCatSet)
						broaderQ.add(new BTNode(broaderC, broaderCatStr,
								broaderNode.BroaderLevel + 1));
				}
			}

			addSubClassProperty(broaderNode.Category, broaderC); // Adds the sub class axiom
		}

		msg.logger.info("DONE!\n");
	}
	
	
	/**
	 * Adds the DBpedia categories to the OWL Ontology
	 */
	private void addDFSCategoryHierarchicalClasses(
			String category,
			OntClass rootCategory, 
			int broadenLimit, 
			boolean addNarrowCategories) {

		OntClass categoryC = addCategory(category); // Firstly, add the category
		DBpediaConnector dbpC = new DBpediaConnector(); // The helper class to interact with DBpedia 

		// ADDS THE NARROW CATEGORIES 
		msg.logger.info("Adding narrow categories for " + category + "\n");
		if (addNarrowCategories) {
			ArrayList<String> narrowCat = dbpC.getNarrowCategories(category);

			OntClass narrowC = null;
			for (String cat : narrowCat) {
				
				narrowC = model.getOntClass(NS + cat);
				if (narrowC == null) // Checks whether that class exists in the ontology
					narrowC = addCategory(cat);
				
				addSubClassProperty(narrowC, categoryC); // Adds the sub class axiom
			
			}
		}
		msg.logger.info("DONE!\n");
		
		
		// ADDS THE BROADER CATEGORIES 
		msg.logger.info("Adding broader categories (DFS)" + "\n");
		ArrayList<String> broaderCat = dbpC.getBroaderCategories(category);
		Stack<BTNode> broaderStack = new Stack<BTNode>();
		for (String cat : broaderCat)
			broaderStack.add(new BTNode(categoryC, cat, 1));

		OntClass broaderC = null;
		BTNode broaderNode = null;

		while (!broaderStack.isEmpty()) {
			broaderNode = broaderStack.pop();

			if (broaderNode.BroaderLevel > broadenLimit) {
				treeHeight = broaderNode.BroaderLevel;
				addSubClassProperty(broaderNode.Category, rootCategory); // Adds the sub class axiom
				continue;
			}

			// Checks whether that class exists in the ontology
			broaderC = model.getOntClass(NS + broaderNode.BroaderCategory);
			if (broaderC == null) {
				
				broaderC = addCategory(broaderNode.BroaderCategory); // Adds the broader category
				ArrayList<String> broaderCatSet = dbpC.getBroaderCategories(broaderNode.BroaderCategory);

				if (broaderCatSet.size() == 0) {
					addSubClassProperty(broaderC, rootCategory); // Adds the sub class axiom

					// This code to find the maximum height of the tree
					if (treeHeight < broaderNode.BroaderLevel)
						treeHeight = broaderNode.BroaderLevel;
				} 
				else {
					for (String broaderCatStr : broaderCatSet)
						broaderStack.add(new BTNode(broaderC, broaderCatStr,
								broaderNode.BroaderLevel + 1));
				}
			}

			addSubClassProperty(broaderNode.Category, broaderC); // Adds the sub class axiom
		}

		msg.logger.info("DONE!\n");
	}
	

	/**
	 * Adds class to the OWL ontology
	 * 
	 * @param category class name
	 */
	private OntClass addCategory(String name) {
		
		OntClass oClass = null;
		try {
			oClass = model.createClass(NS + name);
		} catch (Exception e) {
			msg.logger.severe("Exception in adding class " + name + "\n" + e.getMessage());
			System.exit(1);
		}
		categoryClassCount++; // Increments the category count
		
		msg.logger.info("Added the category : " + name);
		
		return oClass;
	}
	
	/**
	 * Adds property to a given class
	 * 
	 * @param category class name
	 */
	private void addSubClassProperty(OntClass oClass, OntClass superClass) {
		
		try {
			oClass.addProperty(subClassPty, superClass);
		} catch (Exception e) {
			msg.logger.severe("Exception in adding subclass property for " + oClass + "\n" + e.getMessage());
			System.exit(1);
		}
		axiomsCount++; // Increments the axioms count
		
		msg.logger.info("Added axiom: " + oClass + " <-- " + superClass);
	}
	
	

	/**
	 * Function main - This is code is written to test the creation of DBpedia
	 * category library
	 * 
	 * @param category name
	 * @param ontology URI
	 * @param broaden limit (for option hierarchy)
	 * @param BFS (1) or DFS(2) (for option hierarchy)
	 * @param add narrow categories (1) and do not add (2) (for option hierarchy) 
	 *            e.g. Automobiles "http://zion.cise.ufl.edu/ontology/classes/Automobiles" 2 1 1
	 */
	public static void main(String args[]) {
		if (args.length < 5) {
			System.out.println("Invalid user input!");
			System.exit(0);
		}

		// Create category hierarchy
		DBpediaJDBHelper cl = new DBpediaJDBHelper(args[1]);

		String[] arr = args[0].trim().split(",");
		ArrayList<String> categories = new ArrayList<String>();
		for (String category : arr)
			categories.add(category.trim());

		cl.createCategoryHierarchy(categories, Integer.parseInt(args[2]), // broaden limit
				(args[3].equalsIgnoreCase("1")) ? true : false, // BFS or DFS
				(args[4].equalsIgnoreCase("1")) ? true : false); // add narrow cat or not

		// Closes the db connection
		cl.clean();

	}
}
