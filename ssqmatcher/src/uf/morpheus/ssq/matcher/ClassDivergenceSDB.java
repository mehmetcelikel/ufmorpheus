/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.Level;

import com.hp.hpl.jena.sdb.Store;

import uf.morpheus.db.SDBHelper;
import uf.morpheus.meta.Constants;
import uf.morpheus.meta.MessageLogger;


/**
 * This class helps to calculate the class divergence 
 * between the classes in the ontology  
 * 
 * @author Clint P. George
 *
 */
public class ClassDivergenceSDB {
	
	/**
	 * Member class definitions
	 * 
	 */
	public class TNode implements Comparable<TNode>{
		public String oClass = null;
		public long value = 0;
		
		public TNode(String child, long val){
			this.oClass = child;
			this.value = val;
		}
		
		@Override
		public int compareTo(TNode o) {
			return (this.value == o.value)?0:((this.value < o.value)?-1:1);
		}
	}
	
	/**
	 * Variable declarations 
	 * 
	 */

	private static double ontologyTreeHeight = 0.0;
	private MessageLogger msg = MessageLogger.getInstance();
	
	private Store store = null;
	
	
	/**
	 * Constructor:   
	 * */
	public ClassDivergenceSDB()
	{
		
		// Gets the default store 
		store = SDBHelper.getStore();
		
		// Calculates the tree height... 
		ontologyTreeHeight = findOntologyTreeHeight();
	
	}
	
	public void closeConnections(){
		SDBHelper.closeStore();
	}
	
	
	/**
	 * Gets the class object in the ontology based on the given class name  
	 * 
	 */
	private String getClass(String name) {
		
		String cls = "";
		
		if (SDBHelper.containsOWLClass(Constants.NS_CLASSES + name, store))
			cls = Constants.NS_CLASSES + name;
		
		return cls;

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
		
		// Finds the source and target classes URI 
		// in the ontology and validates them 
		String source = getClass(sourceClass); 
		String target = getClass(targetClass); 
		
		if (source == null || target == null) {
			msg.logger.log(Level.SEVERE, "ERROR: " + sourceClass + " or " 
					+ targetClass + " are not find in the class repository.");

			return Constants.DISSIMILARITY;
		}
				
		double divergence = 0.0;
		
		// Case 1: when the URI are similar 
		if (source.equalsIgnoreCase(target)){
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
			String commonAncestor = findCommonAncestor(source, target);

			if (commonAncestor == null){
				divergence = 1.0;
			}
			else {	
				// Finds the distance to the root from the source
				double distRootSource = hierarchicDistance(getClass(Constants.DBPEDIA_ROOT_CLASS_NAME), source); 
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
		String cls = getClass(Constants.DBPEDIA_ROOT_CLASS_NAME);	
		
		if (cls == null){
			msg.logger.log(Level.SEVERE, "Invalid class repository! EXIT");
			System.exit(0);
		}
		
		String strTH = Constants.NS_PROPERTIES + Constants.DBPEDIA_PROPERTY_TREE_HEIGHT;
		String l = SDBHelper.getPropertyValue(cls, strTH, store);
		
		if (l != null && l != "")
			return Double.parseDouble(l.toString());

		return 0.0;
	}

	/**
	 * Finds the common ancestor of the given classes
	 * 
	 * TODO make it shortest common ancestor
	 */
	private String findCommonAncestor(String source, String target) {

		Queue<TNode> aQueue = new PriorityQueue<TNode>();
		aQueue.add(new TNode(source, 0));
		ArrayList<String> visitedC = new ArrayList<String>();
		String ret = null; // if there is no common ancestor
		long hops = -1;
		
		while (!aQueue.isEmpty()) {
			TNode ancestor = aQueue.remove();

			// This is in order to avoid the cycles
			if (visitedC.contains(ancestor.oClass))
				continue;
			else
				visitedC.add(ancestor.oClass);

			if (isAncestor(ancestor.oClass, target)) {
				
				long right = hierarchicDistance(ancestor.oClass, target);
				
				if ((hops < 0) 
						||(((right + ancestor.value) < hops) 
								&& ((right + ancestor.value) <= 2*ontologyTreeHeight))) {
					ret = ancestor.oClass;
					hops = (right + ancestor.value);
				}
				
			} else if ((hops < 0) || (ancestor.value + 1 < hops)){ // this heuristic needs to be improved 
				
				ArrayList<String> ancClasses =  SDBHelper.getOWLSuperClasses(ancestor.oClass, store);
				
				for (String anr : ancClasses)
					aQueue.add(new TNode(anr, ancestor.value + 1));
			}
		}

		return ret;
	}

	
	/**
	 * Finds the hierarchical distance from the child to the parent
	 * 
	 */
	private long hierarchicDistance(String parent, String child) {

		Stack<TNode> ancStack = new Stack<TNode>();
		TNode node = new TNode(child, 0);
		ancStack.add(node);
		ArrayList<String> visitedC = new ArrayList<String>();
		long hops = -1;

		while (!ancStack.isEmpty()) {
			TNode ancestor = ancStack.pop();

			if (ancestor.value > ontologyTreeHeight // Constraints to less than tree height 
					|| ((hops > 0) && (ancestor.value >= hops))) // We don't need any other values > hops 
				continue;	

			// This is in order to avoid the cycles
			if (visitedC.contains(ancestor.oClass))
				continue;
			else
				visitedC.add(ancestor.oClass);

			if (parent.equalsIgnoreCase(ancestor.oClass)) {
				if ((hops < 0) || (hops > ancestor.value))
					hops = ancestor.value;
			} 
			else if ((hops < 0) || ((ancestor.value + 1) < hops)){
				ArrayList<String> anc =  SDBHelper.getOWLSuperClasses(ancestor.oClass, store);
				for (String anr : anc)
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
			String ancestorClass, 
			String descendantClass)
	{
		Stack<String> ancStack = new Stack<String>();
		ancStack.add(descendantClass);
		ArrayList <String> visitedC = new ArrayList<String>();  
		
		while(!ancStack.isEmpty())
		{
			String ancestor = ancStack.pop();

			// This is in order to avoid the cycles
			if (visitedC.contains(ancestor))
				continue;
			else
				visitedC.add(ancestor);

			if (ancestorClass.equals(ancestor)) {
				return true;
			} else {
				ArrayList<String> anc =  SDBHelper.getOWLSuperClasses(ancestor, store);
				for (String anr : anc)
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
	 * e.g. Road_vehicles Automobiles
	 */
	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("Invalid arguments");
			System.exit(0);
		}

		ClassDivergenceSDB cd = new ClassDivergenceSDB();

		System.out.printf("\nThe class divergence between %s and %s : %6.4f\n",
				args[0], args[1], cd.findClassDivergence(args[0], args[1], false));
		System.out.printf("\nThe class divergence (symmetric) between %s and %s : %6.4f\n",
				args[0], args[1], cd.findClassDivergence(args[0], args[1], true));
		
		cd.closeConnections();
	}


}
