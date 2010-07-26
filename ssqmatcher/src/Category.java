

/**
 * Category class 
 * 
 * @author Clint P. George
 *
 */

public class Category implements  Comparable<Category>{
	public String category = "";
	public double probability = 0.0;
	public Category(String category, double probability) {
		super();
		this.category = category;
		this.probability = probability;
	}
	

	public String toString(){		
		String str = category; 
		str +=  ": " + probability;
		return str;		
	}

	@Override
	public int compareTo(Category o) {
		return (this.probability == o.probability) ? 0
				: ((this.probability < o.probability) ? 1 : -1);  

	}
	
}
