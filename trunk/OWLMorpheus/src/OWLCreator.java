


import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;



import db.*; 

public class OWLCreator {
	
	public String uri = null;
	public String file = null;
	public static int queryID; // The ID of the query we are going to work on
	public static OWLOntologyManager manager;
	public static OWLOntology ontology;
	public static OWLDataFactory factory;
	public static URI ontologyURI;
	public static OWLAxiom axiom;
	public static Query query;	
	//Manager for classes
	public static OWLOntologyManager classManager; //Manager for Class ontology
	public static URI physicalURI; 
	public static OWLOntology classOntology;	
	//Manager for Properties
	public static OWLOntologyManager propertyManager; //Manager for Class ontology
	public static URI propertyPhysicalURI; 
	public static OWLOntology propertyOntology;	
	// Hash table with context
	public Hashtable htContext = new Hashtable();

	public OWLCreator(String uri, String file, int queryID) {
		this.uri = uri;
		this.file = file;
		this.queryID = queryID;
		query = DBReader.getQuery(queryID);
		createOntology();
	}

	
	public void createOntology() {
		//Initialize Classes and properties 
		//OWLClasses classes = new OWLClasses(); //Creates the file OWLClasses.xml
		//OWLProperties properties = new OWLProperties(); //Creates the file OWLProperties.xml
		
		manager = OWLManager.createOWLOntologyManager();		
		ontologyURI = URI.create(uri);
		URI physicalURI = URI.create(file);
		SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		manager.addURIMapper(mapper);
		
		try {
			
			importClassesOntology();

			importPropertiesOntology();
			//System.out.println("properties imported succ");
		
			ontology = manager.createOntology(ontologyURI);
			factory = manager.getOWLDataFactory();
						
			//addClassContext();//This will add the class Context for the query
			
			addContext();// Method to add context of the query as instances of classes


			// Adding SSQ-0
			constructSSQ();		
			
			
			manager.saveOntology(ontology);
			
			//System.out.println("Ontology Created");
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	private void addAxiom(OWLAxiom axiom) throws OWLOntologyChangeException {
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		manager.applyChange(addAxiom);
	}
	private OWLClass addClass(String name) throws OWLOntologyChangeException {
		OWLClass oClass = factory.getOWLClass(URI.create(ontologyURI + name));
		OWLAxiom axiom = factory.getOWLDeclarationAxiom(oClass);
		addAxiom(axiom);		
		return oClass;
	}	
	/*
	private OWLClass getClass(String name) {
		OWLClass owlClass = null;
		
		// This for loop will look on the ontology for the class given by name
        for(OWLClass cls : ontology.getReferencedClasses()) {        	
        	if (cls.toString().compareTo(name) == 0) {        		
        		owlClass = cls;
        		//System.out.println("The class founded is: " + owlClass);
        		break;
        	}
            
        }
        return owlClass;
	}
	*/
	private OWLClass getImportedClass(String name) {
		OWLClass owlClass = null;
		
		// This for loop will look on the ontology for the class given by name
        for(OWLClass cls : classOntology.getReferencedClasses()) {        	
        	if (cls.toString().compareTo(name) == 0) {        		
        		owlClass = cls;
        		//System.out.println("The class founded is: " + owlClass);
        		break;
        	}
            
        }
        return owlClass;
	}
	private OWLObjectProperty getImportedProperty(String name ){		
		OWLObjectProperty oproperty = null;
		
		// This for loop will look on the ontology for the property given by name
        for(OWLObjectProperty prop : propertyOntology.getReferencedObjectProperties()) {
        	//System.out.println("Property is: " +prop);
        	if (prop.toString().compareTo(name) == 0) {
        		//System.out.println("Property Founded");
        		oproperty = prop;
        		break;
        	}
            
        }
        return oproperty;
	}
	/**
	 * This method will construct the context of the individual by adding the class that belongs to
	 * and creating a modifier list with a the modifiers of the class
	 * @throws OWLOntologyChangeException
	 */
	private void addContext() throws OWLOntologyChangeException {	
		Individual[] individual = query.getIndividual();
		//Importing the context class
		OWLClass context = getImportedClass("Context");
		OWLObjectProperty belongsToClass =  getImportedProperty("belongsToClass"); //ValueWithinContext
		OWLIndividual instanceClass = null;
		OWLClass currentClass = null;
		OWLClassAssertionAxiom t = null;
		
		for (int x = 0; x < individual.length; x++) {
			//Adding the contextclass as an instance of the class context		
			
			String contextName = insertIntoHasTable("#"+individual[x].getContextname()+"_"+individual[x].getIo()+"_"+individual[x].getPhrasestring(), x);
			OWLIndividual contextClass = factory.getOWLIndividual(URI.create(contextName));
			t = factory.getOWLClassAssertionAxiom(contextClass, context) ;					
			addAxiom(t);
			
			//Importing current class and creating an instance of it
			instanceClass = factory.getOWLIndividual(URI.create("#"+individual[x].getContextclass()));			
			//Getting the class from zion 
			//String sClass = filesystem.ClassSearch.SearchClass(individual[x].getContextclass(), query.getRealm().getRealm());
			//String sClass = db.SearchClass.Search(individual[x].getContextclass());
			String sClass = "http://zion.cise.ufl.edu/ontology/classes#"+individual[x].getContextclass();
			//Will return the class location
			currentClass = factory.getOWLClass(URI.create(sClass));			
			t = factory.getOWLClassAssertionAxiom(instanceClass, currentClass) ;					
			addAxiom(t);
			
			//Using the property belongsToClass to assign the class to the context		  			
			OWLObjectPropertyAssertionAxiom assertion= factory.getOWLObjectPropertyAssertionAxiom(contextClass, belongsToClass, instanceClass) ;    
	        AddAxiom addAxiomChange = new AddAxiom(ontology, assertion); 
	        manager.applyChange(addAxiomChange);
	        
	      //Adding Modifier
			Modifier[] modifier = individual[x].getModifier();
			//System.out.println("Modifier length: " + modifier.length);
			
			OWLObjectProperty hasModifier=null;
			
			//OWLClass mod = null;
			OWLIndividual modifierList=null;
			OWLClass modifierListClass = getImportedClass("ModifierList");
			for (int y = 0; y < modifier.length; y++) {	
				
				if (y == 0) {
					//Adding modifier list 
					
					modifierList = factory.getOWLIndividual(URI.create(contextClass.getURI()+"_Modifier_List"));					
					t = factory.getOWLClassAssertionAxiom(modifierList, modifierListClass) ;					
					addAxiom(t);	
					
					//Adding this list to the context
					assertion= factory.getOWLObjectPropertyAssertionAxiom(contextClass, getImportedProperty("hasModifierList"), modifierList) ;    
			        addAxiomChange = new AddAxiom(ontology, assertion); 
			        manager.applyChange(addAxiomChange);
											
				}
				//Adding a new class Modifier		
				//System.out.println("Modifier for individual " +individual[x].getPhrasestring() + " is: " +modifier[y].getModifierstring());
				OWLClass modifierClass = addClass("#"+modifier[y].getModifierstring());
				addAxiom(factory.getOWLSubClassAxiom(modifierClass, getImportedClass("Modifier")));
				
				//Creates the individual type modifier
				OWLIndividual contextmodifier = factory.getOWLIndividual(URI.create(contextClass.getURI()+"_Modifier_"+(y+1)));
				
				//This shows how to add an instance of a class				
				t = factory.getOWLClassAssertionAxiom(contextmodifier, modifierClass) ;					
				addAxiom(t);
				
				//Adding rank
				OWLDataProperty hasRank = factory.getOWLDataProperty(URI.create("#hasRank"));	            
				OWLDataPropertyAssertionAxiom dataAssertion= factory.getOWLDataPropertyAssertionAxiom(contextmodifier, hasRank, modifier[y].getRank()) ;            
	            addAxiomChange = new AddAxiom(ontology, dataAssertion);
	            manager.applyChange(addAxiomChange);
	            
	            //Adding this modifier to the list
	            hasModifier = getImportedProperty("hasModifier");
				assertion= factory.getOWLObjectPropertyAssertionAxiom(modifierList, hasModifier, contextmodifier) ;    
		        addAxiomChange = new AddAxiom(ontology, assertion); 
		        manager.applyChange(addAxiomChange);		           
				}   
						
		}
		
	}
	
	/**
	 * This method witll construct the SSQ-0 for the ontology
	 * @throws OWLOntologyChangeException
	 */
	public void constructSSQ() throws OWLOntologyChangeException {
		Individual[] individual = query.getIndividual();
		
		//Creates the individual type SSQ
		OWLIndividual SSQ = factory.getOWLIndividual(URI.create("#SSQ-"+query.getQueryid()));	
		
		//Adding realm to the ontology
		OWLIndividual realm = factory.getOWLIndividual(URI.create("#"+query.getRealm().getRealm())); //New instance of realm		
		//Getting the class from zion 
		//String sClass = filesystem.ClassSearch.SearchClass(query.getRealm().getRealm(), query.getRealm().getRealm());
		//String sClass = db.SearchClass.Search(query.getRealm().getRealm());
		String sClass = "http://zion.cise.ufl.edu/ontology/classes#"+query.getRealm().getRealm();
		//Will return the class location
		OWLClass realmClass = factory.getOWLClass(URI.create(sClass));	
		//Create the instance of the class realm
		OWLClassAssertionAxiom t = factory.getOWLClassAssertionAxiom(realm, realmClass) ;					
		addAxiom(t);
		
		//This shows how to add an instance of a class
		//OWLClassAssertionAxiom t = factory.getOWLClassAssertionAxiom(realm, realmClass);					
		//addAxiom(t);
		
		//Adding SSQ-0 as instance of class SSQ		
		t = factory.getOWLClassAssertionAxiom(SSQ, getImportedClass("SSQ")) ;					
		addAxiom(t);
		
		//Adding Realm
		OWLObjectProperty hasRealm = getImportedProperty("hasRealm");	 
		OWLObjectPropertyAssertionAxiom assertion= factory.getOWLObjectPropertyAssertionAxiom(SSQ, hasRealm, realm) ;    
        AddAxiom addAxiomChange = new AddAxiom(ontology, assertion);                
        manager.applyChange(addAxiomChange);
        
		//Adding Query id		
		OWLDataProperty hasQueryID = factory.getOWLDataProperty(URI.create("#hasQueryID"));	            
		OWLDataPropertyAssertionAxiom dataAssertion= factory.getOWLDataPropertyAssertionAxiom(SSQ, hasQueryID, query.getQueryid()) ;            
        addAxiomChange = new AddAxiom(ontology, dataAssertion);
        manager.applyChange(addAxiomChange);
        
        
        
		//Adding instances of the Context to SSQ-0
        OWLIndividual context = null;
         
        for ( Enumeration keys = htContext.keys() ; keys.hasMoreElements();) {
        	
        	String key = (String) keys.nextElement();
        	//System.out.println("Inserting next element: " +key+"into SSQ");
			context = factory.getOWLIndividual(URI.create(key));
			int x = (Integer) htContext.get(key);
			assertion= factory.getOWLObjectPropertyAssertionAxiom(SSQ, getImportedProperty(individual[x].getContextname()+"_"+individual[x].getIo()), context) ;     
		    
	        addAxiomChange = new AddAxiom(ontology, assertion); 
            
	        manager.applyChange(addAxiomChange);
			
        	
        }
		
	}
	/**
	 * Import classes from the classes ontology file by creating a new manager
	 * @throws OWLOntologyCreationException
	 */
	
	public void importClassesOntology() throws OWLOntologyCreationException {
		 classManager = OWLManager.createOWLOntologyManager(); // Manager for Class ontology		 
		 physicalURI = URI.create("http://zion.cise.ufl.edu/ontology/classes.xml");		 
		 classOntology = classManager.loadOntologyFromPhysicalURI(physicalURI);		 
		 
	}
	/**
	 * Imports the properties from the property ontology file by creating a new manager
	 * @throws OWLOntologyCreationException
	 */
	public void importPropertiesOntology() throws OWLOntologyCreationException {
		 propertyManager = OWLManager.createOWLOntologyManager(); // Manager for Class ontology
		 propertyPhysicalURI = URI.create("http://zion.cise.ufl.edu/ontology/properties.xml");		 
		 propertyOntology = propertyManager.loadOntologyFromPhysicalURI(propertyPhysicalURI);
		 		 
	}
	/**
	 * Will insert the next available index for this name and return the new name for the context
	 * @param name
	 * @return
	 */
	public String insertIntoHasTable(String name, int index) {
		int context = 0;
		String contextName = null;
		while(true) {
			if (htContext.containsKey(name+"_"+context) == false)
				break;
			else
				context++;
		}		
		if (context == 0) {
			contextName = name;
			htContext.put(contextName, new Integer(index));			
		}			
		else{
			contextName = name+"_"+context;
			htContext.put(contextName, new Integer(index));
		}
			
		return contextName;
		
	}
	/**
	 * Creates the ontology by a given queryid e.g "347"
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length < 1 ){
			System.out.println("Invalid user input!");
			System.exit(0);
		}
		
		queryID = new Integer(args[0]);		
		//Path to save the ontology	locally			
		//String file = "file:/Users/Guillermo/workspace/OWLMorpheus/OntologyFiles/ssq"+args[0]+".xml";
		String file = "file:/C://temp//ontology//SSQ-"+args[0]+".xml";
		
		//Path to get the ontology locally
		//String uploadFile = "C:\\Users\\Guillermo\\workspace\\OWLMorpheus\\OntologyFiles\\ssq"+args[0]+".xml";
		String uploadFile = "C:\\temp\\ontology\\SSQ-"+args[0]+".xml";
		
		//String url = "http://zion.cise.ufl.edu/ontology/ssq/"+args[0]+".xml";
		String url = "http://zion.cise.ufl.edu/ontology/SSQ-";
		try {
			OWLCreator owl = new OWLCreator(url, file, queryID);	
        
			//Upload file to the server
			filesystem.FTPUploadFile.transferZion(uploadFile, "/var/www/ontology/");
			
			System.out.println(url+args[0]+".xml");
		}
		catch(Exception e) {
			//System.out.println("Error creating Ontology\n" + e);
		}		
	}
	
	
}