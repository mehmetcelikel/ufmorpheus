

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;

public class OntologyCreator {
	public static OWLOntologyManager manager;
	public static OWLOntology ontology;
	public static OWLDataFactory factory;
	public static URI ontologyURI;
	public static OWLAxiom axiom;
	
	public static void main(String[] args) {
		//OWLOntology ontology = createOntology("http://morpheus.cise.ufl.edu/sample", "file:/temp/testOntology.xml");
		OWLOntology ontology = createOntology("http://morpheus.cise.ufl.edu/sample", "file:/Users/Guillermo/workspace/OWLMorpheus/OntologyFiles/testOntology1.xml");
	}
	
	public static OWLOntology createOntology(String uri, String file) {
		manager = OWLManager.createOWLOntologyManager();
		
		ontologyURI = URI.create(uri);
		URI physicalURI = URI.create(file);
		SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		manager.addURIMapper(mapper);

		try {
			ontology = manager.createOntology(ontologyURI);
			factory = manager.getOWLDataFactory();
			
			OWLClass queryClass = addClass("#QueryClass");
			OWLClass ssq = addClass("#SSQ");
			OWLClass thing = addClass("http://www.w3.org/2002/07/owl#Thing");

			
			/*
			OWLObjectProperty hasRealmProp = factory.getOWLObjectProperty(URI.create(ontologyURI + "#hasRealm"));
			addAxiom(manager, ontology, factory.getOWLDeclarationAxiom(hasRealmProp));
			addAxiom(manager, ontology, factory.getOWLFunctionalObjectPropertyAxiom(hasRealmProp));
			addAxiom(manager, ontology, factory.getOWLSymmetricObjectPropertyAxiom(hasRealmProp));
			addAxiom(manager, ontology, factory.getOWLTransitiveObjectPropertyAxiom(hasRealmProp));
			addAxiom(manager, ontology, factory.getOWLObjectPropertyDomainAxiom(hasRealmProp, factory.getOWLClass()
			addAxiom(manager, ontology, factory.getOWLObjectPropertyRangeAxiom(hasRealmProp, factory.getOWLClass(URI.create("http://www.w3.org/2002/07/owl#Thing"))));	
			 */
			

			manager.saveOntology(ontology);
			System.out.println("Ontology Created");
			return ontology;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void addAxiom(OWLOntologyManager manager, OWLOntology ont, OWLAxiom axiom) throws OWLOntologyChangeException {
		AddAxiom addAxiom = new AddAxiom(ont, axiom);
		manager.applyChange(addAxiom);
	}
	private OWLClass addClass(String name) throws OWLOntologyChangeException {
		OWLClass oClass = factory.getOWLClass(URI.create(ontologyURI + "#QueryClass"));
		OWLAxiom axiom = factory.getOWLDeclarationAxiom(oClass);
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		manager.applyChange(addAxiom);
		return oClass;
	}
	
}