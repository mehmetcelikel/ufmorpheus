
package db;

import uf.morpheus.db.SDBHelper;
import com.hp.hpl.jena.sdb.Store;

public class SearchClass {
	
	public static String uri = "http://zion.cise.ufl.edu/ontology/classes#";
	/**
	 * Search for the class on the database using Clint code
	 * @param className
	 * @return
	 */
	public static String Search(String className) {
		Boolean cls;
		Store store = SDBHelper.getStore();
		cls = SDBHelper.containsOWLClass(uri+className, store);
		
		if(cls) 
			return uri+className;
		
		else
			//return uri+"unknown";
			return uri+className;
		
	}
	public static void main(String args[]) {
		System.out.println(Search("Australian_cars"));
		
	}
	
}
