/**
 * 
 */
package uf.morpheus.db;

import java.net.URI;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;

import uf.morpheus.meta.Constants;
import uf.morpheus.meta.MessageLogger;

/**
 * This class is a helper class to build the category class (OWL) library 
 * and Markov Blanket from the DBpedia RDF triples. It uses Jena API and  
 * DBpedia SPARQL query service. 
 * 
 *   Reference : OWLClasses.java (OWLMorpheus package by Guillermo)
 *
 */

public class DBpediaHelper {

	/**
	 * Sub class to handle the broader nodes in the DBpedia 
	 *
	 */
	public class BTNode implements Comparable<BTNode>{
		
		public OWLDescription Category = null;
		public String BroaderCategory = null;
		public int BroaderLevel = 0;

		public BTNode(OWLDescription category, String broaderCategory, int level) {
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
	
	private static OWLOntologyManager manager;
	private static OWLOntology ontology;
	private static OWLDataFactory factory;
	private URI ontologyURI = null;
	private URI physicalURI = null;
	private int categoryClassCount = 0;
	private int axiomsCount = 0;
	private int categoryIndividualsCount = 0;
	private static Stack<OWLDescription> superStack = new Stack<OWLDescription>();
	private MessageLogger msg = MessageLogger.getInstance();
	private long treeHeight = 0; // This should be set to zero in every hierarchy creation 
	
	/**
	 * Constructor
	 * 
	 * @param ontologyURI ontology URI 
	 * @param physicalPath physical path where the ontology to be saved   
	 */
	public DBpediaHelper(String ontoURI, String physicalPath) 
	{
		manager = OWLManager.createOWLOntologyManager();
		ontologyURI = URI.create(ontoURI);
		physicalURI = URI.create(physicalPath);
		SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		manager.addURIMapper(mapper);
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

			ontology = manager.createOntology(ontologyURI);
			factory = manager.getOWLDataFactory();
			
			categoryClassCount = 0;
			axiomsCount = 0;
			categoryIndividualsCount = 0;
			treeHeight = 0;
			
			// Adds the root category 
			OWLDescription rootC = addClass(Constants.DBPEDIA_ROOT_CLASS_NAME);
			msg.logger.info("Added the root category: " + Constants.DBPEDIA_ROOT_CLASS_NAME + "\n");
			
			for (String category : categories){
				if (bfs)
					addBFSCategoryHierarchicalClasses(category, rootC, broadenLimit, addNarrowCategories);
				else 
					addDFSCategoryHierarchicalClasses(category, rootC, broadenLimit, addNarrowCategories); 
			}
			// Adds the tree height 
			addCategoryHierarchyHeight(rootC);

			// Saves the ontology to a physical file 
			manager.saveOntology(ontology, physicalURI);
			msg.logger.info("Saved the ontology in : " + physicalURI + "\n");
			
			StringBuilder sb = new StringBuilder();
			sb.append("REPORT\n---------------------------------------------------------\n");
			sb.append("The ontology class hierarchy has been created!\n");
			sb.append("Total number of category classes added: " + categoryClassCount + "\n");
			sb.append("Total number of category class individuals added: " + categoryIndividualsCount + "\n");
			sb.append("Total number of axioms added: " + axiomsCount + "\n");
			sb.append("Execution time: " + (System.currentTimeMillis() - starttime)/1000 + "s.");
			msg.logger.info(sb.toString());
			
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
	private void addCategoryHierarchyHeight(OWLDescription rootC) throws OWLOntologyChangeException
	{
		OWLDataProperty treeHeightProperty = factory.getOWLDataProperty(URI
				.create(ontologyURI + "#"
						+ Constants.DBPEDIA_PROPERTY_TREE_HEIGHT));
		
		OWLIndividual rootI = addIndividual(
				rootC, 
				Constants.DBPEDIA_ROOT_CLASS_NAME); 
		
		// Adds the tree Height to the root class 
		addAxiom(factory.getOWLDataPropertyAssertionAxiom(
				rootI, 
				treeHeightProperty, 
				treeHeight));

		msg.logger.info("Added the ontology tree height:" + treeHeight);
	}
	
	
	
	/**
	 * Creates the markov blanket for a given category 
	 * using the DBpedia categories   
	 * 
	 * @param category the category name  
	 */
	public void createMarkovBlanket(String category) 
	{
		try {
			long starttime = System.currentTimeMillis();
			msg.logger.info("The category blanket building for " + category + " is started!\n");

			
			ontology = manager.createOntology(ontologyURI);
			factory = manager.getOWLDataFactory();

			categoryClassCount = 0;
			axiomsCount = 0;
			categoryIndividualsCount = 0;
			
			addBlanketCategories(category); 

			// Saves the ontology to a physical file 
			manager.saveOntology(ontology, physicalURI);
			msg.logger.info("Saved the ontology in : " + physicalURI + "\n");
			
			StringBuilder sb = new StringBuilder();
			sb.append("REPORT\n---------------------------------------------------------\n");
			sb.append("The ontology class hierarchy has been created!\n");
			sb.append("Total number of category classes added: " + categoryClassCount + "\n");
			sb.append("Total number of category class individuals added: " + categoryIndividualsCount + "\n");
			sb.append("Total number of axioms added: " + axiomsCount + "\n");
			sb.append("Execution time: " + (System.currentTimeMillis() - starttime)/1000 + "s.");
			msg.logger.info(sb.toString());

		} catch (Exception e) {
			msg.logger.severe("Exception: " + e.getMessage());
		}
	}
	
	/**
	 * Grabs and adds the DBpedia categories for the blanket  
	 * 
	 * @param category the category name    
	 * @throws OWLOntologyChangeException 
	 */
	private void addBlanketCategories(String category) throws OWLOntologyChangeException {

		// Firstly, add the given category to OWL document 
		OWLDescription categoryC = addClass(category);
		
		DBpediaConnector dbpC = new DBpediaConnector();
		ArrayList <String> broaderCat = dbpC.getBroaderCategories(category);
		ArrayList <String> narrowCat = dbpC.getNarrowCategories(category);
		
		// Adds the broader category 
		msg.logger.info("Adding the broader categories\n");
		
		for (String cat : broaderCat){
			OWLDescription broaderC = addClass(cat);
			addAxiom(factory.getOWLSubClassAxiom(categoryC, broaderC));
			msg.logger.info(cat);
		}
		
		
		msg.logger.info("Adding the narrow categories for " 
				+ category 
				+ "\n");
		
		for (String cat : narrowCat){
			OWLDescription narrowC = addClass(cat);
			addAxiom(factory.getOWLSubClassAxiom(narrowC, categoryC));
			msg.logger.info(cat);
			
			// Adds the broader categories of each narrow categories too
			OWLDescription broaderC = null;
			for (String broader : dbpC.getBroaderCategories(cat)){
				
				if (!broader.equalsIgnoreCase(category)){ // ignores the duplicates 
					
					// Checks whether that class exists in the ontology 
					if (this.isExists(broader))
						broaderC = getClass(broader);
					else 
						broaderC = addClass(broader);
					addAxiom(factory.getOWLSubClassAxiom(narrowC, broaderC));
					msg.logger.info("\t " + cat + " <-- " + broader);
				}
			}
		}
		msg.logger.info("DONE!\n");
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
			OWLDescription rootCategory,
			int broadenLimit, 
			boolean addNarrowCategories) throws OWLOntologyChangeException {
		
		msg.logger.info("Building the hierarchy (BFS) for " + category + "\n");
		
		String arr = "";
		// Adds the distance to leaf property 
		//OWLDataProperty distToLeaf = factory.getOWLDataProperty(URI.create(ontologyURI + "#distToLeaf"));
		
		// Firstly, add the category 
		OWLDescription categoryC = addClass(category);
		
		//OWLIndividual categoryI = addIndividual(categoryC, category); 
		
		// Adds the distance to the leaf node
		//addLeafDistance(distToLeaf, categoryI, 1);

		DBpediaConnector dbpC = new DBpediaConnector();
		
		// Adds the narrow categories
		if (addNarrowCategories) {
			
			ArrayList<String> narrowCat = dbpC.getNarrowCategories(category);

			msg.logger
					.info("Adding narrow categories for "
							+ category
							+ "\n");
			OWLDescription narrowC = null;
			// OWLIndividual narrowI = null;
			for (String cat : narrowCat) {

				// Checks whether that class exists in the ontology
				if (this.isExists(cat)) {
					// Gets the existing narrow category
					narrowC = getClass(cat);
				} else {
					narrowC = addClass(cat);
					msg.logger.info("Adde narrow category: " + cat);
					// Adds the distance to the leaf node
					// narrowI = addIndividual(narrowC, cat);
					// addLeafDistance(distToLeaf, narrowI, 0);
				}
				if (!isAncestor(narrowC, categoryC)) {
					addAxiom(factory.getOWLSubClassAxiom(narrowC, categoryC));
					msg.logger.info("Added axiom: " + cat + " <-- " + category);
				} else {
					msg.logger.info(cat + " <-- " + category
							+ " can make cycle.");
				}
			}
		}
		// Forms the hierarchy 
		msg.logger.info("\nAdding broader categories (BFS)" 
				+ "\n");
		ArrayList <String> broaderCat = dbpC.getBroaderCategories(category);
		Queue<BTNode> broaderStack = new PriorityQueue<BTNode>();
		for (String cat : broaderCat)
			broaderStack.add(new BTNode(categoryC, cat, 1));
		
		OWLDescription broaderC = null;
		//OWLIndividual broaderI = null;
		BTNode broaderNode = null;
		
		while(!broaderStack.isEmpty()){
			broaderNode = broaderStack.remove();
			
			if (broaderNode.BroaderLevel > broadenLimit){
				treeHeight = broaderNode.BroaderLevel;
				addAxiom(factory.getOWLSubClassAxiom(broaderNode.Category, rootCategory)); // Adds the sub class axiom  
				continue;
			}
			
			// Checks whether that class exists in the ontology 
			if (this.isExists(broaderNode.BroaderCategory))	{
				// Gets the existing broader category 
				broaderC = getClass(broaderNode.BroaderCategory);
			}
			else {
				// Adds the broader category 
				broaderC = addClass(broaderNode.BroaderCategory);
				msg.logger.info("Added broader category class: " + broaderNode.BroaderCategory);
				
				// Adds the distance to the leaf node
				//broaderI = addIndividual(broaderC, broaderNode.BroaderCategory); 
				//addLeafDistance(distToLeaf, broaderI, broaderNode.BroaderLevel + 1);
				
				ArrayList <String> broaderCatSet = dbpC.getBroaderCategories(broaderNode.BroaderCategory);	
				
				if (broaderCatSet.size() == 0){
					// Adds the sub class axiom  
					addAxiom(factory.getOWLSubClassAxiom(broaderC, rootCategory));
					arr += broaderNode.BroaderCategory + " ";
					
					// This code to find the maximum height of the tree
					if (treeHeight < broaderNode.BroaderLevel)
						treeHeight = broaderNode.BroaderLevel;
				}
				else {
					for (String broaderCatStr : broaderCatSet)
						broaderStack.add(new BTNode(broaderC, broaderCatStr, broaderNode.BroaderLevel + 1));
				}
			}
			
			// Adds the sub class axiom
//			if (!isAncestor(broaderNode.Category, broaderC)) {
				addAxiom(factory.getOWLSubClassAxiom(broaderNode.Category,
						broaderC));
				msg.logger.info("Added axiom: "
						+ broaderNode.Category.toString() + " <-- "
						+ broaderNode.BroaderCategory);
//			} else {
//				writeLog(broaderNode.Category.toString() + " <-- "
//						+ broaderNode.BroaderCategory + " can make cycle.");
//			}
			
		}
		if (arr != "")
			msg.logger.info("The categories with no broder categories: " + arr);
		msg.logger.info("DONE!\n");
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
			OWLDescription rootCategory,
			int broadenLimit, 
			boolean addNarrowCategories) throws OWLOntologyChangeException {
		
		msg.logger.info("Building the hierarchy (DFS) for " + category + "\n");
		
		String arr = "";

		// Adds the distance to leaf property 
		//OWLDataProperty distToLeaf = factory.getOWLDataProperty(URI.create(ontologyURI + "#distToLeaf"));
		
		// Firstly, add the category 
		OWLDescription categoryC = addClass(category);

		
		//OWLIndividual categoryI = addIndividual(categoryC, category); 
		
		// Adds the distance to the leaf node
		//addLeafDistance(distToLeaf, categoryI, 1);

		DBpediaConnector dbpC = new DBpediaConnector();

		// Adds the narrow categories
		if (addNarrowCategories) {
			
			ArrayList<String> narrowCat = dbpC.getNarrowCategories(category);
			msg.logger
					.info("Adding narrow categories for "
							+ category
							+ "\n");
			OWLDescription narrowC = null;
			// OWLIndividual narrowI = null;
			for (String cat : narrowCat) {

				// Checks whether that class exists in the ontology
				if (this.isExists(cat)) {
					// Gets the existing narrow category
					narrowC = getClass(cat);
				} else {
					narrowC = addClass(cat);
					msg.logger.info("Added narrow category: " + cat);
					// Adds the distance to the leaf node
					// narrowI = addIndividual(narrowC, cat);
					// addLeafDistance(distToLeaf, narrowI, 0);
				}
				if (!isAncestor(narrowC, categoryC)) {
					addAxiom(factory.getOWLSubClassAxiom(narrowC, categoryC));
					msg.logger.info("Added axiom: " + cat + " <-- " + category);
				} else {
					msg.logger.info(cat + " <-- " + category
							+ " can make cycle.");
				}
			}

		}
		
		
		// Forms the hierarchy 
		msg.logger.info("Adding broader categories (DFS)" + category + "\n");
		
		ArrayList <String> broaderCat = dbpC.getBroaderCategories(category);
		if (broaderCat.size() > 0){
		
			Stack<BTNode> broaderStack = new Stack<BTNode>();
			for (String cat : broaderCat)
				broaderStack.add(new BTNode(categoryC, cat, 1));
			
			OWLDescription broaderC = null;
			//OWLIndividual broaderI = null;
			BTNode broaderNode = null;
			
			while(!broaderStack.isEmpty()){
				broaderNode = broaderStack.pop();
				
				if (broaderNode.BroaderLevel > broadenLimit){
					treeHeight = broaderNode.BroaderLevel;
					addAxiom(factory.getOWLSubClassAxiom(broaderNode.Category, rootCategory)); // Adds the sub class axiom  
					continue;
				}
				// Checks whether that class exists in the ontology 
				if (this.isExists(broaderNode.BroaderCategory))	{
					// Gets the existing broader category 
					broaderC = getClass(broaderNode.BroaderCategory);
				}
				else {
					// Adds the broader category 
					broaderC = addClass(broaderNode.BroaderCategory);
					msg.logger.info("Added broader category: " + broaderNode.BroaderCategory);
					
					// Adds the distance to the leaf node
					//broaderI = addIndividual(broaderC, broaderNode.BroaderCategory); 
					//addLeafDistance(distToLeaf, broaderI, broaderNode.BroaderLevel + 1);
					
					ArrayList <String> broaderCatSet = dbpC.getBroaderCategories(broaderNode.BroaderCategory);	
					
					if (broaderCatSet.size() == 0){
						// Adds the sub class axiom  
						addAxiom(factory.getOWLSubClassAxiom(broaderC, rootCategory));
						arr += broaderNode.BroaderCategory + " ";
						
						if (treeHeight < broaderNode.BroaderLevel)
							treeHeight = broaderNode.BroaderLevel;
					}
					else {
						for (String broaderCatStr : broaderCatSet)
							broaderStack.add(new BTNode(broaderC, broaderCatStr, broaderNode.BroaderLevel + 1));
					}
				}
				
				// Adds the sub class axiom
	//			if (!isAncestor(broaderNode.Category, broaderC)) {
					addAxiom(factory.getOWLSubClassAxiom(broaderNode.Category,
							broaderC));
					msg.logger.info("Added axiom: "
							+ broaderNode.Category.toString() + " <-- "
							+ broaderNode.BroaderCategory);
	//			} else {
	//				writeLog(broaderNode.Category.toString() + " <-- "
	//						+ broaderNode.BroaderCategory + " can make cycle.");
	//			}
				
			}

		}
		else 
		{
			// Adds the DBpedia root as the ancestor   
			addAxiom(factory.getOWLSubClassAxiom(categoryC, rootCategory));
			arr += category;
			
			if (treeHeight == 0)
				treeHeight = 1;
		}
		
		if (arr != "")
			msg.logger.info("The categories with no broder categories: " + arr);
		
		msg.logger.info("DONE!\n");
	}
	
	
	/**
	 * Adds the leaf distance value to the property 'dist to leaf' 
	 * 
	 * 
	 *      @param property  property that represents the tree height  
	 *      @param individual individual name 
	 *      @param dist the distance to the leaf node 
	 */
	@SuppressWarnings("unused")
	private void addLeafDistance(OWLDataProperty property,
			OWLIndividual individual, int dist) throws OWLOntologyChangeException {
		
		OWLDataPropertyAssertionAxiom a = factory.getOWLDataPropertyAssertionAxiom(
				individual, 
				property, 
				dist);
		addAxiom(a);		
	}

	/**
	 * Creates an instance of a class 
	 * Note: an 'I' will be appended to the category name to make it unique 
	 * 
	 * 
	 *      @param category  class name 
	 *      @param name individual name 
	 */
	private OWLIndividual addIndividual(
			OWLDescription category,
			String name) throws OWLOntologyChangeException {
		
		OWLIndividual ind = null; 
		
		ind = factory.getOWLIndividual(URI.create(ontologyURI + "#I" + name));		
		
		addAxiom(factory.getOWLClassAssertionAxiom(ind, category));
		
		// Increments the category count 
		categoryIndividualsCount++; 
		
		return ind;
	}

	/**
	 * Adds class to the OWL ontology     
	 * 
	 *      @param category  class name 
	 */
	private OWLClass addClass(String name)
			throws OWLOntologyChangeException {
		
		// Checks whether class exists in the ontology 
		// If the class exists in the Ontology we don't 
		// need to add it once more  
		//OWLClass eClass = getClass(name);
		//if (eClass != null)
		//	return eClass;		
		
		OWLClass oClass = factory.getOWLClass(URI.create(ontologyURI + "#" + name));
		OWLAxiom axiom = factory.getOWLDeclarationAxiom(oClass);
		addAxiom(axiom);
		
		/*
		// Adds the index to the data base table :)
		try {
			DBAccess.insertDBpediaCategoryIndex(fileName, name);
		} catch (SQLException e) {
			msg.logger.severe(e.toString());
		}
		*/
		
		// Increments the category count 
		categoryClassCount++; 
		
		return oClass;
	}

	/**
	 * Adds axiom to the OWL ontology    
	 */
	private void addAxiom(OWLAxiom axiom) 
			throws OWLOntologyChangeException {
		
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		
		// Increments the axioms count 
		axiomsCount++;
		
		manager.applyChange(addAxiom);
	}

	
	/**
	 * Gets an existing class in the ontology 
	 * 
	 * @param name class name
	 * @return OWLClass object if a class exists, otherwise null
	 */
	private OWLClass getClass(String name) {
		OWLClass owlClass = null;
		
		// This for loop will look on the ontology for the class given by name
        for( OWLClass cls : ontology.getReferencedClasses()) {        	
        	if (cls.toString().trim().toUpperCase().compareTo(name.trim().toUpperCase()) == 0) {        		
        		owlClass = cls;
        		break;
        	}
            
        }
        return owlClass;
	}
	
	/**
	 * Checks whether a class exists in the ontology 
	 * 
	 * @param name class name
	 * @return true if exists, otherwise false 
	 */
	private boolean isExists(String name) {
		boolean ret = false; 
		
		// This for loop will look on the ontology for the class given by name
        for( OWLClass cls : ontology.getReferencedClasses())   	
        	if (cls.toString().trim().toUpperCase().compareTo(name.trim().toUpperCase()) == 0)   		
        		return true;

        return ret;
	}
	
	
	/**
	 * Checks whether the given class is ancestor to the class 
	 * Note: It is a time consuming procedure call  
	 * 
	 * TODO we may need to improve this logic
	 * there could be chances of memory issues  
	 */
	private boolean isAncestor(
			OWLDescription ancestorClass, 
			OWLDescription descendantClass)
	{
		// Validates the input 
		if (ancestorClass.isOWLNothing()) return false;
		if (descendantClass.isOWLNothing()) return false;
		
		superStack.clear();
		superStack.add(descendantClass);
		
		while(!superStack.isEmpty())
		{
			OWLDescription ancestor = superStack.pop();
			
			if (ancestor.isAnonymous())
				continue;
			
			if (ancestorClass.asOWLClass().getURI().equals(ancestor.asOWLClass().getURI())){
				superStack.clear();
				return true;
			}
			else 
				for (OWLDescription anr : ancestor.asOWLClass().getSuperClasses(ontology))
					superStack.add(anr);				
		}
		
		superStack.clear();		
		return false;
	}


	/**
	 * Function main - This is code is written to 
	 * test the creation of DBpedia category library 
	 * 
	 * @param [1] create category hierarchy [2] create markov blanket 
	 * @param category name
	 * @param ontology URI
	 * @param physical path 
	 * @param broaden limit (for option hierarchy)
	 * @param BFS(1) or DFS(2) (for option hierarchy)
	 * @param add narrow categories (1) and do not add (2) (for option hierarchy) 
	 * 		  e.g. 1 Basketball "file:/C://temp//Basketball.xml" "http://zion.cise.ufl.edu/ontology/classes/Basketball.xml" 5 1 1 
	 *        e.g. 2 Basketball "file:/C://temp//BasketballBlanket.xml" "http://zion.cise.ufl.edu/ontology/blankets/BasketballBlanket.xml"
	 */
	public static void main(String args[]) 
	{
		if (args.length < 4) {
			System.out.println("Invalid user input!");
			System.exit(0);
		} 
		else if (args[0].equalsIgnoreCase("1")) { // Create category hierarchy
			if (args.length < 7) {
				System.out.println("Invalid user input!");
				System.exit(0);
			}

			DBpediaHelper cl = new DBpediaHelper(args[3], args[2]);

			String[] arr = args[1].trim().split(",");
			ArrayList<String> categories = new ArrayList<String>();
			for (String category : arr)
				categories.add(category.trim());

			System.out.println("Total categories: " + categories.size());

			cl.createCategoryHierarchy(
					categories, 
					Integer.parseInt(args[4]), // broaden limit 
					(args[5].equalsIgnoreCase("1")) ? true : false, // BFS or DFS 
					(args[6].equalsIgnoreCase("1")) ? true : false); // add narrow cat or not 
		} 
		else {  // Create markov blanket 
			DBpediaHelper cl = new DBpediaHelper(args[3], args[2]);
			cl.createMarkovBlanket(args[1]);
		}
	}
}
