/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.semanticweb.owl.model.*;

import uf.morpheus.meta.Constants;
import uf.morpheus.meta.Utils;
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
	private SSQOntology sourceSSQ  = null;
	private SSQOntology targetSSQ  = null;
	private MatchingLevel level = MatchingLevel.LEVEL_00; 
	private StringDistanceAlgorithm stringMatchingAlgorithm = StringDistanceAlgorithm.EQUAL_DISTANCE;
	private MessageLogger msg = MessageLogger.getInstance();

	// Class properties...
	public StringDistanceAlgorithm getStringMatchingAlgorithm() {
		return stringMatchingAlgorithm;
	}

	public void setStringMatchingAlgorithm(StringDistanceAlgorithm sMatchingAlgorithm) {
		stringMatchingAlgorithm = sMatchingAlgorithm;
	}

	public void setTargetSSQ(SSQOntology targetSSQ) {
		this.targetSSQ = targetSSQ;
	}

	public SSQOntology getTargetSSQ() {
		return targetSSQ;
	}

	public void setSourceSSQ(SSQOntology sourceSSQ) {
		this.sourceSSQ = sourceSSQ;
	}

	public SSQOntology getSourceSSQ() {
		return sourceSSQ;
	}

	public MatchingLevel getLevel() {
		return level;
	}

	public void setLevel(MatchingLevel level) {
		this.level = level;
	}

	/**
	 * Initializes the source and target OWL SSQ Ontologies    
	 * 
	 * @param sourceOntology the source ontology's URI 
	 * @param targetOntology the target ontology's URI 
	 * */
	public void init(
			String sourceOntology, 
			String targetOntology) throws OWLOntologyCreationException 
	{
		//msg.logger.info("Loading the source and target ontologies.");
		this.setSourceSSQ(new SSQOntology(URI.create(sourceOntology)));
		this.setTargetSSQ(new SSQOntology(URI.create(targetOntology)));
		//msg.logger.fine("DONE!");
	}
	
	/**
	 * Initializes the source and target OWL SSQ Ontologies    
	 * 
	 * @param sourceOntology source ontology
	 * @param targetOntology target ontology 
	 * */
	public void init(
			SSQOntology sourceOntology, 
			SSQOntology targetOntology) throws OWLOntologyCreationException 
	{
		//msg.logger.info("Loading the source and target ontologies.");
		this.setSourceSSQ(sourceOntology);
		this.setTargetSSQ(targetOntology);
		//msg.logger.fine("DONE!");
	}
	
	/** 
	 * Function that gets the ontology path the class belongs to
	 * 
	 * 
	 */
	private String getRefOntologyURI(OWLDescription desc){
		String path = "";
		
		if (!desc.isAnonymous()){
			String [] arr = desc.asOWLClass().getURI().toString().split("#");
			path = arr[0];
		}
		
		return path;
	}
	
	/** 
	 * Function that finds out the class divergence between the realm 
	 * and contexts of the source and target ontology
	 * 
	 * @throws OWLOntologyCreationException 
	 * 
	 */
	public SSQDissimilarityMeasure findSSQClassDivergence() 
	throws OWLOntologyCreationException 
	{

		SSQDissimilarityMeasure measure = new SSQDissimilarityMeasure(MeasureMethod.CLASS_DIVERGENCE);		
		
		// The ontologies doesn't it makes any sense search further
		if (getRefOntologyURI(
				this.getSourceSSQ().getRealm()).equalsIgnoreCase(
						getRefOntologyURI(this.getTargetSSQ().getRealm()))) {
			ClassDivergence cd = new ClassDivergence(getRefOntologyURI(
					this.getSourceSSQ().getRealm()));

			// Calculates the SSQ realm class divergence
			measure.setRealmMeasure(cd.findOWLClassDivergence(
					this.getSourceSSQ().getRealm().toString(), 
					this.getTargetSSQ().getRealm().toString()));
		} else {
			msg.logger.log(Level.INFO, "Realms' ontologies do not match.");
			measure.setRealmMeasure(Constants.DISSIMILARITY);
		}

		
		msg.logger.log(Level.INFO, "Class divergence between the realms "
				+ this.getSourceSSQ().getRealm() + " and "
				+ this.getTargetSSQ().getRealm() + " = "
				+ measure.getRealmMeasure() + "\n");

		
		// Calculates the divergences for all the valid  
		// contexts defined in the Context Enum 
		for(SSQContexts c : SSQContexts.values())
		{
			if (c != SSQContexts.NONE){
				double m = Constants.DISSIMILARITY; // Total dissimilarity

				BaseContext c1 = this.getSourceSSQ().getBaseContext(c);
				BaseContext c2 = this.getTargetSSQ().getBaseContext(c);
				
				if (c1 != null && c2 != null)
				{
					Set<OWLDescription> sClasses = c1.getRanges();
					Set<OWLDescription> tClasses = c2.getRanges();
					
					if (sClasses.size() == 0 || tClasses.size() == 0)
						measure.addContext(false, Constants.DISSIMILARITY, c);
					else {		
						double [] mv = new double[sClasses.size()];
						int index = 0;
						double sum = 0.0;
						
						/*
						// This is a method
						for (OWLDescription sInd : sClasses){
							// Since it is a divergence measure we take the min divergent pair ?
							mv[index++] = 1.0;
							
							if (sInd == null) 
								continue;
							
							double v = 0.0;
							
							for (OWLDescription tInd : tClasses){
								if (tInd == null) 
									continue;
								
								
								if (getRefOntologyURI(sInd).equalsIgnoreCase(getRefOntologyURI(tInd))) {
									ClassDivergence cd = new ClassDivergence(getRefOntologyURI(sInd));

									// Calculates the SSQ context class divergence
									v += cd.findOWLClassDivergence(sInd.toString(), tInd.toString());
								} else {
									msg.logger.log(Level.INFO, "Contexts' ontologies do not match.");
									v += Constants.DISSIMILARITY;
								}

								// mv[index-1] = Math.min(mv[index-1], v);					
							}
							
							mv[index-1] = Math.min(mv[index-1], (v/tClasses.size()));
						}
						*/
						
						// Simplest method 
						for (OWLDescription sInd : sClasses){
							// Since it is a divergence measure we take the min divergent pair ?
							mv[index++] = 1.0;
							
							if (sInd == null) 
								continue;
							
							for (OWLDescription tInd : tClasses){
								if (tInd == null) 
									continue;
								double v = 0.0;
								
								if (getRefOntologyURI(sInd).equalsIgnoreCase(getRefOntologyURI(tInd))) {
									ClassDivergence cd = new ClassDivergence(getRefOntologyURI(sInd));

									// Calculates the SSQ context class divergence
									v += cd.findOWLClassDivergence(sInd.toString(), tInd.toString());
								} else {
									msg.logger.log(Level.INFO, "Contexts' ontologies do not match.");
									v += Constants.DISSIMILARITY;
								}

								mv[index-1] = Math.min(mv[index-1], v);					
							}

						}
						
						
						for (int i = 0; i < sClasses.size(); i++)
							sum += mv[i] * mv[i];
						
						m = (Math.sqrt(sum)/sClasses.size());
						measure.addContext(true, m, c);
						
						msg.logger.log(Level.INFO, "Class divergence between the classes in the context " + c + " = " + m + "\n");

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
	 * Finds the dissimilarity between the source classes (set)
	 * and target classes (set) based on the level of matching 
	 * 
	 * @param sClasses source classes set 
	 * @param tClasses target classes set 
	 * @param level matching level 
	 * */
	private double calcSetOfClassesDissimilarity(
			Set<OWLDescription> sClasses,
			Set<OWLDescription> tClasses,
			MatchingLevel level) 
	{
		// Basic check...other checks should be done at higher level 
		if (sClasses.size() == 0 && tClasses.size() == 0)
			return Constants.SIMILARITY; 
		else if (sClasses.size() == 0 || tClasses.size() == 0)
			return Constants.DISSIMILARITY; 
				
		
		double [] mv = new double[sClasses.size()];
		int index = 0;
		
		for (OWLDescription sInd : sClasses)
		{
			mv[index++] = 1.0;
			for (OWLDescription tInd : tClasses)
			{
				double v = this.calcClassDissimilarity(sInd, tInd, level);
				mv[index-1] = Math.min(mv[index-1], v);	// Since it is a distance					
			}
		}
		
		double sum = 0.0;
		for (int i = 0; i < sClasses.size(); i++)
			sum += mv[i] * mv[i];
		
		return (Math.sqrt(sum)/sClasses.size());
	}

	/**
	 * Finds the dissimilarity between the given source class
	 * and target class based on the level of matching 
	 * 
	 * @param sDesc source class desc 
	 * @param tDesc target class desc  
	 * @param level matching level 
	 * */
	private double calcClassDissimilarity(
			OWLDescription sDesc, 
			OWLDescription tDesc, 
			MatchingLevel level) 
	{
		double m = Constants.DISSIMILARITY; 
		
		// We ignore the anonymous classes 
		if (sDesc == null || tDesc == null)
			return Constants.DISSIMILARITY;
		else if (sDesc.isAnonymous() && tDesc.isAnonymous())
			return Constants.SIMILARITY; 
		else if (sDesc.isAnonymous() || tDesc.isAnonymous())
			return Constants.DISSIMILARITY; 
		
		double sim = Utils.findURISimilarity(
				sDesc.asOWLClass().getURI(), 
				tDesc.asOWLClass().getURI(), 
				this.getStringMatchingAlgorithm());
		
		// If both class has same URI returns 0.0 
		if (sim == Constants.SIMILARITY)
			return sim;
		
		// Level 0 matching 
		if (level == MatchingLevel.LEVEL_00)
			m = sim;
		// Level 1 matching
		else 
		{
			double wt = 1.0 / 4; // weights 
			
			Set <OWLDescription> sObj = this.sourceSSQ.getAncestorClasses(sDesc);
			Set <OWLDescription> tObj = this.targetSSQ.getAncestorClasses(tDesc);
			Set <OWLDescription> sData = this.sourceSSQ.getDescendantClasses(sDesc);
			Set <OWLDescription> tData = this.targetSSQ.getDescendantClasses(tDesc);
			
			Set <OWLIndividual> indS = this.sourceSSQ.getClassIndividuals(sDesc, true);
			Set <OWLIndividual> indT = this.targetSSQ.getClassIndividuals(tDesc, true);
			Set <OWLObjectProperty> opS = new HashSet<OWLObjectProperty>();
			Set <OWLObjectProperty> opT = new HashSet<OWLObjectProperty>();
			for (OWLIndividual i : indS)
				opS.addAll(this.sourceSSQ.getIndividualObjectProperties(i));
			for (OWLIndividual i : indT)
				opT.addAll(this.targetSSQ.getIndividualObjectProperties(i));
			
			
			double m1 = sim; // does the URI match
			double m2 = calcSetOfClassesDissimilarity(
					sObj,
					tObj,
					MatchingLevel.LEVEL_00);
			double m3 = calcSetOfClassesDissimilarity(
					sData,
					tData,
					MatchingLevel.LEVEL_00);
			
			double m4 = calcSetOfObjectPropertiesDissimilarity(
					opS, 
					opT, 
					MatchingLevel.LEVEL_00);
			
			m = m1 * wt + m2 * wt + m3 * wt + m4 * wt;
			
		}
		return m;

	}

	/**
	 * Finds the URI dissimilarity between the given source SSQ ontology object 
	 * properties (set) and target SSQ ontology object properties (set)  
	 * */
	private double calcSetOfObjectPropertiesDissimilarity(
			Set<OWLObjectProperty> sObjectProperties,
			Set<OWLObjectProperty> tObjectProperties,
			MatchingLevel level) 
	{
		// Validates the input 
		if (sObjectProperties.size() == 0 
				&& tObjectProperties.size() == 0)
			return Constants.SIMILARITY; 
		else if (sObjectProperties.size() == 0 
				|| tObjectProperties.size() == 0)
			return Constants.DISSIMILARITY; 
		
		double [] mv = new double[sObjectProperties.size()];
		int index = 0;
		double sum = 0.0;
		
		for (OWLObjectProperty sInd : sObjectProperties)
		{
			mv[index++] = 1.0;
			// Since it is a distance measure we take min distance pair 
			for (OWLObjectProperty tInd : tObjectProperties){
				double v = Utils.findURISimilarity(sInd.getURI(), tInd.getURI(), this.getStringMatchingAlgorithm());
				mv[index-1] = Math.min(mv[index-1], v);				
			}
		}
		
		for (int i = 0; i < sObjectProperties.size(); i++)
			sum += mv[i] * mv[i];
		
		return (Math.sqrt(sum)/sObjectProperties.size());
	}


	/** 
	 * Function that finds out the dissimilarity measure based 
	 * on the SSQ (ontology) components URI's dissimilarity  
	 * 
	 */
	public SSQDissimilarityMeasure findSSQURIDissimilarity() 
	{

		SSQDissimilarityMeasure measure = new SSQDissimilarityMeasure(MeasureMethod.URI_DISSIMILARITY);		
		
		// Calculates the SSQ realm class URI dissimilarity 
		measure.setRealmMeasure(this.calcClassDissimilarity(
				this.getSourceSSQ().getRealm(), 
				this.getTargetSSQ().getRealm(),
				this.getLevel()));
		
		msg.logger.log(Level.INFO, "Class URI dissimilarity between the realms "
				+ this.getSourceSSQ().getRealm() + " and "
				+ this.getTargetSSQ().getRealm() + " = "
				+ measure.getRealmMeasure() + "\n");
		

		// Calculates the URI dissimilarity for all the valid  
		// contexts defined in the Context Enum 
		for(SSQContexts c : SSQContexts.values())
		{
			if (c != SSQContexts.NONE){
				double m = Constants.DISSIMILARITY; // Total dissimilarity

				BaseContext c1 = this.getSourceSSQ().getBaseContext(c);
				BaseContext c2 = this.getTargetSSQ().getBaseContext(c);
				
				if (c1 != null && c2 != null){
					Set<OWLDescription> sClasses = c1.getRanges();
					Set<OWLDescription> tClasses = c2.getRanges();
					
					// Basic check...other checks should be done at higher level 
					if (sClasses.size() == 0 || tClasses.size() == 0)
						measure.addContext(false, Constants.DISSIMILARITY, c);
					else {		
						double [] mv = new double[sClasses.size()];
						int index = 0;
						
						for (OWLDescription sInd : sClasses){
							mv[index++] = 1.0;
							for (OWLDescription tInd : tClasses){
								double v = this.calcClassDissimilarity(sInd, tInd, MatchingLevel.LEVEL_01);
								mv[index-1] = Math.min(mv[index-1], v);	// Since it is a distance					
							}
						}
						
						double sum = 0.0;
						for (int i = 0; i < sClasses.size(); i++)
							sum += mv[i] * mv[i];
						
						m = (Math.sqrt(sum)/sClasses.size());
						
						msg.logger.log(Level.INFO, "Class URI dissimilarity between the classes in the context " + c + " = " + m + "\n");
						
						measure.addContext(true, m, c);
					}
				}
				else {
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
	 * @param arg2 [1] use class divergence measure; [2] use structural measure;   
	 * @param arg3 string distance algorithm to be used (optional)
	 * 
	 * E.g. SSQ-0
	 */
	public static void main(String[] args)
	{
		if (args.length == 0){
			System.out.print("Invalid arguments");
			System.exit(0);
		}
		
		String output = "";
		output = "{'array':[SSQ-476, SSQ-473], 'id':" + args[0] + "}";
		
		System.out.println(output);
		
		
		/*
		
		SSQRelevanceMatcher matcher = new SSQRelevanceMatcher();

		try {
			// Initializes the matcher class
			matcher.init(args[1], args[2]);
			
			if (args[0].equalsIgnoreCase("1")){
				
				// Calculates the SSQ ontology components URI dissimilarity 
				
				matcher.setLevel(MatchingLevel.LEVEL_01); // matching levels...
				
				if (args.length > 3){
					switch(Integer.parseInt(args[3])){
					case 1:
						matcher.setStringMatchingAlgorithm(StringDistanceAlgorithm.LEVENSHTEIN_DISTANCE);
					case 2:
						matcher.setStringMatchingAlgorithm(StringDistanceAlgorithm.EQUAL_DISTANCE);
					case 3:
						matcher.setStringMatchingAlgorithm(StringDistanceAlgorithm.HAMMINGDISTANCE);
					case 4:
						matcher.setStringMatchingAlgorithm(StringDistanceAlgorithm.JARO_MEASURE);
					case 5:
						matcher.setStringMatchingAlgorithm(StringDistanceAlgorithm.JARO_WINKLER_MEASURE);
					case 6:
						matcher.setStringMatchingAlgorithm(StringDistanceAlgorithm.NGRAM_DISTANCE);
					case 7:
						matcher.setStringMatchingAlgorithm(StringDistanceAlgorithm.SMOA_DISTANCE);
					case 8:
						matcher.setStringMatchingAlgorithm(StringDistanceAlgorithm.SUBSTRING_DISTANCE);
					}
				}
				else 
					matcher.setStringMatchingAlgorithm(StringDistanceAlgorithm.EQUAL_DISTANCE);
				
				SSQDissimilarityMeasure mu = matcher.findSSQURIDissimilarity();
	
				System.out.println(mu.toString());
				System.out.println("The aggregated SSQ dissimilarity value: " + matcher.findSSQAggregateMeasure(mu));
			}
			else {
			
				// Calculates the SSQ components class divergence  
				
				SSQDissimilarityMeasure md = matcher.findSSQClassDivergence();
	
				System.out.println(md.toString());
				System.out.println("The aggregated SSQ divergence value: " + matcher.findSSQAggregateMeasure(md));
			}

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		*/
	}
}
