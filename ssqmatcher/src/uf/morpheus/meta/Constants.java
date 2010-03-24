/**
 * 
 */
package uf.morpheus.meta;

/**
 * @author Clint P. George
 * 
 */
public final class Constants {

	public static final String SSQ_CLASS_NAME = "SSQ";
	public static final String SSQ_QUERY_CLASS_NAME = "QueryClass";
	public static final String SSQ_REALM_PROPERTY_NAME = "hasRealm";
	public static final String SSQ_CONTEXT_PROPERTY_BELONGSTOCLASS = "belongsToClass";
	public static final String DBPEDIA_PROPERTY_TREE_HEIGHT = "treeHeight";
	public static final String DBPEDIA_ROOT_CLASS_NAME = "DBpediaRoot";
	public static final double DISSIMILARITY = 1.0;
	public static final double SIMILARITY = 0.0;
	public static final String CLASS_HIERARCHY_ONTOLOGY_FOLDER = "http://zion.cise.ufl.edu/ontology/classes/";
	
	// DB details 
	public static final String DB_URL = "jdbc:postgresql://babylon.cise.ufl.edu:5432/Morpheus3DB";
	public static final String DB_USERNAME = "morpheus3";
	public static final String DB_PWD = "crimson03.sql";
	public static final String SDB_URL = "jdbc:postgresql://babylon.cise.ufl.edu:5432/sdb";
	public static final String DB_TYPE = "PostgreSQL";
	public static final String DB_DRIVER = "org.postgresql.Driver";
	
	public static final String TEST_DB_USERNAME = "postgres";
	public static final String TEST_DB_PWD = "sombrero";
	public static final String TEST_SDB_URL = "jdbc:postgresql://localhost:5432/sdb";
	public static final String TEST_JDB_URL = "jdbc:postgresql://localhost:5432/jenatest";
	
	
	public static final String NS_RDFS = "http://www.w3.org/2000/01/rdf-schema#";

	/**
	 * Different matching levels
	 * 
	 */
	public static enum StringDistanceAlgorithm {
		EQUAL_DISTANCE, HAMMINGDISTANCE, JARO_MEASURE, JARO_WINKLER_MEASURE, LEVENSHTEIN_DISTANCE, NEEDLEMAN_WUNCH_2_DISTANCE, NGRAM_DISTANCE, SMOA_DISTANCE, SUBSTRING_DISTANCE
	}

	/**
	 * Different measure types
	 * 
	 */
	public static enum MeasureMethod {
		CLASS_DIVERGENCE, URI_DISSIMILARITY
	}
	
	/**
	 * Different matching levels
	 * 
	 */
	public static enum MatchingLevel {
		LEVEL_00, LEVEL_01, LEVEL_02
	}

	/**
	 * Enumerator data type for the SSQ contexts
	 * 
	 */
	public static enum SSQContexts {
		NONE(0), WHAT_INPUT(1), WHAT_OUTPUT(2), WHEN_INPUT(3), WHEN_OUTPUT(4), WHERE_INPUT(5), WHERE_OUTPUT(
				6), WHO_INPUT(7), WHO_OUTPUT(8), HOW_INPUT(9), HOW_OUTPUT(10);

		private int index = 0;

		public int index() {
			return this.index;
		}

		SSQContexts(int index) {
			this.index = index;
		}

		/**
		 * The function that returns SSQ Context type based on the given context
		 * name
		 * 
		 * @param contextName
		 *            Context name
		 * 
		 */
		public static SSQContexts getContext(String contextName) {
			for (SSQContexts contexts : SSQContexts.values())
				if (contextName.toUpperCase().equalsIgnoreCase(
						contexts.toString()))
					return contexts;

			return null;
		}
	};
}
