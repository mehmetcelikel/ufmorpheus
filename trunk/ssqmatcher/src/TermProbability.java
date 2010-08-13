import java.util.ArrayList;
import java.util.Collections;

public class TermProbability{
	public String Term = ""; 
	public ArrayList<Category> Categories = new ArrayList<Category>();
	public TermProbability(String term, ArrayList<Category> categories) {
		super();
		this.Term = term;
		this.Categories = categories;
		Collections.sort(this.Categories);
	}
}