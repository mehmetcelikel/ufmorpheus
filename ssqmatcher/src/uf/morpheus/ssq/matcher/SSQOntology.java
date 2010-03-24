/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import uf.morpheus.meta.Constants;
import uf.morpheus.meta.MessageLogger;
import uf.morpheus.meta.Constants.SSQContexts;

/**
 * This class represents SSQ ontology in OWL format 
 * 
 * @author Clint P. George
 * 
 */
public class SSQOntology {
	/**
	 * Variable declarations
	 * 
	 */
	private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private Reasoner reasoner = null;
	private URI physicalURI = null;
	private URI logicalURI = null;
	private OWLClass queryClass = null;
	private OWLDescription realm = null;
	private Set<BaseContext> baseContexts = new HashSet<BaseContext>();
	private Set<ModifiedContext> modifiedContexts = new HashSet<ModifiedContext>();
	private OWLOntology loadedOntology;
	private MessageLogger msg = MessageLogger.getInstance();
	/**
	 * Property definitions ...
	 * 
	 */
	public URI getLogicalURI() {
		return logicalURI;
	}

	public URI getPhysicalURI() {
		return physicalURI;
	}

	public OWLClass getQueryClass() {
		return queryClass;
	}

	public OWLDescription getRealm() {
		return realm;
	}

	public Set<BaseContext> getBaseContexts() {
		return baseContexts;
	}

	public Set<ModifiedContext> getModifiedContexts() {
		return modifiedContexts;
	}

	/**
	 * This function returns a base Context if it matches with the given context
	 * name of the SSQ
	 * 
	 * @param contextName
	 *            SSQ context name
	 */
	public BaseContext getBaseContext(SSQContexts context) {
		for (BaseContext ct : this.baseContexts)
			if (ct.getContext() == context)
				return ct;

		return null;
	}

	/**
	 * This function returns a modified Context if it matches with the given
	 * context name of the SSQ
	 * 
	 * @param contextName
	 *            SSQ context name
	 */
	public ModifiedContext getModifiedContext(SSQContexts context) {
		for (ModifiedContext ct : this.modifiedContexts)
			if (ct.getContext() == context)
				return ct;

		return null;
	}

	/**
	 * This function adds the base context details
	 * 
	 * @param contextName
	 *            SSQ context name
	 * @param ranges
	 *            context ranges (OWL Classes)
	 * 
	 */
	public BaseContext addBaseContextRanges(SSQContexts context,
			Set<OWLDescription> ranges) {
		BaseContext c = this.getBaseContext(context);

		if (c != null) {
			for (OWLDescription ind : ranges)
				c.addRanges(ind);
		} else {
			c = new BaseContext(context);
			c.setRange(ranges);
			this.baseContexts.add(c);
		}

		return c;
	}
	
	/**
	 * This function adds the base context details
	 * 
	 * @param contextName
	 *            SSQ context name
	 * @param ranges
	 *            context range (OWL Class)
	 * 
	 */
	public BaseContext addBaseContextRange(SSQContexts context,
			OWLDescription range) {
		BaseContext c = this.getBaseContext(context);

		if (c != null) {
			c.addRanges(range);
		} else {
			c = new BaseContext(context);
			c.addRanges(range);
			this.baseContexts.add(c);
		}

		return c;
	}

	/**
	 * Class constructor
	 * 
	 * @param baseURI
	 *            The URI for the ontology to be loaded
	 * 
	 */
	public SSQOntology(URI baseURI) {
		this.physicalURI = baseURI;

		// Loads the SSQ ontology from the URI...
		try {
			this.loadedOntology = manager.loadOntologyFromPhysicalURI(this.physicalURI);
		} catch (OWLOntologyCreationException e) {
			msg.logger.log(Level.SEVERE, "Error in loading the ontology: " + e.getMessage());
		}

		this.logicalURI = this.loadedOntology.getURI();

		// Creates a reasoner for an SSQ Ontology...
		this.reasoner = new Reasoner(manager);
		this.reasoner.loadOntology(this.loadedOntology);
		
		// Loads the SSQ details...
		this.loadSSQDetails();
	}
	
	/**
	 * Loads the SSQ details from the loaded OWL SSQ Ontology...
	 * 
	 */
	private void loadSSQDetails() {

		// Query class
		this.queryClass = this.getClass(Constants.SSQ_QUERY_CLASS_NAME);
		OWLObjectProperty belongsToClassProperty = this.getObjectProperty(Constants.SSQ_CONTEXT_PROPERTY_BELONGSTOCLASS);
		Set <OWLIndividual> ssqSub = this.reasoner.getIndividuals(this.getClass(Constants.SSQ_CLASS_NAME), false);
		
		// Gets the individuals of SSQ
		for (OWLIndividual cls : ssqSub) {
			
			Map<OWLObjectProperty, Set<OWLIndividual>> op = this.reasoner.getObjectPropertyRelationships(cls);
			Set <OWLObjectProperty> opSet = getIndividualObjectProperties(cls);
			
			for (OWLObjectProperty p : opSet) {
				Set<OWLIndividual> c = op.get(p);

				if (p.toString().equalsIgnoreCase(
						Constants.SSQ_REALM_PROPERTY_NAME)) {
					for (OWLIndividual i : c)
						this.realm = getClass(i.toString());
					//msg.logger.log(Level.INFO, "loaded the SSQ realm class : " + this.realm);
				} else {
					for (OWLIndividual i : c) {
						// Need to find the classes associated with the
						// individual
						Map<OWLObjectProperty, Set<OWLIndividual>> mp = this.reasoner
								.getObjectPropertyRelationships(i);

						for (OWLObjectProperty contextProperty : mp.keySet())
							// we only consider the property 'belongsToClass'
							if (contextProperty.equals(belongsToClassProperty))
								for (OWLIndividual ind : mp.get(contextProperty))
									this.addBaseContextRanges(SSQContexts.getContext(p.toString()),
											getIndividualTypes(ind, false));

						//msg.logger.log(Level.INFO, "loaded the SSQ context " + p.toString());
					}
				}

			}

		}
	}
	

	/**
	 * Gets the class object in the ontology based on the given class name
	 * 
	 */
	public OWLClass getClass(String name) {
		OWLClass owlClass = null;

		// This for loop will look on the ontology for the class given by name
		for (OWLClass cls : this.loadedOntology.getReferencedClasses()) {
			if (cls.toString().compareTo(name) == 0) {
				owlClass = cls;
				break;
			}
		}
		return owlClass;
	}
	
	/**
	 * Gets the property object in the ontology based on the given property name
	 * 
	 */
	public OWLObjectProperty getObjectProperty(String name) {
		OWLObjectProperty pty = null;

		// This for loop will look on the ontology for the class given by name
		for (OWLObjectProperty p : this.loadedOntology.getReferencedObjectProperties()) {
			if (p.toString().compareTo(name) == 0) {
				pty = p;
				break;
			}
		}
		return pty;
	}

	public Set<OWLObjectProperty> getIndividualObjectProperties(
			OWLIndividual indinvidual) {
		Map<OWLObjectProperty, Set<OWLIndividual>> mp = this.reasoner
				.getObjectPropertyRelationships(indinvidual);
		return mp.keySet();
	}

	public Set<OWLDataProperty> getIndividualDataProperties(
			OWLIndividual indinvidual) {
		Map<OWLDataProperty, Set<OWLConstant>> mp = this.reasoner
				.getDataPropertyRelationships(indinvidual);
		return mp.keySet();
	}

	public Set<OWLDescription> getAncestorClasses(OWLDescription cls) {
		Set<OWLDescription> ancestors = cls.asOWLClass().getSuperClasses(
				this.loadedOntology);

		return ancestors;
	}

	public Set<OWLDescription> getDescendantClasses(OWLDescription cls) {
		Set<OWLDescription> descendants = cls.asOWLClass().getSubClasses(
				this.loadedOntology);

		return descendants;
	}

	/**
	 * Gets a OWL OWLDescription's instances as a set of OWLIndividuals
	 * 
	 * @param clsC
	 *            an OWLDescription
	 * @param direct
	 *            direct individual or not
	 */
	public Set<OWLIndividual> getClassIndividuals(
			OWLDescription clsC,
			boolean direct) {
		return this.reasoner.getIndividuals(clsC, direct);
	}

	/**
	 * Gets an OWL individual's types as a set of OWLDescriptions
	 * 
	 * @param ind
	 *            an OWLIndividual instance
	 * @param direct
	 *            direct type or not
	 */
	public Set<OWLDescription> getIndividualTypes(
			OWLIndividual ind,
			boolean direct) {
		Set<OWLDescription> indCls = new HashSet<OWLDescription>();

		for (Set<OWLClass> indClsS : this.reasoner.getTypes(ind, direct))
			for (OWLClass cls : indClsS)
				if (!cls.isOWLThing())
					indCls.add(cls);				

		return indCls;
	}

}
