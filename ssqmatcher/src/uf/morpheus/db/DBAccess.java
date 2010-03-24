/**
 * 
 */
package uf.morpheus.db;

import java.sql.*;
import java.util.Properties;
import uf.morpheus.meta.Constants;
/**
 * @author Clint
 *
 */
public class DBAccess {
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
			Properties props = new Properties();
			props.setProperty("user", Constants.DB_USERNAME);
			props.setProperty("password", Constants.DB_PWD);
			connection = DriverManager.getConnection(Constants.DB_URL, props);

			return connection;
		} catch (Exception e) {
			System.out.println("Problem opening database");
			e.printStackTrace();
		}
		return null;
	}

	public static int executeInsert(String insertSQL) throws SQLException {

		connection = openDatabaseConnection();
		Statement stmt = connection.createStatement();
		int ret = stmt.executeUpdate(insertSQL);

		return ret;
	}

	public static ResultSet executeSelect(String selectSQL) throws SQLException {

		connection = openDatabaseConnection();
		Statement stmt = connection.createStatement();
		ResultSet ret = stmt.executeQuery(selectSQL);

		return ret;
	}

	public static int insertDBpediaCategoryIndex(String fileName,
			String category) throws SQLException {
		return executeInsert("INSERT INTO dbpediacategoryindex(filename, categoryname) VALUES('"
				+ fileName + "', '" + category + "');");
	}
	
	
	/**
	 * @param args
	
	public static void main(String[] args) {

		ResultSet rs;
		try {
			rs = executeSelect("SELECT * FROM dbpediacategoryindex");
			
			insertDBpediaCategoryIndex("test2.xm", "test2");
			
			if (rs.next()) {
				System.out.println(rs.getString(1) + " " + rs.getString(2));
			}
			else
				System.out.println("error creating the query");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		


	}
	*/
}
