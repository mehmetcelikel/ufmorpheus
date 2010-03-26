/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owl.model.*;

import com.hp.hpl.jena.sdb.Store;

import uf.morpheus.db.SDBHelper;
import uf.morpheus.meta.Constants;
import uf.morpheus.meta.Constants.MatchingLevel;
import uf.morpheus.meta.Constants.MeasureMethod;
import uf.morpheus.meta.Constants.SSQContexts;
import uf.morpheus.meta.Constants.StringDistanceAlgorithm;
import uf.morpheus.meta.MessageLogger;

/**
 * This class implements the helper functions to calculate 
 * the SSQ's similarity measure 
 * 
 * @author Clint P. George
 *
 */
public class SSQRelevanceMatcher
{
	// Class variables...
	private SSQOntologySDB sourceSSQ  = null;
	private SSQOntologySDB targetSSQ  = null;
	private MatchingLevel level = MatchingLevel.LEVEL_00; 
	private StringDistanceAlgorithm stringMatchingAlgorithm = StringDistanceAlgorithm.EQUAL_DISTANCE;
	private MessageLogger msg = MessageLogger.getInstance();
	private Store store = null;

	// Class properties...
	public StringDistanceAlgorithm getStringMatchingAlgorithm() {
		return stringMatchingAlgorithm;
	}

	public void setStringMatchingAlgorithm(StringDistanceAlgorithm sMatchingAlgorithm) {
		stringMatchingAlgorithm = sMatchingAlgorithm;
	}

	public void setTargetSSQ(SSQOntologySDB targetSSQ) {
		this.targetSSQ = targetSSQ;
	}

	public SSQOntologySDB getTargetSSQ() {
		return targetSSQ;
	}

	public void setSourceSSQ(SSQOntologySDB sourceSSQ) {
		this.sourceSSQ = sourceSSQ;
	}

	public SSQOntologySDB getSourceSSQ() {
		return sourceSSQ;
	}

	public MatchingLevel getLevel() {
		return level;
	}

	public void setLevel(MatchingLevel level) {
		this.level = level;
	}

	
	public SSQRelevanceMatcher(){
		store = SDBHelper.getStore();
	}
	
	public SSQRelevanceMatcher(Store store){
		this.store = store;
	}
	
	
	/**
	 * Initializes the source and target SSQ Ontologies    
	 * 
	 * @param sourceSSQ the source ontology's id  
	 * @param targetSSQ the target ontology's id  
	 * */
	public boolean loadSSQs(String sourceSSQ, String targetSSQ) 
	{
		
		SSQOntologySDB sSSQ = new SSQOntologySDB(sourceSSQ, store);
		SSQOntologySDB tSSQ = new SSQOntologySDB(targetSSQ, store);
		
		if (!sSSQ.loadSSQDetails() || ! tSSQ.loadSSQDetails()) // If the SSQs loading fail 
			return false;
		else{
			this.sourceSSQ = sSSQ;  // Assigns the value 
			this.targetSSQ = tSSQ;
		}

		return true; 
	}

	/** 
	 * Function that finds out the class divergence between the realm 
	 * and contexts of the source and target ontology
	 * 
	 * @throws OWLOntologyCreationException 
	 * 
	 */
	public SSQDissimilarityMeasure findSSQClassDivergence()
	{

		SSQDissimilarityMeasure measure = new SSQDissimilarityMeasure(MeasureMethod.CLASS_DIVERGENCE);		
		

		ClassDivergenceSDB cd = new ClassDivergenceSDB(store);

		// Calculates the SSQ realm class divergence
		measure.setRealmMeasure(cd.findOWLClassDivergence(
				this.getSourceSSQ().getRealm(), 
				this.getTargetSSQ().getRealm()));


		
//		msg.logger.log(Level.INFO, "Class divergence between the realms "
//				+ this.getSourceSSQ().getRealm() + " and "
//				+ this.getTargetSSQ().getRealm() + " = "
//				+ measure.getRealmMeasure() + "\n");

		
		// Calculates the divergences for all the valid  
		// contexts defined in the Context Enum 
		for(SSQContexts c : SSQContexts.values())
		{
			if (c != SSQContexts.NONE){
				double m = Constants.DISSIMILARITY; // Total dissimilarity

				BaseContextSDB c1 = this.getSourceSSQ().getBaseContext(c);
				BaseContextSDB c2 = this.getTargetSSQ().getBaseContext(c);
				
				if (c1 != null && c2 != null)
				{
					Set<String> sClasses = c1.getRanges();
					Set<String> tClasses = c2.getRanges();
					
					if (sClasses.size() == 0 || tClasses.size() == 0)
						measure.addContext(false, Constants.DISSIMILARITY, c);
					else {		
						double [] mv = new double[sClasses.size()];
						int index = 0;
						double sum = 0.0;
						
						// Simplest method 
						for (String sInd : sClasses){
							// Since it is a divergence measure we take the min divergent pair ?
							mv[index++] = 1.0;
							
							if (sInd == null) 
								continue;
							
							for (String tInd : tClasses){
								if (tInd == null) 
									continue;
								double v = 0.0;

								// Calculates the SSQ context class divergence
								v += cd.findOWLClassDivergence(sInd, tInd);
								mv[index-1] = Math.min(mv[index-1], v);					
							}

						}
						
						
						for (int i = 0; i < sClasses.size(); i++)
							sum += mv[i] * mv[i];
						
						m = (Math.sqrt(sum)/sClasses.size());
						measure.addContext(true, m, c);
//						
//						msg.logger.info("Class divergence between the classes in the context " + c + " = " + m + "\n");

					}
				}
				else 
				{
					measure.addContext(false, Constants.DISSIMILARITY, c);
				}
			}
		}
		
		return measure;
	}

	/**
	 * It is function to aggregate the measure 
	 * divergence / dissimilarity values 
	 * 
	 * @param measure the divergence measure in SSQDissimilarityMeasure format 
	 * @return aggregated value 
	 */
	public double findSSQAggregateMeasure(SSQDissimilarityMeasure measure){
		
		
		// Calculates the total number of applicable contexts 
		int count = 0;
		for(SSQContexts c : SSQContexts.values())
			if (c != SSQContexts.NONE)
				if (measure.getContextMeasure(c) != null 
						&& measure.getContextMeasure(c).isApplicable())
					count++;
		
		// Calculates an equal proportion aggregate 
		double agg = 0.0;
		double weights = 1.0 / (count + 1);
		
		agg += weights * measure.getRealmMeasure();
		for(SSQContexts c : SSQContexts.values())
			if (c != SSQContexts.NONE)
				if (measure.getContextMeasure(c) != null 
						&& measure.getContextMeasure(c).isApplicable()){
					agg += weights * measure.getContextMeasure(c).getValue();
				}

		return agg;		
	}
	
	
	/** 
	 * Function main() 
	 * 
	 * @param arg1 SSQ id that exists in the data base  
	 * 
	 * E.g. SSQ-0
	 */
	public static void main(String[] args)
	{
		if (args.length == 0){
			System.out.print("Invalid arguments");
			System.exit(0);
		}
		
		//String output = "";
		//output = "{'array':['SSQ-476', 'SSQ-473'], 'id':'" + args[0] + "'}";
		
		//System.out.println(output);

		// Gets the SDB store connection 
		Store store = SDBHelper.getStore();
		
		// To all the SSQs in the data base 
		ArrayList<String> ssqClasses = SDBHelper.getReferencedClasses("http://zion.cise.ufl.edu/ontology/classes/OWLClasses.xml#SSQ", store); 

		SSQRelevanceMatcher matcher = new SSQRelevanceMatcher(store);
		
		String newSSQ = "http://zion.cise.ufl.edu/ontology/ssq/#" + args[0];
		
		for (String ssqId:ssqClasses){
			if (!matcher.loadSSQs(newSSQ, ssqId)) {
				System.out.println("Please enter a valid SSQ id");
			} else {
				// Calculates the SSQ components class divergence
				SSQDissimilarityMeasure md = matcher.findSSQClassDivergence();
	
				System.out.println(md.toString());
				System.out.println("The aggregated SSQ divergence value: "
						+ matcher.findSSQAggregateMeasure(md));
			}
		}
		
		// Closes the SDB store connection 
		SDBHelper.closeStore(); 
		
	}
}
