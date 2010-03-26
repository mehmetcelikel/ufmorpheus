import java.net.URI;

import org.semanticweb.owl.apibinding.OWLManager;

import org.semanticweb.owl.model.OWLAxiom;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;

import db.ContextClass;
import db.DBReader;
import db.Query;
import db.Realm;


public class OWLClasses {
	public String uri = null;
	public String file = null;	
	public static OWLOntologyManager manager;
	public static OWLOntology ontology;
	public static OWLDataFactory factory;
	public static URI ontologyURI;
	public static OWLAxiom axiom;
	
	public OWLClasses() {
		createClasses();
	}
	
	public void createClasses() {
		uri = "http://zion.cise.ufl.edu/ontology/classes.xml";
		file = "file:/C://temp//ontology//classes.xml";
		manager = OWLManager.createOWLOntologyManager();		
		ontologyURI = URI.create(uri);
		URI physicalURI = URI.create(file);
		SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		manager.addURIMapper(mapper);

		manager = OWLManager.createOWLOntologyManager();		
	
		
		manager.addURIMapper(mapper);

		try {
			ontology = manager.createOntology(ontologyURI);
			factory = manager.getOWLDataFactory();

			
			addDefaultClasses();// Adding default classes to the ontology
			//addAllTableClasses(); // Add the classes from the class table on the database
			//addRealms();// Add the realms from the table ream
			
			manager.saveOntology(ontology);
			System.out.println("Ontology classes Created");
			
			String uploadFile = "C:\\temp\\ontology\\classes.xml";
			
			//String url = "http://zion.cise.ufl.edu/ontology/ssq/"+args[0]+".xml";
			String url = "http://zion.cise.ufl.edu/ontology/classes.xml";
			try {				
	        
				//Upload file to the server
				filesystem.FTPUploadFile.transferZion(uploadFile, "/var/www/ontology/");
				
				System.out.println(url);
			}
			catch(Exception e) {
				//System.out.println("Error creating Ontology\n" + e);
			}	
		

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		private OWLClass addClass(String name) throws OWLOntologyChangeException {
			OWLClass oClass = factory.getOWLClass(URI.create(ontologyURI + name));
			OWLAxiom axiom = factory.getOWLDeclarationAxiom(oClass);
			addAxiom(axiom);		
			return oClass;
		}	
		private void addAxiom(OWLAxiom axiom) throws OWLOntologyChangeException {
			AddAxiom addAxiom = new AddAxiom(ontology, axiom);
			manager.applyChange(addAxiom);
		}
		private void addDefaultClasses() throws OWLOntologyChangeException {
			//Creating Classes
			addClass("#QueryClass");
			addClass("#SSQ");			
			addClass("#Context");
			addClass("#ContextClass");
			addClass("#Realm");
			addClass("#Modifier");
			addClass("#ModifierList");
		}
		private void addAllTableClasses() throws OWLOntologyChangeException {
			ContextClass[] classes = DBReader.getContextClasses();
			OWLClass ContextClass = getClass("ContextClass");
			OWLClass currentClass = null;
			
			System.out.println("The lenght of the class array is: " + classes.length);
			for (int x = 0; x < classes.length; x++) {
				//System.out.println("adding class: " + classes[x].getname());
				currentClass = addClass("#"+classes[x].getname());
				addAxiom(factory.getOWLSubClassAxiom(currentClass, ContextClass));
			}
			
		}
		private void addRealms() throws OWLOntologyChangeException {
			Realm[] realms = DBReader.getRealms();
			OWLClass realm = getClass("Realm");
			OWLClass currentClass = null;
			
			System.out.println("The lenght of the realm array is: " + realms.length);
			for(int x = 0; x < realms.length;x++) {
				//System.out.println("Adding realm: " + realms[x].getRealm());
				currentClass = addClass("#"+realms[x].getRealm());
				addAxiom(factory.getOWLSubClassAxiom(currentClass, realm));
			}
			
		}
		private OWLClass getClass(String name) {
			OWLClass owlClass = null;
			
			// This for loop will look on the ontology for the class given by name
	        for( OWLClass cls : ontology.getReferencedClasses()) {        	
	        	if (cls.toString().compareTo(name) == 0) {        		
	        		owlClass = cls;
	        		//System.out.println("The class founded is: " + owlClass);
	        		break;
	        	}
	            
	        }
	        return owlClass;
		}
		public static void main(String args[]) {
			OWLClasses classes = new OWLClasses();
		}
}
