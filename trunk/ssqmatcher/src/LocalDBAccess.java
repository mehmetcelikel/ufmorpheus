/**
 * 
 */


import java.sql.*;
import java.util.Properties;

/**
 * @author Clint
 *
 */
public class LocalDBAccess {
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
			props.setProperty("user", Constants.TEST_DB_USERNAME);
			props.setProperty("password", Constants.TEST_DB_PWD);
			connection = DriverManager.getConnection(Constants.TEST_DB_URL, props);

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
			rs = executeSelect("select msr_getcondprob('3', 'toyota camry v6', 'automobile')");
			
			if (rs.next()) {
				System.out.println(rs.getDouble(1));
			}
			else
				System.out.println("error creating the query");

		} catch (SQLException e) {
			e.printStackTrace();
		} 


	}
	
}
