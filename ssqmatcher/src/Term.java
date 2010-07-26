

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;





/**
 * Term class: It represents a term in a user query and its characteristics 
 * 
 * @author Clint P. George
 *
 */


public class Term {
	public int order = 0; // Assume that order may make difference in future
	public String term = "";
	public Constants.TermType Type = Constants.TermType.ONE_GRAM; // default is one - gram
	private ArrayList<Category> categories = new ArrayList<Category>();

	
	/**
	 * This constructor is usually called for an SSQ generated by QRR 
	 * 
	 * @param order order of the term in a user query 
	 * @param a term term in the string format 
	 * @param category an annotated category 
	 */
	public Term(int order, String term, String category) {
		this.order = order;
		this.assignTerm(term);
		this.categories.add(new Category(category, 1.0));
	}

	/**
	 * This constructor is usually called for an SSQ generated by QRR 
	 * 
	 * @param a term term in the string format 
	 * @param category an annotated category 
	 */
	public Term(String term, String category) {
		this.assignTerm(term);
		this.categories.add(new Category(category, 1.0));
	}
	
	/**
	 * This constructor is usually called for an SSQ generated by NLP engine  
	 * 
	 * @param order order of the term in a user query (not sure how to handle this!!)
	 * @param term a term in the string format 
	 */
	public Term(int order, String term) {
		this.order = order;
		this.assignTerm(term);
		this.assignCategories(term);
	}
	
	/**
	 * This constructor is usually called for an SSQ generated by NLP engine  
	 * 
	 * @param term a term in the string format 
	 */
	public Term(String term) {
		this.assignTerm(term);
		this.assignCategories(term);
	}

	private void assignCategories(String term) {

		float numTermInstance = 0;
		float totalTermInstance = 0;

		// Assign categories & probabilities to the term
		try {

			String selectSQL = "SELECT * FROM grams WHERE gram LIKE '"
					+ term.trim() + "'";
			ResultSet rs = NLPDBAccess.executeSelect(selectSQL);
			boolean exits = false;
			while (rs.next()) {
				numTermInstance = rs.getInt("count");
				exits = true;
				break;
			}
			if (!exits) {
				Utils.log("The " + term + " is not in the corpus!", true);
				this.categories.add(new Category("UNKNOWN", 1));
				return;
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

				this.addCategory(category, prob_category_given_term);
			}

			Collections.sort(this.categories);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private int getCategoryInstances(String category) throws SQLException {

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
	 * Assigns the term to the Term object and classify its gram 
	 * 
	 * @param term a term in the string format 
	 */
	
	private void assignTerm(String term){
		
		String[] strAry = term.toString().split(" ");
		
		switch(strAry.length){
		case 1: 
			this.Type = Constants.TermType.ONE_GRAM;
		case 2: 
			this.Type = Constants.TermType.ONE_GRAM;
		case 3: 
			this.Type = Constants.TermType.ONE_GRAM;
		default: 
			this.Type = Constants.TermType.N_GRAM;
		}
		
		this.term = term.trim(); 		
	}
	
	public void addCategory(String category, double probability){
		this.categories.add(new Category(category, probability));
	}
	
	public ArrayList<Category> getCategories() {
		return this.categories;
	}
	
	public Category getMostProbableCategory() {
		
		Category c = null; 
		
		
		
		c = this.categories.get(0);
		
		return c;
	}
}
