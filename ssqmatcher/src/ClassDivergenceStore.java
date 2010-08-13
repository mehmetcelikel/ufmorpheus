import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.hp.hpl.jena.sdb.Store;

/**
 * 
 */

/**
 * @author clint
 *
 */
public class ClassDivergenceStore {

	/**
	 * @author Clint P. George
	 *
	 */
	public class ClassDivergenceValues {

		private String sourceClass = "";
		private String targetClass = "";
		
		private double classDivergence = 0.0;

		public String getSourceClass() {
			return sourceClass;
		}

		public void setSourceClass(String sourceClass) {
			this.sourceClass = sourceClass;
		}

		public String getTargetClass() {
			return targetClass;
		}

		public void setTargetClass(String targetClass) {
			this.targetClass = targetClass;
		}

		public double getClassDivergence() {
			return classDivergence;
		}

		public void setClassDivergence(double classDivergence) {
			this.classDivergence = classDivergence;
		}

		public ClassDivergenceValues(
				String sourceClass, 
				String targetClass, 
				double classDiv) {
			super();
			this.sourceClass = sourceClass;
			this.targetClass = targetClass;
			this.classDivergence = classDiv;
		}
		
		public String toString(){
			
			return "[" + this.sourceClass + ", " + this.targetClass + ", " + this.classDivergence + "]";
		}

	}

	
	

	private CategoryDivergence cd = null;
	private static ArrayList<ClassDivergenceValues> cdAL = new ArrayList<ClassDivergenceValues>();
	
	public ClassDivergenceStore(Store store, String classNS, String dataPropertyNS, String treeHeight)  {
		super();
		this.cd =  new CategoryDivergence(
				store, 
				classNS, 
				dataPropertyNS, 
				treeHeight);	
	}

	public double getClassDivergence(
			String sourceClass, 
			String targetClass){
		
		double classDiv = 1.0;
		
		// Case 1: when the strings match
		if (sourceClass.equalsIgnoreCase(targetClass)) 
			return 0.0;	
		
		for (ClassDivergenceValues cdv : cdAL)			
			if (cdv.getSourceClass().equalsIgnoreCase(sourceClass)
					&& cdv.getTargetClass().equalsIgnoreCase(targetClass))
				return cdv.getClassDivergence();		
		

		
		// Checks whether we already 
		// calculated the CD value 
		classDiv = getClassDivergenceFromDB(
				sourceClass, 
				targetClass);
		
		if (classDiv < 0){

			// Calculates from SDB 
			classDiv = cd.findOWLClassDivergence(
				sourceClass, 
				targetClass);
			
			// Inserts into the persistent storage 
			insertClassDivergenceValuesToDB(
					sourceClass, 
					targetClass, 
					classDiv);			
		}
		

		
		cdAL.add(new ClassDivergenceValues(sourceClass, targetClass, classDiv));
		
		return classDiv;
	}

	private double getClassDivergenceFromDB(
			String sourceClass,
			String targetClass) {
		
		double cd = -1;
		ResultSet rs;
		
		String selectSQL = "SELECT divergence FROM classdivergence WHERE sourceclass = '" 
			+ sourceClass.trim() 
			+ "' AND targetclass = '" 
			+ targetClass.trim() + "'";
		
		try {
			rs = NLPDBAccess.executeSelect(selectSQL);
			while (rs.next())
				cd = rs.getDouble("divergence");
		} catch (SQLException e) {
			cd = -1;
		}

		return cd;
	}

	public void insertClassDivergenceValuesToDB(
			String sourceClass,
			String targetClass, 
			double classDiv) {
	
		String insertSQL = "INSERT INTO classdivergence(sourceclass, targetclass, divergence) " 
			+ "VALUES('" 
			+ sourceClass.trim() + "', '" 
			+ targetClass.trim() + "', "
			+ classDiv + ");";
		
		try {
			NLPDBAccess.executeInsert(insertSQL);
		} catch (SQLException e) {}
		
	}

}
