/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;


import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import uf.morpheus.meta.Constants;
import uf.morpheus.meta.MessageLogger;


/**
 * This class helps to calculate the class divergence 
 * between the classes in the ontology  
 * 
 * @author Clint P. George
 *
 */
public class ClassDivergence {
	
	/**
	 * Member class definitions
	 * 
	 */
	public class TNode implements Comparable<TNode>{
		public OWLDescription oClass = null;
		public long value = 0;
		
		public TNode(OWLDescription OWLClass, long val){
			this.oClass = OWLClass;
			this.value = val;
		}
		
		@Override
		public int compareTo(TNode o) {
			return (this.value == o.value)?0:((this.value < o.value)?1:-1);
		}
	}
	
	/**
	 * Variable declarations 
	 * 
	 */
	private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private Reasoner reasoner = null;
	private URI physicalURI = null;
	private URI logicalURI = null;
	private OWLOntology loadedOntology;
	private static double ontologyTreeHeight = 0.0;
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
	

	/**
	 * Constructor: loads the OWL Classes Ontology    
	 * */
	public ClassDivergence(
			String ontology) throws OWLOntologyCreationException 
	{
		msg.logger.log(Level.INFO, "Loading the class ontology from " + ontology);
		
		this.physicalURI = URI.create(ontology);

		// Loads the ontology from the URI...
		try {
			this.loadedOntology = manager.loadOntologyFromPhysicalURI(this.physicalURI);
		} catch (OWLOntologyCreationException e) {
			msg.logger.log(Level.SEVERE, "Error in loading the ontology: " + e.getMessage());
		}
		
		this.logicalURI = this.loadedOntology.getURI();
		
		// Creates a reasoner for an SSQ Ontology...
		this.reasoner = new Reasoner(manager);
		this.reasoner.loadOntology(this.loadedOntology);
		
		// Calculates the tree height... 
		ontologyTreeHeight = findOntologyTreeHeight();
		
		
		msg.logger.log(Level.INFO, "DONE!");
	
	}
	
	/**
	 * Gets the class object in the ontology based on the given class name  
	 * 
	 */
	private OWLClass getClass(String name) {
		OWLClass owlClass = null;
		
        for(OWLClass cls : this.loadedOntology.getReferencedClasses()) {        	
        	if (cls.toString().toUpperCase().compareTo(name.toUpperCase()) == 0) {        		
        		owlClass = cls;
        		break;
        	}
        	
        }
        return owlClass;
	}
	
	/**
	 * Finds the class divergence for the given Classes 
	 * 
	 * @param sourceClass - source class name 
	 * @param targetClass - target class name 
	 * @param symmetric - find symmetric distance (true) or not (false)
	 * 
	 */
	public double findClassDivergence(
			String sourceClass, 
			String targetClass, 
			boolean symmetric)
	{
		double div = this.findOWLClassDivergence(sourceClass, targetClass);
		
		if (symmetric){
			double cdiv2 = this.findOWLClassDivergence(targetClass, sourceClass);
			return (div + cdiv2) * 0.5;
		}
		else 
			return div;
	}
	
	/**
	 * Finds the class divergence measure (asymmetric) for the given OWL classes 
	 * 
	 * @param sourceClass - source class name 
	 * @param targetClass - target class name 
	 */
	public double findOWLClassDivergence(
			String sourceClass, 
			String targetClass)
	{
		
		// Finds the source and target classes in the 
		// ontology and validates them 
		OWLDescription source = getClass(sourceClass); 
		OWLDescription target = getClass(targetClass); 
		
		if (source == null || target == null) {
			msg.logger.log(Level.SEVERE, "ERROR: " + sourceClass + " or " 
					+ targetClass 
					+ " are not find in the class ontology repository (" 
					+ this.physicalURI + ").");

			return Constants.DISSIMILARITY;
		}
				
		double divergence = 0.0;
		
		// Case 1: when the URI are similar 
		if (source.asOWLClass().getURI().equals(target.asOWLClass().getURI())){
			divergence = 0.0;
		}
		// Case 2: when the source class is an ancestor of the target class
		else if (isAncestor(source, target)){
			divergence = hierarchicDistance(source, target) / (3*ontologyTreeHeight);
		}
		// Case 3: when the source class is an descendant of the target class
		else if (isAncestor(target, source)){
			divergence = 1.0;
		}
		// Case 4:
		else {
			// Finds the common ancestor
			OWLDescription commonAncestor = findCommonAncestor(source, target);

			if (commonAncestor == null){
				divergence = 1.0;
			}
			else {	
				// Finds the distance to the root from the source
				double distRootSource = hierarchicDistance(getClass(Constants.DBPEDIA_ROOT_CLASS_NAME), source); //recursiveRootDistance(source);
				// Finds the distance to the common ancestor from source and target 
				double distAncestorSourse = hierarchicDistance(commonAncestor, source);
				double distAncestorTarget = hierarchicDistance(commonAncestor, target);
				
				divergence = (distRootSource + distAncestorSourse + distAncestorTarget)	/ (3*ontologyTreeHeight);
			}
		}	
		
		return divergence;		
	}

	
	/**
	 * Finds the ontology tree height for a root node
	 * 
	 */
	private double findOntologyTreeHeight() 
	{
		OWLClass cls = getClass(Constants.DBPEDIA_ROOT_CLASS_NAME);	
		
		if (cls == null){
			msg.logger.log(Level.SEVERE, "Invalid class repository! EXIT");
			System.exit(0);
		}
		
		
		Set <OWLIndividual> indSet= cls.getIndividuals(this.loadedOntology);
		for (OWLIndividual ind : indSet){
			Map<OWLDataPropertyExpression, Set<OWLConstant>> mp =  ind.getDataPropertyValues(loadedOntology);
			
			for (OWLDataPropertyExpression exp : mp.keySet()){
				if (exp.toString().equalsIgnoreCase(Constants.DBPEDIA_PROPERTY_TREE_HEIGHT)){
					for (OWLConstant c : mp.get(exp)){
						return Double.parseDouble(c.getLiteral());
					}
				}
			}
			
		}
		return 0.0;
		/*
		OWLDescription root = getOntologyRoot();
		long len = 0;
		Stack<TNode> ontoStack = new Stack<TNode>();
		
		for (OWLDescription cd : root.asOWLClass().getSubClasses(this.loadedOntology))
			ontoStack.add(new TNode(cd, 1));
		
		while(!ontoStack.isEmpty()){
			TNode node = ontoStack.pop();
			Set <OWLDescription> descendants = node.oClass.asOWLClass().getSubClasses(this.loadedOntology);
			
			if (descendants.size() == 0){
				System.out.println("Tree Height: " + node.value);
				if (node.value > len) // Finds the largest values (discards all other leaf nodes)
					len = node.value;
			}
			else {
				for (OWLDescription cd : descendants)
					ontoStack.add(new TNode(cd, node.value+1));
			}
		}

		return len;
		*/
	}

	/**
	 * Finds the common ancestor of the given classes
	 * 
	 * TODO make it shortest common ancestor
	 */
	private OWLDescription findCommonAncestor(OWLDescription source,
			OWLDescription target) {

		Queue<TNode> aQueue = new PriorityQueue<TNode>();
		aQueue.add(new TNode(source, 0));
		ArrayList<String> visitedC = new ArrayList<String>();
		OWLDescription ret = null; // if there is no common ancestor
		long hops = -1;
		
		while (!aQueue.isEmpty()) {
			TNode ancestor = aQueue.remove();

			if (ancestor.oClass.isAnonymous())
				continue;

			// This is in order to avoid the cycles
			if (visitedC.contains(ancestor.oClass.toString()))
				continue;
			else
				visitedC.add(ancestor.oClass.toString());

			if (isAncestor(ancestor.oClass, target)) {
				
				long right = hierarchicDistance(ancestor.oClass, target);
				
				if ((hops < 0) 
						||(((right + ancestor.value) < hops) 
								&& ((right + ancestor.value) <= 2*ontologyTreeHeight))) {
					ret = ancestor.oClass;
					hops = (right + ancestor.value);
				}
				
			} else if ((hops < 0) || (ancestor.value + 1 < hops)){ // this heuristic needs to be improved 
				
				Set<OWLDescription> ancClasses = ancestor.oClass.asOWLClass()
						.getSuperClasses(this.loadedOntology);
				
				for (OWLDescription anr : ancClasses)
					aQueue.add(new TNode(anr, ancestor.value + 1));
			}
		}

		return ret;
	}

	
	/**
	 * Finds the hierarchical distance from the child to the parent
	 * 
	 */
	private long hierarchicDistance(OWLDescription parent, OWLDescription child) {

		Stack<TNode> ancStack = new Stack<TNode>();
		TNode node = new TNode(child, 0);
		ancStack.add(node);
		ArrayList<String> visitedC = new ArrayList<String>();
		long hops = -1;

		while (!ancStack.isEmpty()) {
			TNode ancestor = ancStack.pop();

			if (ancestor.oClass.isAnonymous()
					|| ancestor.value > ontologyTreeHeight // Constraints to less than tree height 
					|| ((hops > 0) && (ancestor.value >= hops))) // We don't need any other values > hops 
				continue;	

			// This is in order to avoid the cycles
			if (visitedC.contains(ancestor.oClass.toString()))
				continue;
			else
				visitedC.add(ancestor.oClass.toString());

			if (parent.asOWLClass().getURI().equals(
					ancestor.oClass.asOWLClass().getURI())) {
				if ((hops < 0) || (hops > ancestor.value))
					hops = ancestor.value;
			} 
			else if ((hops < 0) || ((ancestor.value + 1) < hops)){
				
				Set <OWLDescription> anc = ancestor.oClass.asOWLClass()
					.getSuperClasses(this.loadedOntology);
				for (OWLDescription anr : anc)
					ancStack.add(new TNode(anr, ancestor.value + 1));
			}
		}

		return hops;
	}

	/**
	 * Checks whether the given class is ancestor to the class 
	 * 
	 * TODO we may need to improve this logic, there could be 
	 * chances of memory issues  
	 */
	public boolean isAncestor(
			OWLDescription ancestorClass, 
			OWLDescription descendantClass)
	{
		// Validates the input 
		if (ancestorClass.isOWLNothing()) return false;
		if (descendantClass.isOWLNothing()) return false;
		
		Stack<OWLDescription> ancStack = new Stack<OWLDescription>();
		ancStack.add(descendantClass);
		ArrayList <String> visitedC = new ArrayList<String>();  
		
		while(!ancStack.isEmpty())
		{
			OWLDescription ancestor = ancStack.pop();

			if (ancestor.isAnonymous())
				continue;

			// This is in order to avoid the cycles
			if (visitedC.contains(ancestor.toString()))
				continue;
			else
				visitedC.add(ancestor.toString());

			if (ancestorClass.asOWLClass().getURI().equals(
					ancestor.asOWLClass().getURI())) {
				return true;
			} else {
				Set<OWLDescription> anc = ancestor.asOWLClass()
						.getSuperClasses(this.loadedOntology);
				for (OWLDescription anr : anc)
					ancStack.add(anr);
			}
		}
		
				
		return false;
	}


	/**
	 * Function main()
	 * 
	 * @param args
	 * 
	 * e.g. Automotive_templates Automotive_styling_features "file:/C://temp//Automotive.xml"
	 */
	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.println("Invalid arguments");
			System.exit(0);
		}

		try {
			ClassDivergence cd = new ClassDivergence(args[2]);

			System.out.printf(
					"\nThe class divergence between %s and %s : %6.4f\n",
					args[0], args[1], cd.findClassDivergence(args[0], args[1],
							false));
			System.out.printf(
					"\nThe class divergence (symmetric) between %s and %s : %6.4f\n",
					args[0], args[1], cd.findClassDivergence(args[0],
							args[1], true));

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}


}
