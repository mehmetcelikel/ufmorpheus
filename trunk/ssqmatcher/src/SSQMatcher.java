

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	public class QCTermMeasure {
		public String QualifiedCategory = "";
		public ArrayList<SSQMatcher.TermMeasure> MatchingTermsCD = new ArrayList<SSQMatcher.TermMeasure>();
		
		public QCTermMeasure(String qualifiedCategory) {
			super();
			QualifiedCategory = qualifiedCategory;
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
		

		public TermMeasure(String candidateTerm, String candidateCategory,
				double candidateCategoryProb, String qualifiedTerm,
				String qualifiedCategory, double categoryDivergence) {
			super();
			CandidateTerm = candidateTerm;
			CandidateCategory = candidateCategory;
			CandidateCategoryProb = candidateCategoryProb;
			QualifiedTerm = qualifiedTerm;
			QualifiedCategory = qualifiedCategory;
			CategoryDivergence = categoryDivergence;
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
		
		if (candidate.getInputs().size() == 0)
			return termsCD;
		
		// Assumes that the order of the SSQ terms is not important 
		for (Term tc : candidate.getInputs()){

			Category cc = tc.getMostProbableCategory();
			
			if (cc.category.equalsIgnoreCase("UNKNOWN")){
				termsCD.add(new TermMeasure(tc.term, cc.category, 1.0, "", "", Constants.DEFAULT_CLASS_DIV));
				continue;
			}
			
			for (Term tq : qualified.getInputs()){
				// For the time being we only consider  
				// the most probable category of a candidate term;   
				Category qc = tq.getMostProbableCategory();
				double cdM = cdStore.getClassDivergence(qc.category, cc.category);
				termsCD.add(new TermMeasure(tc.term, cc.category, cc.probability, tq.term, qc.category, cdM));
			}

		}
		
		Collections.sort(termsCD);
		
		return termsCD;
	}
	
	
	
	
	
	/**
	 * "Q:a 1997 Toyota Camry V6 needs what size tires?; AF:size tires; DI:1997 Toyota Camry V6; NGDI:1997,1997 Toyota,1997 Toyota Camry,Toyota,Toyota Camry,Toyota Camry V6,Camry,Camry V6,V6,; WH:what;"
	 * @param args
	 * @throws IOException 
	 * 
	 */
	
	
	public static void main(String[] args) throws IOException {
		
		String candidateQInfo = "";
		
		if (args.length < 1){
			System.out.println("Invalid input. EXIT!");
			System.exit(1);
		}
		
		// Load configurations  
		readConfig();
		
		candidateQInfo = args[0]; 
		String[] qSSQIds = QUALIFIED_SSQIDS.trim().split(",");
		
		Utils.log("Candidate SSQ: " + candidateQInfo);
		Utils.log("Qualified SSQs: " + QUALIFIED_SSQIDS);
		Utils.log("Building candidate SSQ...");


		StringBuilder sb = new StringBuilder();  

		SSQClass candidate = buildCandidateQuery(candidateQInfo);

		sb.append(" { ");
		
		sb.append(candidate.toSend());
		
		Utils.log(candidate.toString());

		// Gets the SDB data store instance 
		Store sdbStore = SDBHelper.getStore();
		SSQMatcher m = new SSQMatcher(sdbStore);
		
		ArrayList<SSQMatcher.SSQSimilarityMeasure> qrmSim = m.findSSQRelevance(qSSQIds, candidate);

		
		sb.append(m.getQueryIdString(qrmSim));

		sb.append("\"nqoutput\": {} } ");
		
		Utils.log("\nOUTPUT\n" + sb.toString());
		
		System.out.println(sb.toString());
	}
	
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
	
	public ArrayList<SSQMatcher.SSQSimilarityMeasure> findSSQRelevance(
			String[] qSSQIds, 
			SSQClass candidate){
		
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
			double ssqSim = 0.0;
	
			if (N <= 0){
				Utils.log("The SSQ " + qualified.getSsqId() + "is skipped", true);
				continue;
			}
			
			ArrayList<SSQMatcher.TermMeasure> termsDiv = this.calcSSQTermsSimilarity(qualified, candidate);
			
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
			
			ssqSim = 0.0;
			for (SSQMatcher.TermMeasure tm : termsDiv){
				Utils.log(tm.toString());
				ssqSim += tm.CategoryDivergence;
			}
			
		
			ssqSimilarities.add(new SSQSimilarityMeasure((ssqSim / (double)termsDiv.size()), termsDiv, Integer.parseInt(qualifiedSSQId)));
			
			Utils.log("\nTotal terms category divergence : " + ssqSim / (double)termsDiv.size()  + "\n");
			
			
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

		} catch (ParseException pe) {

			System.out.println("Invalid input. EXIT! " + pe.toString());
			System.exit(2);
		}
		
		return candidate;
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
				
				
			}

			input.close();
		} catch (Exception e) {
			System.out.println("Error in reading config file. EXIT!");
			System.exit(1);
		}

	}
	
}