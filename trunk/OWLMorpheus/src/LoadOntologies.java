
public class LoadOntologies {
	public static void main(String args[]) {
		//"347",
		String[] queries = new String[]{"473","476"};
		
		for (String query: queries) {
			String[] q = new String[]{query};
			OWLCreator.main(q);
		}
		
	}
}
