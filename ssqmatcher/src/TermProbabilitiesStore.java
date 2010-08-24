import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;


public class TermProbabilitiesStore {
	
	
	public static ArrayList<String> categoryNames = new ArrayList<String>();
	public static ArrayList<TermProbability> termProbabilities = new ArrayList<TermProbability>(); 
	
	/**
	 * Loads classes/ categories from the SDB data base  
	 *  
	 */
	
	
	public static void loadClasses(){
		
		if (categoryNames.size() > 0)
			return;
		
		// Gets the probabilities from MS N-gram service 
		
		ResultSet rsClasses;
		try {
			rsClasses = SDBAccess.executeSelect(
					"SELECT substring(lex from length('http://zion.cise.ufl.edu/ontology/Vehicle/Class/')+1) FROM nodes WHERE lex LIKE 'http://zion.cise.ufl.edu/ontology/Vehicle/Class/%'; ");
			
			while(rsClasses.next())
				categoryNames.add(rsClasses.getString(1));

		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
	}

	
	/**
	 * Gets term probabilities from Microsoft N-gram service 
	 * TODO: Not tested in the babylon data base 
	 * 
	 * 
	 * @param term term from a user query 
	 * @return
	 */
		
	public static ArrayList<Category> getTermProbabilites(String term){
		
		ArrayList<Category> categories = new ArrayList<Category>(); 
		
		// Gets from memory 
		for (TermProbability tp : termProbabilities){
			if (tp.Term.equalsIgnoreCase(term)){
				categories = tp.Categories;
				break;
			}
		}
		
		if (categories.size() == 0){
			ResultSet rs = null;
			for (String categoryName : categoryNames){
				try {
					rs = DBAccess.executeSelect("select msr_getcondprob('3', '"
							+ term + "', '" + categoryName+"')");
					if (rs.next()) 
						categories.add(new Category(categoryName, rs.getDouble(1)));

				} catch (SQLException e) {} 
			}

			// Adds the term and probabilities into memory 
			if (categories.size() > 0)
				termProbabilities.add(new TermProbability(term, categories));
		}
		
		return categories;		
	}
	
	
	/**
	 * Gets term's associated classes and their probabilities 
	 * @param term
	 * @return
	 */
	
	public static ArrayList<Category> getTermProbabilitiesFromNLPDB(String term, Constants.TermType type){


		float numTermInstance = 0;
		float totalTermInstance = 0;
		ArrayList<Category> categories = new ArrayList<Category>(); 
		
		// Case 1 
		// Gets from memory 
		for (TermProbability tp : termProbabilities)
			if (tp.Term.equalsIgnoreCase(term))
				return tp.Categories;

		// Case 2: Year
		// Handles the terms that contains years 
		// TODO: This needs to be improved 
		if (containsYear(term)){
			
			switch(type){
				case ONE_GRAM:
					categories.add(new Category("Year", 1.0));
					break;
				case TWO_GRAM:
					categories.add(new Category("Year", 0.8));
					break;
				case THREE_GRAM:
					categories.add(new Category("Year", 0.6));
					break;
				default:
					categories.add(new Category("Year", 0.4));
					break;
			}
			termProbabilities.add(new TermProbability(term, categories));
			return categories;
		}

		// Case 3: Gets the probabilities from the termcategories table 
		// TODO: Currently, we manually enter these values. We're expecting 
		// some other automatic mechanism to do so  
		String selectSQL = "SELECT * FROM termcategories WHERE term LIKE '" + term.trim() + "'";
		ResultSet rs;
		try {
			rs = NLPDBAccess.executeSelect(selectSQL);
			while (rs.next()) 
				categories.add(new Category(rs.getString("category"), rs.getDouble("cp"))); 
			Collections.sort(categories);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		// Stores to memory 
		if (categories.size() > 0){
			termProbabilities.add(new TermProbability(term, categories));
			return categories; 
		}

		// Case 4: Using frequency dist.  
		// If we couldn't find from the cached table, 
		// calculates it from NLPDB term frequencies 
		// Assign categories & probabilities to the term
		try {

			selectSQL = "SELECT * FROM grams WHERE gram LIKE '" + term.trim() + "'";
			rs = NLPDBAccess.executeSelect(selectSQL);
			boolean exits = false;
			while (rs.next()) {
				numTermInstance = rs.getInt("count");
				exits = true;
				break;
			}
			if (!exits) {
				Utils.log("The " + term + " is not in the corpus!", true);
				categories.add(new Category("UNKNOWN", 1));
				return categories;
			}

			selectSQL = "SELECT SUM(count) FROM categories ";
			rs = NLPDBAccess.executeSelect(selectSQL);
			while (rs.next())
				totalTermInstance = rs.getInt("sum");

			selectSQL = "SELECT * FROM categorygrams WHERE gram LIKE '"
					+ term.trim() + "'";
			rs = NLPDBAccess.executeSelect(selectSQL);
			while (rs.next()) {

				String category = rs.getString("category");
				float categoryTermInstances = rs.getInt("count");
				float categoryInstances = getCategoryInstances(category);

				float prob_category = categoryInstances / totalTermInstance;
				float prob_term = numTermInstance / totalTermInstance;
				float prob_term_given_category = categoryTermInstances
						/ categoryInstances;

				float prob_category_given_term = prob_term_given_category
						* prob_category * prob_term;

				categories.add(new Category(category, prob_category_given_term)); 
			}

			Collections.sort(categories);
			
			// Adds the term and probabilities into memory 
			if (categories.size() > 0)
				termProbabilities.add(new TermProbability(term, categories));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return categories;
	}
	

	private static int getCategoryInstances(String category) throws SQLException {

		int num = 0;
		ResultSet rs = NLPDBAccess.executeSelect(
				"SELECT * FROM category WHERE category LIKE '" + category
						+ "'");
		while (rs.next()) {
			num = rs.getInt("count");
			break;
		}
		return num;
	}

	
	/**
	 * This function will return true if the given  
	 * term contains an year 
	 * 
	 * TODO: This needs to improved 
	 * */
	
	private static boolean containsYear(String term) {
		String[] strAry = term.trim().toString().split(" ");

		for (String str : strAry)
			if (isValidYear(str))
				return true;

		return false;
	}
	
	public static boolean isValidYear(String inYear) {

		if (inYear == null)
			return false;

		// set the format to use as a constructor argument
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");

		if (inYear.trim().length() != dateFormat.toPattern().length())
			return false;

		dateFormat.setLenient(false);

		try {
			// parse the inDate parameter
			dateFormat.parse(inYear.trim());
		} catch (ParseException pe) {
			return false;
		}
		return true;
	}

	
	
}
