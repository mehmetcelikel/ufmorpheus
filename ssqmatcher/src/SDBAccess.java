/**
 * 
 */


import java.sql.*;
import java.util.Properties;

/**
 * @author Clint
 *
 */
public class SDBAccess {
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
			connection = DriverManager.getConnection(Constants.SDB_URL, props);

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

	
	/**
	 * @param args
	*/
	
	public static void main(String[] args) {

		
		ResultSet rs;
		try {
			rs = executeSelect("SELECT substring(lex from length('http://zion.cise.ufl.edu/ontology/Vehicle/Class/')+1) FROM nodes WHERE lex LIKE 'http://zion.cise.ufl.edu/ontology/Vehicle/Class/%'; ");
			
			while(rs.next())
					System.out.println(rs.getString(1));
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 


	}
	
}
