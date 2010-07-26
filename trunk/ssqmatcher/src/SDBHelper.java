


import java.util.ArrayList;



import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;


/** Connect to a store using API calls. */ 

public class SDBHelper
{
    public static final String TYPE = Constants.NS_RDF + "type";
    public static final String OWL_CLASS = Constants.NS_OWL + "Class";   
    public static final String SUB_CLASS_OF = Constants.NS_RDFS + "subClassOf";
	
	private static StoreDesc storeDesc = new StoreDesc(
			LayoutType.LayoutTripleNodesIndex, 
			DatabaseType.PostgreSQL) ;
	private static SDBConnection connection = null;
	private static Store dbstore = null; 
	
	
    // Database connection parameters, with defaults
	/**/
	// For local db 
    //private static String s_dbURL = Constants.TEST_SDB_URL;
    //private static String s_dbUser = Constants.TEST_DB_USERNAME;
    //private static String s_dbPw = Constants.TEST_DB_PWD;
    private static String s_dbCreate = "0";
    private static String s_dbClean = "0";
    
    // For babylon 
    private static String s_dbURL = Constants.SDB_URL;
    private static String s_dbUser = Constants.DB_USERNAME;
    private static String s_dbPw = Constants.DB_PWD;
	
	
	/**
	 * Gets the store object (create a new one if it has none)
	 * 
	 */ 
	public static synchronized Store getStore() {
		
		if (dbstore != null) {
			if (!dbstore.isClosed())
				return dbstore;
		}

		try {
			JDBC.loadDriverPGSQL();
			connection = new SDBConnection(s_dbURL, s_dbUser, s_dbPw); 
			dbstore = SDBFactory.connectStore(connection, storeDesc);
			return dbstore;
		} catch (Exception e) {
			Utils.log("Problem opening database. " + e.toString());
			System.exit(1);
		}
		
		return null; 
	}
	
	
	/**
	 * Gets the connection object (create a new one if it has none)
	 * 
	 */ 
	public static synchronized SDBConnection getConnection() {
		
		if (connection != null) {
			return connection;
		}

		try {
			JDBC.loadDriverPGSQL();
			connection = new SDBConnection(s_dbURL, s_dbUser, s_dbPw);
			return connection;
		} catch (Exception e) {
			Utils.log("Problem opening database. " + e.toString());
		}
		
		return null; 
	}
	
	/**
	 * Closes the store and its JDBC connection 
	 * 
	 */ 
	public static synchronized void closeStore() {

		if (dbstore != null) {
			if (!dbstore.isClosed())
				dbstore.close();
			else 
				Utils.log("Problem closing store.");
		}
		
		if (connection != null) {
			connection.close();
		}
	}
	
	
	/**
	 * Create a store in the specified db 
	 *  
	 */ 
	public static Store createStore()
	{
		Store store = getStore();
        store.getTableFormatter().create(); // Creating the database 
        store.getTableFormatter().truncate();  // Truncating all sdb tables 
        return store;
	}
	
	/**
	 * Removes all the data in the store  
	 *  
	 */ 
	public static void cleanStore()
	{
		Store store = getStore();
        store.getTableFormatter().truncate();  // Truncating all sdb tables
	}
	
	
	/**
	 * Gets a named named model 
	 * Note: The triples will be stored in the quads table
	 *  
	 */ 
	public static Model getDBModel(Store store, String modelName)
	{
        Model mdl = SDBFactory.connectNamedModel(store, modelName);    
        return mdl; 
	}
	
	/**
	 * Gets the default model 
	 * Note: the triples will be stored in the triples table.  
	 *  
	 */ 
	public static Model getDBModel(Store store)
	{
        Model mdl = SDBFactory.connectDefaultModel(store);    
        return mdl; 
	}
	
	
	/**
	 * To execute the SPARQL queries on a named model 
	 * Note: the triples will be stored in the triples table.  
	 *  
	 */ 
	public static ResultSet execSelect(String queryString, Model model){
	    
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        
        ResultSet rs = qe.execSelect();

        return rs;
	}
	
	
	/**
	 * To execute the SPARQL queries on the default model 
	 * Note: the triples will be stored in the triples table.  
	 *  
	 */ 
	public static ResultSet execSelect(String queryString, Store store){
	    
		Query query = QueryFactory.create(queryString);
		Dataset ds = SDBFactory.connectDataset(store);

		QueryExecution qe = QueryExecutionFactory.create(query, ds);
		ResultSet rs = qe.execSelect();
    
		return rs;
	}
	
	/**
	 * Checks whether the OWL class is there in the data base 
	 *  
	 * Note: 1. Always append the prefix Constants.NS_CLASSES with className  
	 *		 2. If the database is big it not recommend to use this method   
	 *  
	 */ 
	
	public static boolean containsClass(String className, Model model) {

		boolean ret = false;
		String queryString = "SELECT * { <" + className 
		+ "> <" + TYPE + "> ?o }";
		Query query = QueryFactory.create(queryString);

		QueryExecution qe = QueryExecutionFactory.create(query, model);

		try {
			ResultSet rs = qe.execSelect();

			if (rs != null){
				if (rs.hasNext()){
					QuerySolution qs = rs.next();
					Resource r = qs.getResource("o");
					if (r.toString().equals(OWL_CLASS))
						return true;
				}
			}
		} finally {
			qe.close();
		}

		return ret;
	}

	
	/**
	 * Checks whether the OWL class is there in the data base 
	 *  
	 * Note: Always append the prefix Constants.NS_CLASSES with className   
	 *  
	 */ 
	
	public static boolean containsOWLClass(String className, Store store) {
		boolean ret = false;
		String queryString = "SELECT * { <" + className 
		+ "> <" + TYPE + "> ?o }";
		Query query = QueryFactory.create(queryString);
		Dataset ds = SDBFactory.connectDataset(store);

		QueryExecution qe = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet rs = qe.execSelect();

			if (rs != null){
				if (rs.hasNext()){
					QuerySolution qs = rs.next();
					Resource r = qs.getResource("o");
					if (r.toString().equals(OWL_CLASS))
						return true;
				}
			}
		} finally {
			qe.close();
		}

		return ret;
	}
	
	/**
	 * Checks whether the OWL class is there in the data base 
	 *  
	 * Note: Always append the prefix Constants.NS_CLASSES with className   
	 *  
	 */ 
	
	public static String getOWLClass(String className, Store store) {

		String queryString = "SELECT * { <" + className
				+ "> <" + TYPE + "> ?o }";
		Query query = QueryFactory.create(queryString);
		Dataset ds = SDBFactory.connectDataset(store);

		QueryExecution qe = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet rs = qe.execSelect();

			if (rs != null)
				if (rs.hasNext()) {
					QuerySolution qs = rs.next();
					Resource r = qs.getResource("o");
					if (r.toString().equals(OWL_CLASS))
						return className;
				}
		} finally {
			qe.close();
		}

		return null;
	}
	
	
	/**
	 * Gets the property value  
	 *  
	 * Note: Always append the prefix Constants.NS_CLASSES with className   
	 *  
	 */ 
	
	public static int getPropertyValue(String property, Store store) {

		String queryString = "SELECT * { ?s <" + property + "> ?o }";
		Query query = QueryFactory.create(queryString);
		Dataset ds = SDBFactory.connectDataset(store);

		QueryExecution qe = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet rs = qe.execSelect();

			if (rs != null)
				if (rs.hasNext()) {
					QuerySolution qs = rs.next();
					Literal r = qs.getLiteral("o");
					return r.getInt();
				}
		} finally {
			qe.close();
		}

		return -1;
	}
	

	
	/**
	 * Checks whether the OWL class is there in the data base 
	 *  
	 * Note: Always append the prefix Constants.NS_CLASSES with className and classType    
	 *  
	 */ 
	public static boolean isATypeOf(String className, String classType, Store store) {

		String queryString = "SELECT * { <" + className + "> <" + TYPE + "> ?o }";
		Query query = QueryFactory.create(queryString);
		Dataset ds = SDBFactory.connectDataset(store);

		QueryExecution qe = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet rs = qe.execSelect();

			if (rs != null)
				if (rs.hasNext()) {
					QuerySolution qs = rs.next();
					Resource r = qs.getResource("o");
					
					if (r.toString().equals(classType))
						return true;
				}
		} finally {
			qe.close();
		}

		return false;
	}
	
	
	/**
	 * Checks whether the OWL class is there in the data base
	 *  
	 */
	public static ArrayList<String> getReferencedClasses(
			String classType,
			Store store) {

		String queryString = "SELECT * { ?s <" + TYPE + "> <" + classType
				+ "> }";
		Query query = QueryFactory.create(queryString);
		Dataset ds = SDBFactory.connectDataset(store);
		ArrayList<String> classes = new ArrayList<String>();

		QueryExecution qe = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet rs = qe.execSelect();

			if (rs != null) {
				while (rs.hasNext()) {
					QuerySolution qs = rs.next();
					Resource r = qs.getResource("s");
					classes.add(r.toString());
				}
			}

		} finally {
			qe.close();
		}

		return classes;
	}
	
	
	
	/**
     * Process any command line arguments
     */
    private static void processArgs( String[] args ) {
        int i = 0;
        while (i < args.length) {
            String arg = args[i++];

			if (arg.equals("--dbUser")) {
				s_dbURL = args[i++];
			} else if (arg.equals("--dbURL")) {
				s_dbURL = args[i++];
			} else if (arg.equals("--dbPasswd")) {
				s_dbPw = args[i++];
			} else if (arg.equals("--dbCreate")) {
				s_dbCreate = args[i++];
			} else if (arg.equals("--dbClean")) {
				s_dbClean = args[i++];
			}
        }
    }

	public static ArrayList<String> getOWLSuperClasses(String category, Store store) {

		ArrayList<String> sc = new ArrayList<String>();
		
		String queryString = "SELECT * WHERE { <" + category + "> <"
				+ SUB_CLASS_OF + "> ?o }";
		
		ResultSet rs = SDBHelper.execSelect(queryString, store);

		for (; rs.hasNext();) {
			QuerySolution soln = rs.nextSolution();
			Resource r = soln.getResource("o");
			sc.add(r.toString());
		}

		return sc;
	}
	
    static public void main(String...argv)
    {
    	
    	processArgs(argv); // process the arguments 
    	
    	if (s_dbCreate.equalsIgnoreCase("1"))
    		createStore();
    	else if (s_dbClean.equalsIgnoreCase("1"))
    	 	cleanStore();
    	
    }
  
}

