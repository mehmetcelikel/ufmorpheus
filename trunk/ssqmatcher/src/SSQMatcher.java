

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



import com.hp.hpl.jena.sdb.Store;


public class SSQMatcher {

	private ClassDivergenceStore cdStore = null;
	public static String ONTOLOGY_NS = ""; 
	public static String ONTOLOGY_CLASSES_NS = "";
	public static String ONTOLOGY_DATAPROPERTY_NS = "";
	public static String ONTOLOGY_PROPERTY_TREE_HEIGHT = "";
	private static String QUALIFIED_SSQIDS = "";
	public static double INPUT_TERMS_WEIGHT = 0.5;
	public static double OUTPUT_TERMS_WEIGHT = 0.5;
	
	public ArrayList<TermMeasureSet> termSI = new ArrayList<TermMeasureSet>();
	public ArrayList<TermMeasureSet> termSO = new ArrayList<TermMeasureSet>();
	

	public class QCTermMeasure {
		public String QualifiedCategory = "";
		public ArrayList<SSQMatcher.TermMeasure> MatchingTermsCD = new ArrayList<SSQMatcher.TermMeasure>();
		
		public QCTermMeasure(String qualifiedCategory) {
			super();
			QualifiedCategory = qualifiedCategory;
		}
	}
	
	public class TermMeasureSet{
		public ArrayList<TermMeasure> TermMeasures = new ArrayList<TermMeasure>();
		public int SetID = 0;
		public TermMeasureSet(ArrayList<TermMeasure> termMeasures, int setID) {
			super();
			TermMeasures = termMeasures;
			Collections.sort(TermMeasures);
			SetID = setID;
		}
	}
	
	
	public class TermMeasure implements  Comparable<TermMeasure>{
		public String CandidateTerm = "";
		public String CandidateCategory = "";
		public double CandidateCategoryProb = 0.0;
		
		public String QualifiedTerm = "";
		public String QualifiedCategory = "";
		
		public double CategoryDivergence = 0.0;
		public boolean selected = false; 
		public Constants.TermClassType TermType; 
		public int Number = 0;

		public TermMeasure(String candidateTerm, String candidateCategory,
				double candidateCategoryProb, String qualifiedTerm,
				String qualifiedCategory, double categoryDivergence, 
				Constants.TermClassType termClassType, int sn) {
			super();

			CandidateTerm = candidateTerm;
			CandidateCategory = candidateCategory;
			CandidateCategoryProb = candidateCategoryProb;
			QualifiedTerm = qualifiedTerm;
			QualifiedCategory = qualifiedCategory;
			CategoryDivergence = categoryDivergence;
			
			TermType = termClassType;
			Number = sn; 
			
		}

		
		@Override
		public int compareTo(TermMeasure o) {
			return (this.CategoryDivergence == o.CategoryDivergence) ? 0
					: ((this.CategoryDivergence < o.CategoryDivergence) ? -1 : 1);  
		}
		
		public String toString(){			
			StringBuilder sb = new StringBuilder();
			
			sb.append("Candidate - ");
			sb.append(CandidateTerm + " : " + CandidateCategory 
					+ "[" + CandidateCategoryProb + "]\n");
			
			sb.append("Qualified - ");
			sb.append(QualifiedTerm + " : " + QualifiedCategory + "\n");
			sb.append("Category Divergence : " + CategoryDivergence + "\n");
			
			return sb.toString();			
		}
	}

	
	public class SSQSimilarityMeasure implements  Comparable<SSQSimilarityMeasure> {
		public double SSQDivergence = 0.0;
		public int QueryID = 0;
		public ArrayList<SSQMatcher.TermMeasure> TermsClassDivergence = null;
		
		public SSQSimilarityMeasure(
				double ssqDivergence, 
				ArrayList<SSQMatcher.TermMeasure> termCDs, 
				int queryId) {
			super();
			SSQDivergence = ssqDivergence;
			QueryID = queryId;
			TermsClassDivergence = termCDs;
		}

		@Override
		public int compareTo(SSQSimilarityMeasure o) {
			return (this.SSQDivergence == o.SSQDivergence) ? 0
					: ((this.SSQDivergence > o.SSQDivergence) ? -1 : 1);  
		}
		
		public String toString(){			
			StringBuilder sb = new StringBuilder();
			sb.append("Query ID: ");
			sb.append(QueryID);
			sb.append(", SSQ Divergence: ");
			sb.append(SSQDivergence + "\n");
			return sb.toString();			
		}
	}
	
	
	public SSQMatcher(Store store){
		this.cdStore = new ClassDivergenceStore(
				store, 
				ONTOLOGY_CLASSES_NS, 
				ONTOLOGY_DATAPROPERTY_NS, 
				ONTOLOGY_PROPERTY_TREE_HEIGHT);		
	}
	
	/**
	 * This is a crude way of finding similarity between  
	 * the candidate SSQ and qualified SSQ 
	 * 
	 * @param qualified A qualified SSQ from the QRM data store 
	 * @param candidate the candidate SSQ 
	 * @return
	 */
	
	public double findSSQSimilarity(
			SSQClass qualified, 
			SSQClass candidate){
		
		
		double N = candidate.getInputs().size();
		double m = Constants.SIMILARITY;
		
		if (N == 0)
			return Constants.DISSIMILARITY;
		
		
		// Assumes that the order of the SSQ terms is not important 
		for (Term tc : candidate.getInputs()){
			
			double min = 1.0; 
			Category candidateCategory = tc.getMostProbableCategory();
			
			if (candidateCategory.category.equalsIgnoreCase("UNKNOWN")){
				m += 1/N;
				continue;
			}
			
			for (Term tq : qualified.getInputs()){
				
				// TODO: For the time being we only consider  
				// the most probable category of a candidate term;   
				
				
				Category qualifiedCategory = tq.getMostProbableCategory();
				
				double cdM = cdStore.getClassDivergence(
						qualifiedCategory.category, 
						candidateCategory.category);
				
				double divergence = candidateCategory.probability * cdM;
				
				min = Math.min(cdM, min); 

				System.out.println("Q-SSQ:" + tq.term + " [" + qualifiedCategory 
						+ "]:  ~ C-SSQ: " + tc.term + " [" + candidateCategory + "] c.d. - " + cdM 
						+ ", effective c.d. - " + divergence + ", min match - " + min);
			}
			
			m += min/N;
		}
		
		return m;
	}
	
	/**
	 * This is a crude way of finding similarity between  
	 * the candidate SSQ and qualified SSQ 
	 * @param qualified A qualified SSQ from the QRM data store 
	 * @param candidate candidate SSQ 
	 * @return
	 */
	
	public ArrayList<TermMeasure> calcSSQTermsSimilarity(
			SSQClass qualified, 
			SSQClass candidate){
		
	
		ArrayList<TermMeasure> termsCD = new ArrayList<TermMeasure>();
		termSI.clear();
		termSO.clear();
		
		if (candidate.getInputs().size() == 0)
			return termsCD;
		
		// Process the input terms and classes 
		// Assumes that the order of the SSQ terms is not important 
		int count = 1;
		int setId = 1;
		for (Term tc : candidate.getInputs()){

			Category cc = tc.getMostProbableCategory();

			ArrayList<TermMeasure> tms = new ArrayList<TermMeasure>();
			count = 1;
			if (cc.category.equalsIgnoreCase("UNKNOWN")){
				/*
				 * 
				 * * Skip the candidate term that is not assigned to a class  
				termsCD.add(new TermMeasure(
						tc.term, 
						cc.category, 
						1.0, "UNKNOWN", "UNKNOWN", 
						Constants.DISSIMILARITY,
						Constants.TermClassType.INPUT,
						count));
				tms.add(new TermMeasure(
						tc.term, 
						cc.category, 
						1.0, "UNKNOWN", "UNKNOWN", 
						Constants.DISSIMILARITY,
						Constants.TermClassType.INPUT,
						count));
				*/
				continue;
			}
			
			for (Term tq : qualified.getInputs()){
				// For the time being we only consider  
				// the most probable category of a candidate term;   
				Category qc = tq.getMostProbableCategory();
				
				// multiplies w/ confidence value 
				double cdM = cdStore.getClassDivergence(qc.category, cc.category) * cc.probability;
				
				termsCD.add(new TermMeasure(
						tc.term, 
						cc.category, 
						cc.probability, 
						tq.term, 
						qc.category, 
						cdM,
						Constants.TermClassType.INPUT, 
						count));
				tms.add(new TermMeasure(
						tc.term, 
						cc.category, 
						cc.probability, 
						tq.term, 
						qc.category, 
						cdM,
						Constants.TermClassType.INPUT, 
						count));
				count++;
			}

			
			termSI.add(new TermMeasureSet(tms, setId));
			
			setId++;
		}
		
		
		// Process outputs  
		setId = 1;
		for (Term tc : candidate.getOutputs()){

			Category cc = tc.getMostProbableCategory();
			count = 1;
			ArrayList<TermMeasure> tms = new ArrayList<TermMeasure>();
			
			if (cc.category.equalsIgnoreCase("UNKNOWN")){
				
				/*
				 * 
				 * * Skip the candidate term that is not assigned to a class 
				
				termsCD.add(new TermMeasure(
						tc.term, 
						cc.category, 
						1.0, "UNKNOWN", "UNKNOWN", 
						Constants.DISSIMILARITY,
						Constants.TermClassType.OUTPUT,
						count));
				
				tms.add(new TermMeasure(
						tc.term, 
						cc.category, 
						1.0, "UNKNOWN", "UNKNOWN", 
						Constants.DISSIMILARITY,
						Constants.TermClassType.OUTPUT,
						count));
				*/
				continue;
			}
			
			for (Term tq : qualified.getOutputs()){
				// For the time being we only consider  
				// the most probable category of a candidate term;   
				Category qc = tq.getMostProbableCategory();
				
				// multiplies w/ confidence value 
				double cdM = cdStore.getClassDivergence(qc.category, cc.category) * cc.probability;
				
				termsCD.add(new TermMeasure(
						tc.term, 
						cc.category, 
						cc.probability, 
						tq.term, 
						qc.category, 
						cdM,
						Constants.TermClassType.OUTPUT,
						count));
				tms.add(new TermMeasure(
						tc.term, 
						cc.category, 
						cc.probability, 
						tq.term, 
						qc.category, 
						cdM,
						Constants.TermClassType.OUTPUT,
						count));
				count++;
			}

			termSO.add(new TermMeasureSet(tms, setId));
			
			setId++;
		}
		
		Collections.sort(termsCD);
		
		return termsCD;
	}
	
	
	/**
	 * This is a crude way of finding similarity between  
	 * the candidate SSQ and qualified SSQ (2nd approach)
	 * @param qualified A qualified SSQ from the QRM data store 
	 * @param candidate candidate SSQ 
	 * @return
	 */
	
	public ArrayList<TermMeasure> calcSSQTermsSimilarity2(
			SSQClass qualified, 
			SSQClass candidate){
		
	
		ArrayList<TermMeasure> termsCD = new ArrayList<TermMeasure>();
		termSI.clear();
		termSO.clear();
		
		if (candidate.getInputs().size() == 0)
			return termsCD;
		
		// Process the input terms and classes 
		// Assumes that the order of the SSQ terms is not important 
		int count = 1;
		int setId = 1;
		for (Term tq : qualified.getInputs()){

			Category qc = tq.getMostProbableCategory();

			ArrayList<TermMeasure> tms = new ArrayList<TermMeasure>();
			count = 1;

			
			for (Term tc : candidate.getInputs()){
				// For the time being we only consider  
				// the most probable category of a candidate term;   
				Category cc = tc.getMostProbableCategory();
				
				
				if (qc.category.equalsIgnoreCase("UNKNOWN")){
					/*
					 * 
					 * * Skip the candidate term that is not assigned to a class  
					termsCD.add(new TermMeasure(
							tc.term, 
							cc.category, 
							1.0, "UNKNOWN", "UNKNOWN", 
							Constants.DISSIMILARITY,
							Constants.TermClassType.INPUT,
							count));
					tms.add(new TermMeasure(
							tc.term, 
							cc.category, 
							1.0, "UNKNOWN", "UNKNOWN", 
							Constants.DISSIMILARITY,
							Constants.TermClassType.INPUT,
							count));
					*/
					continue;
				}
				
				
				// multiplies w/ confidence value 
				double cdM = cdStore.getClassDivergence(qc.category, cc.category) * cc.probability;
				
				termsCD.add(new TermMeasure(
						tc.term, 
						cc.category, 
						cc.probability, 
						tq.term, 
						qc.category, 
						cdM,
						Constants.TermClassType.INPUT, 
						count));
				tms.add(new TermMeasure(
						tc.term, 
						cc.category, 
						cc.probability, 
						tq.term, 
						qc.category, 
						cdM,
						Constants.TermClassType.INPUT, 
						count));
				count++;
			}

			
			termSI.add(new TermMeasureSet(tms, setId));
			
			setId++;
		}
		
		
		// Process outputs  
		setId = 1;
		for (Term tq : qualified.getOutputs()){
			// For the time being we only consider  
			// the most probable category of a candidate term;   
			Category qc = tq.getMostProbableCategory();

			count = 1;
			ArrayList<TermMeasure> tms = new ArrayList<TermMeasure>();

			for (Term tc : candidate.getOutputs()){

				Category cc = tc.getMostProbableCategory();				
				
				if (cc.category.equalsIgnoreCase("UNKNOWN")){
					
					/*
					 * 
					 * * Skip the candidate term that is not assigned to a class 
					
					termsCD.add(new TermMeasure(
							tc.term, 
							cc.category, 
							1.0, "UNKNOWN", "UNKNOWN", 
							Constants.DISSIMILARITY,
							Constants.TermClassType.OUTPUT,
							count));
					
					tms.add(new TermMeasure(
							tc.term, 
							cc.category, 
							1.0, "UNKNOWN", "UNKNOWN", 
							Constants.DISSIMILARITY,
							Constants.TermClassType.OUTPUT,
							count));
					*/
					continue;
				}
				
				
				
				
				// multiplies w/ confidence value 
				double cdM = cdStore.getClassDivergence(qc.category, cc.category) * cc.probability;
				
				termsCD.add(new TermMeasure(
						tc.term, 
						cc.category, 
						cc.probability, 
						tq.term, 
						qc.category, 
						cdM,
						Constants.TermClassType.OUTPUT,
						count));
				tms.add(new TermMeasure(
						tc.term, 
						cc.category, 
						cc.probability, 
						tq.term, 
						qc.category, 
						cdM,
						Constants.TermClassType.OUTPUT,
						count));
				count++;
			}

			termSO.add(new TermMeasureSet(tms, setId));
			
			setId++;
		}
		
		Collections.sort(termsCD);
		
		return termsCD;
	}
	
	
	
	
	/**
	 * Creates query IDs and its similarity values in a JSON format 
	 * to send the output 
	 * 
	 * @param qrmSim
	 * @return
	 */
	
	
	private String getQueryIdString(ArrayList<SSQMatcher.SSQSimilarityMeasure> qrmSim) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(" \"queryids\": [");
		
		for (SSQMatcher.SSQSimilarityMeasure qm : qrmSim){
			
			ArrayList<QCTermMeasure> qctms = new ArrayList<QCTermMeasure>();
			
			for (SSQMatcher.TermMeasure tm : qm.TermsClassDivergence){

				if(tm.QualifiedCategory.equalsIgnoreCase(""))
					continue; 
				
				// If found add the term measure 
				boolean found = false;  
				for (QCTermMeasure qctm : qctms){
					if (qctm.QualifiedCategory.equalsIgnoreCase(tm.QualifiedCategory)){
						qctm.MatchingTermsCD.add(tm);
						found = true;
					}
				}
				
				// else add a new qc measure 
				if (!found){
					QCTermMeasure qctm = new SSQMatcher.QCTermMeasure(tm.QualifiedCategory);
					qctm.MatchingTermsCD.add(tm);
					qctms.add(qctm);
				}
			}
			
			sb.append("[" + qm.QueryID + ", " + qm.SSQDivergence + ", {");
			
			for (QCTermMeasure qctm : qctms){
				Collections.sort(qctm.MatchingTermsCD);
				
				sb.append("\"" + qctm.QualifiedCategory + "\": [");

				for (SSQMatcher.TermMeasure itm : qctm.MatchingTermsCD){
					sb.append("[\"" + itm.CandidateCategory);
					sb.append("\", \"" + itm.CandidateTerm);
					sb.append("\", " + itm.CategoryDivergence + "], ");
				}
				if(qctm.MatchingTermsCD.size() > 0)
					sb.deleteCharAt(sb.lastIndexOf(", "));

				sb.append("], ");
			}
			if(qctms.size() > 0)
				sb.deleteCharAt(sb.lastIndexOf(", "));
			
			sb.append("}], ");
		}
		
		
		if(qrmSim.size() > 0)
			sb.deleteCharAt(sb.lastIndexOf(", "));
		
		
		sb.append("], ");
		
		return sb.toString();
	}

	/**
	 * This function calculates similarity values with all 
	 * the qualified SSQs in the data base for a given 
	 * candidate SSQ
	 * 
	 */
	
	public ArrayList<SSQMatcher.SSQSimilarityMeasure> findSSQDivergences(
			String[] qSSQIds, 
			SSQClass candidate,
			boolean discardUnKownTerms){
		
		ArrayList<SSQMatcher.SSQSimilarityMeasure> ssqSimilarities = new ArrayList<SSQMatcher.SSQSimilarityMeasure>();
		long start = 0; 
		
		for (String qualifiedSSQId : qSSQIds){
		

			Utils.log("\n------------------------------------------------------------------------------------\n");
	
			start = (new Date()).getTime();
			
			// Loads the qualified SSQ as a SSQClass object from db 
			SSQClass qualified = null;
			try {
				
				Utils.log("Building qualified SSQ - " + qualifiedSSQId + "...");
				qualified = new SSQClass(Integer.parseInt(qualifiedSSQId));
				Utils.log(qualified.toString());
				
			} catch (SQLException e) {
				Utils.log("Exception in loading the qualified SSQ [" 
						+ qualifiedSSQId + "] : " + e.getMessage());
				continue; 
			}

			double N = qualified.getInputs().size();
			double ssqInputsDSim = 0.0;
			double ssqOutputsDSim = 0.0;
			double knownInputTerms = 0;
			double knownOutputTerms = 0;
	
			if (N <= 0){
				Utils.log("The SSQ " + qualified.getSsqId() + "is skipped", true);
				continue;
			}
			
			ArrayList<SSQMatcher.TermMeasure> termsDiv 
				= this.calcSSQTermsSimilarity2(qualified, candidate);
			
			/*
			
			Utils.log("Selected terms for SSQ relevance:\n");
			for (Term tq : qualified.getInputs()){
				
				if (tq.getCategories().size() == 0)
					continue;
				
				String cc = tq.getMostProbableCategory().category;
				
				for (SSQMatcher.TermMeasure tm : termsDiv){				
					if (!tm.selected && tm.QualifiedCategory.equalsIgnoreCase(cc)){
						Utils.log(tm.toString());
						ssqSim += (tm.CategoryDivergence / N);
						tm.selected = true; 						
						break; 
					}
				}
			}
			
			Utils.log("\nSSQ relevance using selected terms : " + (1.0 - ssqSim) + "\n");

			*/
			boolean flag = false; 
			Set<String> candidateTerms = new HashSet<String>();
			Set<String> qualifiedTerms = new HashSet<String>();
			for (TermMeasureSet tms : termSI){
				flag = false;
				for (TermMeasure tm : tms.TermMeasures){
					if (!(candidateTerms.contains(tm.CandidateCategory) 
							|| qualifiedTerms.contains(tm.QualifiedCategory))){
						
						Utils.log("Input set id: " 
								+ tms.SetID + ", candidate class : " 
								+ tm.CandidateCategory + ", qualified class : " 
								+ tm.QualifiedCategory + ", cd : " 
								+ tm.CategoryDivergence);
						candidateTerms.add(tm.CandidateCategory);
						qualifiedTerms.add(tm.QualifiedCategory);
						ssqInputsDSim += tm.CategoryDivergence;
						flag = true;
						break;
					}
				}
				
				// Adds dissimilarity 
				if (!flag)
					ssqInputsDSim += 1.0;
				knownInputTerms++;
				
				Utils.log("\n");
			}
			
			candidateTerms.clear();
			qualifiedTerms.clear();

			for (TermMeasureSet tms : termSO){
				flag = false;
				for (TermMeasure tm : tms.TermMeasures){
					if (!(candidateTerms.contains(tm.CandidateCategory) 
							|| qualifiedTerms.contains(tm.QualifiedCategory))){
						
						Utils.log("Output set id: " 
								+ tms.SetID + ", candidate class : " 
								+ tm.CandidateCategory + ", qualified class : " 
								+ tm.QualifiedCategory + ", cd : " 
								+ tm.CategoryDivergence);
						candidateTerms.add(tm.CandidateCategory);
						qualifiedTerms.add(tm.QualifiedCategory);
						ssqOutputsDSim += tm.CategoryDivergence;
						flag = true;
						break;
					}
				}
				
				// Adds dissimilarity 
				// e.g. when a qualified SSQ has two output classes 
				// and the candidate SSQ has only one output class
				if (!flag)
					ssqOutputsDSim += 1.0;
				knownOutputTerms++;	
				Utils.log("\n");
			}
			
			double ssqDSimMetricNew = ((knownInputTerms > 0) ? (ssqInputsDSim / (double)knownInputTerms) : 1.0) * INPUT_TERMS_WEIGHT
			+ ((knownOutputTerms > 0) ? (ssqOutputsDSim / (double)knownOutputTerms) : 1.0) * OUTPUT_TERMS_WEIGHT;
			
			Utils.log("\nTotal terms category divergence using new method : " + ssqDSimMetricNew  + "\n");
			
			ssqInputsDSim = 0.0;
			ssqOutputsDSim = 0.0;
			knownInputTerms = 0;
			knownOutputTerms = 0;
			
			for (SSQMatcher.TermMeasure tm : termsDiv){
				Utils.log(tm.toString());
				
				// Code to discard the unknown n-grams in the corpus 
				if (discardUnKownTerms 
						&& tm.CandidateCategory.equalsIgnoreCase("UNKNOWN")){
					Utils.log("Skipped the term " + tm.CandidateTerm + "\n");
					continue; 
				}
				
				// Considers both input terms and output terms 
				if (tm.TermType == Constants.TermClassType.INPUT){
					ssqInputsDSim += tm.CategoryDivergence;
					knownInputTerms++;
				}
				else {
					ssqOutputsDSim += tm.CategoryDivergence;
					knownOutputTerms++;					
				}
			}
			
			
			double ssqDSimMetric = ((knownInputTerms > 0) ? (ssqInputsDSim / (double)knownInputTerms) : 1.0) * INPUT_TERMS_WEIGHT
					+ ((knownOutputTerms > 0) ? (ssqOutputsDSim / (double)knownOutputTerms) : 1.0) * OUTPUT_TERMS_WEIGHT;
		
			ssqSimilarities.add(
					new SSQSimilarityMeasure(
							ssqDSimMetric, 
							termsDiv, 
							Integer.parseInt(qualifiedSSQId)));
			
			Utils.log("\nTotal terms category divergence : " + ssqDSimMetric  + "\n");
			
			
			Utils.log("Execution time : " + ((new Date()).getTime() - start) + "ms\n");
			Utils.log("------------------------------------------------------------------------------------");

		}

		Collections.sort(ssqSimilarities);
		
		return ssqSimilarities;
	}

	private static SSQClass buildCandidateQuery(String candidateQInfo) {
		
		/*
		{
		 "QU": "What is the the tire size for a 1997 Toyota Camry",
		 "AF": "the tire size",
		 "DI": "for a 1997 Toyota Camry",
		 "VG": "",
		 "NG": ["for", "for a", "for a 1997", "a", "a 1997", "a 1997 Toyota", "1997", "1997 Toyota", "1997 Toyota Camry", "Toyota", "Toyota Camry", "Camry", "the", "the tire", "the tire size", "tire", "tire size", "size"],
		 "WH": "what"
		}
		*/
		
		JSONParser parser = new JSONParser();
		ContainerFactory containerFactory = new ContainerFactory() {
			public List creatArrayContainer() {
				return new LinkedList();
			}

			public Map createObjectContainer() {
				return new LinkedHashMap();
			}

		};

		SSQClass candidate = new SSQClass();
		
		try {
			Map json = (Map) parser.parse(candidateQInfo.trim(),
					containerFactory);
			Iterator iter = json.entrySet().iterator();
			
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();

				if (entry.getKey().toString().equalsIgnoreCase("QU"))
					candidate.setQuery(entry.getValue().toString());
				else if (entry.getKey().toString().equalsIgnoreCase("AF"))
					candidate.addOutputs(entry.getValue().toString());
				else if (entry.getKey().toString().equalsIgnoreCase("DI")) {
					// Do we need this ??
				} else if (entry.getKey().toString().equalsIgnoreCase("NG")) {
					LinkedList array = (LinkedList) entry.getValue();
					for (Object term : array)
						candidate.addInputs(term.toString().trim());

				} else if (entry.getKey().toString().equalsIgnoreCase("WH")) {
					// Do we need this ?
				}

				// System.out.println(entry.getKey() + " => " + entry.getValue());
			}
			
			disableInvalidTerms(candidate);

		} catch (ParseException pe) {

			System.out.println("Invalid input. EXIT! " + pe.toString());
			System.exit(2);
		}
		
		return candidate;
	}
	
	
	private static void disableInvalidTerms(SSQClass candidate) {
		
		ArrayList<Term> inputTerms = candidate.getInputs();
		int N = 3; // right now we're using up to TRI-GRAMS 
		int highest = 0;
		double highestValue = 0.0;
		
		for (int i = 0; i < inputTerms.size(); i++){

			Term tc = inputTerms.get(i);
			
			if (!tc.processed){
				
				ArrayList<Integer> idx = new ArrayList<Integer>();
				idx.add(i);
				highest = i;
				highestValue = tc.getMostProbableCategory().category.equalsIgnoreCase("UNKNOWN")
					? 0.0 : tc.getMostProbableCategory().probability;
				
				for (int j = i+1; (j < i+N) && (j < inputTerms.size()) ; j++){
					Term t = inputTerms.get(j);
					
					if (t.term.contains(tc.term)){
						idx.add(j);
						if ((t.getMostProbableCategory().probability > highestValue)
								&& !t.getMostProbableCategory().category.equalsIgnoreCase("UNKNOWN")){
							highest = j;
							highestValue = t.getMostProbableCategory().probability;
						}
					}
					else 
						// we assume that the next gram is different 
						break; 
				}
				
				for (int k : idx){
					Term tk = inputTerms.get(k);
					if (k != highest)
						tk.valid = false;					
					tk.processed = true;
				}
				
			}
		}
		
	}

	public static void readConfig() {

		try {
			BufferedReader input = new BufferedReader(new FileReader(
					"config.ini"));
			String line = null;

			while ((line = input.readLine()) != null) {

				String[] la = line.trim().split("="); 
				
				if (la[0].equalsIgnoreCase("ONTOLOGY_NS"))
					ONTOLOGY_NS = la[1];
				
				else if (la[0].equalsIgnoreCase("ONTOLOGY_CLASSES_NS"))
					ONTOLOGY_CLASSES_NS = la[1];
				
				else if (la[0].equalsIgnoreCase("ONTOLOGY_DATAPROPERTY_NS"))
					ONTOLOGY_DATAPROPERTY_NS = la[1];

				else if (la[0].equalsIgnoreCase("ONTOLOGY_PROPERTY_TREE_HEIGHT"))
					ONTOLOGY_PROPERTY_TREE_HEIGHT = la[1];	
				
				else if (la[0].equalsIgnoreCase("QUALIFIED_SSQIDS"))
					QUALIFIED_SSQIDS = la[1];

				else if (la[0].equalsIgnoreCase("INPUT_TERMS_WEIGHT"))
					INPUT_TERMS_WEIGHT = Double.parseDouble(la[1]);
				
				else if (la[0].equalsIgnoreCase("OUTPUT_TERMS_WEIGHT"))
					OUTPUT_TERMS_WEIGHT = Double.parseDouble(la[1]);				
				
			}

			input.close();
		} catch (Exception e) {
			System.out.println("Error in reading config file. EXIT!");
			System.exit(1);
		}

	}
	
	
	
	/**
	 * Main 
	 * 
	 * 
	 * "Q:a 1997 Toyota Camry V6 needs what size tires?; 
	 * AF:size tires; DI:1997 Toyota Camry V6; NGDI:1997,
	 * 1997 Toyota,1997 Toyota Camry,Toyota,Toyota Camry,
	 * Toyota Camry V6,Camry,Camry V6,V6,; WH:what;"
	 * 
	 * @param args
	 * @throws IOException 
	 * 
	 */
	
	
	public static void main(String[] args) throws IOException {
		
		if (args.length < 1){
			System.out.println("Invalid input. EXIT!");
			System.exit(1);
		}
		
		
		String candidateQInfo = "";
		String[] qSSQIds = null;
		ArrayList<SSQMatcher.SSQSimilarityMeasure> qrmSim = null;
		StringBuilder sb = new StringBuilder();  
		SSQClass candidate = null;
		
		// Load configurations  
		readConfig();
		
		candidateQInfo = args[0]; 
		qSSQIds = QUALIFIED_SSQIDS.trim().split(",");
		
		Utils.log("Candidate SSQ: " + candidateQInfo);
		Utils.log("Qualified SSQs: " + QUALIFIED_SSQIDS);
		Utils.log("Building candidate SSQ...");

		candidate = buildCandidateQuery(candidateQInfo);
		Utils.log(candidate.toString());

		sb.append(" { ");
		sb.append(candidate.toSend());
		
		// Gets the SDB data store instance 
		Store sdbStore = SDBHelper.getStore();
		SSQMatcher m = new SSQMatcher(sdbStore);
		
		// Finds SSQ divergence values 
		qrmSim = m.findSSQDivergences(qSSQIds, candidate, true);

		
		sb.append(m.getQueryIdString(qrmSim));
		sb.append("\"nqoutput\": {} } ");

		Utils.log("\nOUTPUT\n" + sb.toString());
		System.out.println(sb.toString());
	}
	

}
