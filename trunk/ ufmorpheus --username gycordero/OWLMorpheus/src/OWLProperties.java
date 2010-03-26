import java.net.URI;

import org.semanticweb.owl.apibinding.OWLManager;

import org.semanticweb.owl.model.OWLAxiom;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;

import db.Context;

import db.DBReader;



public class OWLProperties {
	public String uri = null;
	public String file = null;	
	public static OWLOntologyManager manager;
	public static OWLOntology ontology;
	public static OWLDataFactory factory;
	public static URI ontologyURI;
	public static OWLAxiom axiom;
	
	public static OWLOntologyManager classManager; //Manager for Class ontology
	public static URI physicalURI; 
	public static OWLOntology classOntology; 

	public OWLProperties() {
		createProperties();
	}
	
	public void createProperties() {
		
		
		
		uri = "http://zion.cise.ufl.edu/ontology/properties.xml";
		//file = "http://zion.cise.ufl.edu/ontology/Properties/OWLProperties.xml";
		file = "file:/Users/Guillermo/workspace/OWLMorpheus/OntologyFiles/OWLProperties.xml";
		manager = OWLManager.createOWLOntologyManager();		
		ontologyURI = URI.create(uri);
		URI physicalURI = URI.create(file);
		SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		manager.addURIMapper(mapper);

		manager = OWLManager.createOWLOntologyManager();		
	
		
		manager.addURIMapper(mapper);

		try {
			
			importClassesOntology(); // Import class Ontology
			
			ontology = manager.createOntology(ontologyURI);
			factory = manager.getOWLDataFactory();
			
			addContextProperties(); // Add the properties from the context table on the database
			addDefaultProperties();//Add all properties
		
			manager.saveOntology(ontology);
			System.out.println("Ontology classes Created");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

		private void addAxiom(OWLAxiom axiom) throws OWLOntologyChangeException {
			AddAxiom addAxiom = new AddAxiom(ontology, axiom);
			manager.applyChange(addAxiom);
		}
		private void addContextProperties() throws OWLOntologyChangeException {
			Context[] contexts = DBReader.getContexts();
			OWLObjectProperty property;
			
			
			//Add contextProperties with domain and range
			for (int x = 0; x < contexts.length; x++) {
				property = factory.getOWLObjectProperty(URI.create(ontologyURI + "#" + contexts[x].getContextName() + "_input"));
				addAxiom(factory.getOWLDeclarationAxiom(property));			
				addAxiom(factory.getOWLObjectPropertyDomainAxiom(property, getImportedClass("QueryClass")));			
				addAxiom(factory.getOWLObjectPropertyRangeAxiom(property, getImportedClass("Context")));
				
				property = factory.getOWLObjectProperty(URI.create(ontologyURI + "#" + contexts[x].getContextName() + "_output"));
				addAxiom(factory.getOWLDeclarationAxiom(property));			
				addAxiom(factory.getOWLObjectPropertyDomainAxiom(property, getImportedClass("QueryClass")));			
				addAxiom(factory.getOWLObjectPropertyRangeAxiom(property, getImportedClass("Context")));

			}
		}
		private OWLClass getImportedClass(String name) {
			OWLClass owlClass = null;
			
			// This for loop will look on the ontology for the class given by name
	        for(OWLClass cls : classOntology.getReferencedClasses()) {        	
	        	if (cls.toString().compareTo(name) == 0) {        		
	        		owlClass = cls;
	        		System.out.println("The class founded is: " + owlClass);
	        		break;
	        	}
	            
	        }
	        return owlClass;
		}
		private void addDefaultProperties() throws OWLOntologyChangeException {
			OWLObjectProperty property;
			property = factory.getOWLObjectProperty(URI.create("#ValueWithinContext"));
			addAxiom(factory.getOWLDeclarationAxiom(property));	
			
			property = factory.getOWLObjectProperty(URI.create("#hasRank"));
			addAxiom(factory.getOWLDeclarationAxiom(property));
			
			property = factory.getOWLObjectProperty(URI.create("#hasModifier"));
			addAxiom(factory.getOWLDeclarationAxiom(property));
			
			property = factory.getOWLObjectProperty(URI.create("#hasModifierList"));
			addAxiom(factory.getOWLDeclarationAxiom(property));
			
			property = factory.getOWLObjectProperty(URI.create("#belongsToClass"));
			addAxiom(factory.getOWLDeclarationAxiom(property));
			
			property = factory.getOWLObjectProperty(URI.create("#hasQueryID"));
			addAxiom(factory.getOWLDeclarationAxiom(property));
			
			OWLClass oClass = factory.getOWLClass(URI.create("http://www.w3.org/2002/07/owl#Thing"));
			OWLAxiom axiom = factory.getOWLDeclarationAxiom(oClass);
			addAxiom(axiom);
			
			// Adding realm
			OWLObjectProperty hasRealmProp = factory.getOWLObjectProperty(URI.create(ontologyURI + "#hasRealm"));
			addAxiom(factory.getOWLDeclarationAxiom(hasRealmProp));
			addAxiom(factory.getOWLFunctionalObjectPropertyAxiom(hasRealmProp));
			addAxiom(factory.getOWLSymmetricObjectPropertyAxiom(hasRealmProp));
			addAxiom(factory.getOWLTransitiveObjectPropertyAxiom(hasRealmProp));
			addAxiom(factory.getOWLObjectPropertyDomainAxiom(hasRealmProp, getImportedClass("QueryClass")));
			addAxiom(factory.getOWLObjectPropertyRangeAxiom(hasRealmProp,  oClass));	
			 
			
		}
		public void importClassesOntology() throws OWLOntologyCreationException {
			 classManager = OWLManager.createOWLOntologyManager(); // Manager for Class ontology
			 //physicalURI = URI.create("http://www.co-ode.org/ontologies/pizza/2007/02/12/pizza.owl");
			 physicalURI = URI.create("http://zion.cise.ufl.edu/ontology/classes/OWLClasses.xml");
			 //manager.lo
			 classOntology = classManager.loadOntologyFromPhysicalURI(physicalURI);
			 
			 /*
	         for(OWLClass cls : classOntology.getReferencedClasses()) {
	             System.out.println(cls);
	         }
	         */
		}

		public static void main(String args[]) {
			OWLProperties properties = new OWLProperties();
		}
}




