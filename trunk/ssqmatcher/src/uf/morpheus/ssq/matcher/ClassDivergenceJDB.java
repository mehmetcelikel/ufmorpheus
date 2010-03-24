/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.Level;


import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

import uf.morpheus.db.PersistentOntology;
import uf.morpheus.meta.Constants;
import uf.morpheus.meta.MessageLogger;


/**
 * This class helps to calculate the class divergence 
 * between the classes in the ontology in the db   
 * 
 * @author Clint P. George
 *
 */
public class ClassDivergenceJDB {
	
	/**
	 * Member class definitions
	 * 
	 */
	public class TNode implements Comparable<TNode>{
		public OntClass oClass = null;
		public long value = 0;
		
		public TNode(OntClass OWLClass, long val){
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
	private String ontoURI = null;
	private static double ontologyTreeHeight = 0.0;
	private MessageLogger msg = MessageLogger.getInstance();
	private OntModel model = null;
	private PersistentOntology po = null;
	private String NS = "";
	
	/**
	 * Property definitions ...
	 *
	 */
	public String getLogicalURI() {
		return ontoURI;
	}



	/**
	 * Constructor: loads the OWL Classes Ontology    
	 * */
	public ClassDivergenceJDB(String ontology)
	{
		// Loads the ontology from the URI...
		po = new PersistentOntology();
		model = po.getOntModel(ontology); 
		
		this.ontoURI = ontology;
		this.NS = ontology + "#";

		// Calculates the tree height... 
		ontologyTreeHeight = findOntologyTreeHeight();
	}
	
	/**
	 * Gets the class object in the ontology based on the given class name  
	 * 
	 */
	private OntClass getClass(String name) {
		OntClass owlClass = null;

		owlClass = model.getOntClass(NS + name);
		
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
		OntClass source = getClass(sourceClass); 
		OntClass target = getClass(targetClass); 
		
		if (source == null || target == null) {
			msg.logger.log(Level.SEVERE, "ERROR: " + sourceClass + " or " 
					+ targetClass 
					+ " are not find in the class ontology repository.");

			return Constants.DISSIMILARITY;
		}
				
		double divergence = 0.0;
		
		// Case 1: when the URI are similar 
		if (source.getURI().equals(target.getURI())){
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
			OntClass commonAncestor = findCommonAncestor(source, target);

			if (commonAncestor == null){
				divergence = 1.0;
			}
			else {	
				// Finds the distance to the root from the source
				double distRootSource = hierarchicDistance(
						getClass(Constants.DBPEDIA_ROOT_CLASS_NAME), source); 
				// Finds the distance to the common ancestor from source and
				// target
				double distAncestorSourse = hierarchicDistance(commonAncestor,
						source);
				double distAncestorTarget = hierarchicDistance(commonAncestor,
						target);

				divergence = (distRootSource + distAncestorSourse + distAncestorTarget)
						/ (3 * ontologyTreeHeight);
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
		OntClass cls = getClass(Constants.DBPEDIA_ROOT_CLASS_NAME);	
		
		if (cls == null){
			msg.logger.log(Level.SEVERE, "Invalid class repository! EXIT");
			System.exit(0);
		}
		
		Property p1 = model.getProperty(NS + Constants.DBPEDIA_PROPERTY_TREE_HEIGHT);
        Statement stmt = cls.getProperty(p1);
        
        //Resource  subject   = stmt.getSubject();     // get the subject
        //Property  predicate = stmt.getPredicate();   // get the predicate
        RDFNode   object    = stmt.getObject();      // get the object

        //System.out.print(subject.toString());
        //System.out.print(" " + predicate.toString() + " ");
        if (object instanceof Literal) {
            // object is a literal
            //System.out.print(" " + object.toString() + " \n");
            return Double.parseDouble(object.toString());
        }
		
		
		
		return 0.0;
	}

	/**
	 * Finds the common ancestor of the given classes
	 * 
	 * TODO make it shortest common ancestor
	 */
	private OntClass findCommonAncestor(OntClass source,
			OntClass target) {

		Queue<TNode> aQueue = new PriorityQueue<TNode>();
		aQueue.add(new TNode(source, 0));
		ArrayList<String> visitedC = new ArrayList<String>();
		OntClass ret = null; // if there is no common ancestor
		long hops = -1;
		
		while (!aQueue.isEmpty()) {
			TNode ancestor = aQueue.remove();

			if (ancestor.oClass.isAnon())
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
				
				for (Iterator<OntClass> i = ancestor.oClass
						.listSuperClasses(true); i.hasNext();) {
					OntClass anr = i.next();
					aQueue.add(new TNode(anr, ancestor.value + 1));
				}
				
			}
		}

		return ret;
	}

	
	/**
	 * Finds the hierarchical distance from the child to the parent
	 * 
	 */
	private long hierarchicDistance(OntClass parent, OntClass child) {

		Stack<TNode> ancStack = new Stack<TNode>();
		TNode node = new TNode(child, 0);
		ancStack.add(node);
		ArrayList<String> visitedC = new ArrayList<String>();
		long hops = -1;

		while (!ancStack.isEmpty()) {
			TNode ancestor = ancStack.pop();

			if (ancestor.oClass.isAnon()
					|| ancestor.value > ontologyTreeHeight // Constraints to less than tree height 
					|| ((hops > 0) && (ancestor.value >= hops))) // We don't need any other values > hops 
				continue;	

			// This is in order to avoid the cycles
			if (visitedC.contains(ancestor.oClass.toString()))
				continue;
			else
				visitedC.add(ancestor.oClass.toString());

			if (parent.getURI().equals(ancestor.oClass.getURI())) {
				if ((hops < 0) || (hops > ancestor.value))
					hops = ancestor.value;
			} 
			else if ((hops < 0) || ((ancestor.value + 1) < hops)) {

				for (Iterator<OntClass> i = ancestor.oClass
						.listSuperClasses(true); i.hasNext();) {
					OntClass anr = i.next();
					ancStack.add(new TNode(anr, ancestor.value + 1));
				}
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
			OntClass ancestorClass, 
			OntClass descendantClass)
	{
		// Validates the input 
		if (!ancestorClass.isClass() || !descendantClass.isClass()) return false;
		
		Stack<OntClass> ancStack = new Stack<OntClass>();
		ancStack.add(descendantClass);
		ArrayList <String> visitedC = new ArrayList<String>();  
		
		while(!ancStack.isEmpty())
		{
			OntClass ancestor = ancStack.pop();

			if (ancestor.isAnon())
				continue;

			// This is in order to avoid the cycles
			if (visitedC.contains(ancestor.toString()))
				continue;
			else
				visitedC.add(ancestor.toString());

			if (ancestorClass.getURI().equals(
					ancestor.getURI())) {
				return true;
			} else {
		        for (Iterator<OntClass> i = ancestor.listSuperClasses(true); i.hasNext(); ) {
		            OntClass anr = i.next();
					ancStack.add(anr);
				}
			}
		}
		
				
		return false;
	}


	/**
	 * Function main()
	 * 
	 * @param args
	 * 
	 * e.g. Automobiles Car_classifications "http://zion.cise.ufl.edu/ontology/classes/Automobiles"
	 */
	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.println("Invalid arguments");
			System.exit(0);
		}

		ClassDivergenceJDB cd = new ClassDivergenceJDB(args[2]);

		System.out.printf(
				"\nThe class divergence between %s and %s : %6.4f\n",
				args[0], args[1], cd.findClassDivergence(args[0], args[1],
						false));
		System.out.printf(
				"\nThe class divergence (symmetric) between %s and %s : %6.4f\n",
				args[0], args[1], cd.findClassDivergence(args[0],
						args[1], true));


	}


}
