package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import xmlparser.Form;
import xmlparser.Highlight;
import xmlparser.Input;
import xmlparser.XMLParser;

public class DBQueries {
	
	private static Connection connection;
	
	public static synchronized Connection openDatabaseConnection() {
		if (connection != null) {
			try {
				if (!connection.isClosed()) {
					return connection;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			DriverManager.registerDriver(new org.postgresql.Driver());
			String url = "jdbc:postgresql://babylon.cise.ufl.edu:5432/Morpheus3DB";
			Properties props = new Properties();
			props.setProperty("user","morpheus3");
			props.setProperty("password","crimson03.sql");
			connection = DriverManager.getConnection(url, props);
			
			return connection;
		} catch (Exception e) {
			System.out.println("Problem opening database");
			e.printStackTrace();
		}
		return null;
	}
	public static void updateDatabase(XMLParser data) throws SQLException {
		
		int queryid = createQuery(data.getQuery(), data.getRealm());
		
		insertInputs(queryid,data.getInputs());
	}
	/**
	 * Insert all inputs types into the Database. For example, forms, highlights.
	 * @param queryid
	 * @throws SQLException 
	 */
	public static void insertInputs(int queryid, ArrayList<Input> inputs) throws SQLException {
		for(Input i: inputs) {
			
			if (i.getType().compareToIgnoreCase("highlight") == 0) {
				Highlight h = (Highlight) i.getData(); 
				createHighlight(h, queryid);
				System.out.println("Highlight inserted");
			}
			else if (i.getType().compareToIgnoreCase("form") == 0) {
				Form form = (Form) i.getData();
				createForm(queryid, form);
				System.out.println("Form inserted");
			}
			else {
				throw new SQLException("Error inserting inputs");
			}
		}
	}
	/**
	 * Inserts tha data for a form on the database
	 * @param queryid
	 * @param form
	 * @throws SQLException
	 */
	public static void createForm(int queryid, Form form) throws SQLException {
		
		//TODO: I think the base URL should not contain parameters. Check that and fix it 
		int pageid = createPage(queryid, form.getUrl());
		int pageReferenceid = createPageReference(queryid, pageid, form);
		
		//TODO: After the form is created, the inputs should be added in this step. However, I could not found information
		//      on the database to do this, so it needs to be fixed
	}
	public static int createPageReference(int queryid, int pageid, Form form) throws SQLException {
		
		connection = openDatabaseConnection();		
		String SQL_INSERT = "INSERT INTO pagereference(pageid, queryid, pagesrc, formxpath, timestamp) VALUES(?,?,?,?,?)";
		
		
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

		//querystring text,
		preparedStatement.setInt(1, pageid);//pageid integer,
		preparedStatement.setInt(2, queryid);//queryid integer,
		preparedStatement.setString(3, form.getPage());//pagesrc text,
		preparedStatement.setString(4, form.getXpath());//formxpath text,		
		preparedStatement.setDouble(5, Double.parseDouble(form.getTime().trim()));//"timestamp" bigint,
		//destinationurl text,
		
		  
		int affectedRows = preparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        ResultSet generatedKey = preparedStatement.getGeneratedKeys();
        
        if (generatedKey.next()) {        	
            return generatedKey.getInt(1);
        } else {        	
            throw new SQLException("Creating user failed, no generated key obtained.");
            
        }
	}
	public static int createPage(int queryid, String baseurl) throws SQLException {
		
		connection = openDatabaseConnection();		
		String SQL_INSERT = "INSERT INTO page(baseurl) VALUES(?)";
		
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);		
		preparedStatement.setString(1, baseurl);		
		
        int affectedRows = preparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        ResultSet generatedKey = preparedStatement.getGeneratedKeys();
        
        if (generatedKey.next()) {        	
            return generatedKey.getInt(1);
        } else {        	
            throw new SQLException("Creating user failed, no generated key obtained.");
            
        }
	}
	/**
	 * Update the database with a Highlight
	 * @param h
	 * @param queryid
	 * @return returns the id of the created highlight
	 * @throws SQLException
	 */
	//TODO: I am not creating an answer. This should be implemented  
	public static int createHighlight(Highlight h, int queryid) throws SQLException {
		
		String cls = h.gethClass();		
		int classid = DBQueries.searchClass(cls);
		
		//Creates the query
		connection = openDatabaseConnection();		
		String SQL_INSERT = "INSERT INTO highlight(beginoffset, endoffset, queryid, startxpath, endxpath, url, " +
				"timestamp, pagesource, classid, meetpoint) VALUES(?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
		
		preparedStatement.setInt(1, Integer.parseInt(h.getStart().trim())); //beginoffset integer
		preparedStatement.setInt(2, Integer.parseInt(h.getEnd().trim())); //endoffset integer
		preparedStatement.setInt(3, queryid); //queryid integer,
		preparedStatement.setString(4, h.getXpathAnchor());//startxpath text,
		preparedStatement.setString(5, h.getXpathFocus());//endxpath text,
		preparedStatement.setString(6, h.getUrl());//url text,			
		preparedStatement.setDouble(7, Double.parseDouble(h.getTime().trim()));//"timestamp" bigint,
		preparedStatement.setString(8, h.getPage());//pagesource text,
		preparedStatement.setInt(9, classid); //classid integer,		
		//answerid integer,
		preparedStatement.setString(10, h.getMeetpoint());//meetpoint text,
	
        int affectedRows = preparedStatement.executeUpdate();
        
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        ResultSet generatedKey = preparedStatement.getGeneratedKeys();
        
        if (generatedKey.next()) {        	
            return generatedKey.getInt(1);
        } else {        	
            throw new SQLException("Creating user failed, no generated key obtained.");
            
        }
	}
	/**
	 * Search the table class for this given class
	 * @param clss
	 * @return classid
	 * @throws SQLException
	 */
	public static int searchClass(String clss) throws SQLException {
		String selectSQL = "SELECT classid FROM class where name='"+clss+"'";
		ResultSet rs = executeSelect(selectSQL);        
		rs.next();
        String realmid =  rs.getString(1);
        
        return Integer.parseInt(realmid.trim());		
		
	}
	/**
	 * Create the query id on the database
	 * This method could be expanded to add more information on the table like qrmid etc...
	 * @param querystring this is the query text
	 * @param realm the id of the realm	 
	 * @return the id of the query
	 * @throws SQLException 
	 */
	public static int createQuery(String querystring, String realm) throws SQLException {
		
		int realmid = findRealm(realm);
		//System.out.println("realmid is " + realmid);
		
		//Creates the query
		connection = openDatabaseConnection();		
		String SQL_INSERT = "INSERT INTO query(querystring, realmid) VALUES(?,?)";
		
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);		
		preparedStatement.setString(1, querystring);
		preparedStatement.setInt(2,realmid);
		
        int affectedRows = preparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        ResultSet generatedKey = preparedStatement.getGeneratedKeys();
        
        if (generatedKey.next()) {        	
            return generatedKey.getInt(1);
        } else {        	
            throw new SQLException("Creating user failed, no generated key obtained.");
            
        }
		
		
	}

	/**
	 * Find the real or call createRealm
	 * @param realm
	 * @return  realmid
	 * @throws SQLException
	 */
	public static int findRealm(String realm) throws SQLException{
		ResultSet rs = executeSelect("SELECT realmid FROM realm where realm='"+realm+"'");
		
        if (rs.next()) {
            //System.out.println("The realm is : " + rs.getString(1));
        	String realmid =  rs.getString(1);
        	return Integer.parseInt(realmid.trim());
        }
        else {
        	
        	return createRealm(realm);
        }
            
        
		
	}
	/**
	 * Create Realm
	 * @param realm
	 * @return the id of the realm
	 * @throws SQLException
	 */
	public static int createRealm(String realm) throws SQLException {
		connection = openDatabaseConnection();		
		String SQL_INSERT = "INSERT INTO realm(realm) VALUES(?)";
		
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);		
		preparedStatement.setString(1, realm);
		
        int affectedRows = preparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        ResultSet generatedKey = preparedStatement.getGeneratedKeys();
        
        if (generatedKey.next()) {        	
            return generatedKey.getInt(1);
        } else {        	
            throw new SQLException("Creating user failed, no generated key obtained.");
            
        }
		
		
	}
    public static ResultSet executeSelect(String selectSQL) throws SQLException {

        connection = openDatabaseConnection();
        Statement stmt = connection.createStatement();
        ResultSet ret = stmt.executeQuery(selectSQL);

        return ret;
    }    
	
}
