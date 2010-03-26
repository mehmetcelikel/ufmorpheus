package filesystem;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import db.DBClassSearch;

import uf.morpheus.db.DBpediaHelper;

public class ClassSearch {
	public static String uri = "http://zion.cise.ufl.edu/ontology/classes/";
	/**
	 * Search inside index.xml on zion for the class
	 * @param cls Class
	 * @param realm 
	 * @return
	 */	
	public static String SearchClass(String cls, String realm) {			
		
		
		/* Old code to test the url if the file is there
	    try {
	        HttpURLConnection.setFollowRedirects(false);
	        HttpURLConnection con =
	           (HttpURLConnection) new URL(uri+cls+".xml").openConnection();
	        	
	        con.setRequestMethod("HEAD");
	        exists = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
	      }
	      catch (Exception e) {
	         e.printStackTrace();
	         exists = false;
	      }
	    */
		DBClassSearch search = new DBClassSearch(cls);
		String[] classFiles = search.getClassFiles();
		
		//System.out.println("The lenght is : " +classFiles.length+" The class is: " + cls);
	    if (classFiles.length >= 1) {
	    	return uri+classFiles[0]+"#"+cls;	
	    }
	    else {	 
	    	//Creating File	    	
	    	return createClass(cls);	    	
	    }
	    	
	}
	/**
	 * Create a new class from DBPedia and store it 
	 * @param cls
	 * @return
	 */
	public static String createClass(String cls) {		
		String store = "C:\\temp\\"+cls+".xml";		
		String temp = "file:/C://temp//"+cls+".xml";
		String[] args = new String[]{"1", cls,temp ,uri, "10","2","1"};		
		DBpediaHelper.main(args); 		
		filesystem.FTPUploadFile.transferZion(store, "/var/www/ontology/classes");
		return uri+cls+".xml#"+cls;
		
	}
	public static void main(String args[]) {
		//String cls = "test";
		//String realm = null;
		//System.out.println("Url: " + SearchClass(cls, realm));
		//createClass("travel");
		System.out.println("class Created");
		
		String url = createClass("year,make,model,option,Size,Automotive");
		System.out.println("Ontology Created " + url);
	}
}
