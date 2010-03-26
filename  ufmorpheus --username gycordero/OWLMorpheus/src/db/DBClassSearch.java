package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Find on the database if the class exist in a file
 * @author Guillermo
 *
 */
public class DBClassSearch {
	private String cls = null;
	private String[] classFiles = null;

	public String[] getClassFiles() {
		return classFiles;
	}

	public void setClassFiles(String[] classFiles) {
		this.classFiles = classFiles;
	}

	public String getCls() {
		return cls;
	}

	public void setCls(String cls) {
		this.cls = cls;
	}
	
	public DBClassSearch(String cls){
		this.cls = cls;
		classFiles = search();
	}
	/**
	 * Search for the class on the db and return the files where it can be accessed
	 * @return String[] with the files
	 */
	private String[] search() {	
		
		try {
			Connection connection = DBReader.openDatabaseConnection();
			Statement stmt = connection.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT filename FROM dbpediacategoryindex where categoryname='"+cls+"';");
			
			ArrayList<String> results = new ArrayList<String>();
			while (rs.next()) {
				results.add(new String(rs.getString(1)));
				
			}
			rs.close();
			stmt.close();
			return results.toArray(new String[] {});
		} catch (Exception e){ 
			e.printStackTrace();
			return null;
		}		
	}
/**
 * Testing method
 * @param argsp
 */
	public static void main(String argsp[]) {
		String cls = "Automobiles";
		DBClassSearch search = new DBClassSearch(cls);
		for (String x : search.getClassFiles()) {
			System.out.println("file = " + x);
		}
	}
	
	
}
