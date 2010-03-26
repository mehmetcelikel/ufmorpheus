package db;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Properties;

public class DBReader {

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
			String url = "jdbc:postgresql://babylon.cise.ufl.edu:5432/Morpheus3DB";
			Properties props = new Properties();
			props.setProperty("user","morpheus3");
			props.setProperty("password","crimson03.sql");
			connection = DriverManager.getConnection(url, props);
			
			return connection;
		} catch (Exception e) {
			System.out.println("Problem opening database");
			e.printStackTrace();
		}
		return null;
	}
	
	//Getting all from the context table
	public static Context[] getContexts() {
		try {
			connection = openDatabaseConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT contextid, contextname FROM context");
			ArrayList<Context> results = new ArrayList<Context>();
			while (rs.next()) {
				results.add(new Context(rs.getInt(1), rs.getString(2)));
			}
			rs.close();
			stmt.close();
			return results.toArray(new Context[] {});
		} catch (Exception e){ 
			e.printStackTrace();
			return null;
		}
	}
	
	//Getting all from the class table
	public static ContextClass[] getContextClasses() {
		try {
			connection = openDatabaseConnection();
			ArrayList<ContextClass> results = new ArrayList<ContextClass>();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT classid, contextid, name FROM class");
			while (rs.next()) {
				results.add(new ContextClass(rs.getInt(1),rs.getInt(2), rs.getString(3)));
			}
			rs.close();
			stmt.close();
			return results.toArray(new ContextClass[] {});
		} catch (Exception e){ 
			e.printStackTrace();
			return null;
		}
	}
	
	//Getting all from the Realm table
	public static Realm[] getRealms() {
		try {
			connection = openDatabaseConnection();
			ArrayList<Realm> results = new ArrayList<Realm>();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT realmid, realm FROM realm");
			while (rs.next()) {
				results.add(new Realm(rs.getInt(1),rs.getString(2)));
			}
			rs.close();
			stmt.close();
			return results.toArray(new Realm[] {});
		} catch (Exception e){ 
			e.printStackTrace();
			return null;
		}
	}
	public static Query getQuery(int queryid) {
		Query query = null;
		Realm realm = null;
		try {
			connection = openDatabaseConnection();			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT querystring, realmid FROM query where queryid="+queryid);			
			
			if (rs.next()) {
				query = new Query(queryid,rs.getString(1), rs.getInt(2));
			}
			else
				System.out.println("error creating the query");
			
			//System.out.println("The realm id is : " + query.getRealmid());
			
			//Getting the realm of the query
			
			rs = stmt.executeQuery("SELECT realm.realm FROM realm where realmid =" + query.getRealmid());
			if (rs.next()) {
				//System.out.println(rs.getString(1));
				realm = new Realm(query.getRealmid(),rs.getString(1));
			}
			else {
				//System.out.println("error generating realm");
			}
			//System.out.println(" the realm String is:" + realm.getRealm());			
			 
			//Getting queryhas information queryid and io will return a queryhas[][]
			String[] ids = new String[2];
			String[][] queryhas;
			rs = stmt.executeQuery("SELECT individualid, io FROM queryhas where queryid =" + query.getQueryid());
			ArrayList<String[]> results = new ArrayList<String[]>();
			while (rs.next()) {
				results.add(ids = new String[]{rs.getString(1), rs.getString(2)});

			}
			
			queryhas = results.toArray(new String[][] {});
			//System.out.println("the length of the individual is: " + queryhas.length);
			
			//Creating Individual
			//Context context = null;
			//Modifier modifier = null;
			//Phrase phrase = null;
			//ContextClass contextclass = null;
			Individual[] individual = new Individual[queryhas.length];
			for (int x = 0; x < queryhas.length;x++) {
				//public Individual(int individualid,String phrasestring,String context,String contextclass, int classid, int phraseid)
				String select = "phrase.phrasestring, context.contextname, class.name, class.classid, phrase.phraseid";
				String from = "class, individual, phrase, phrasebelongstocontext, context";				
				String where = "individual.individualid=" + queryhas[x][0] + " and class.classid=individual.classid and phrase.phraseid=individual.phraseid and phrasebelongstocontext.phraseid=phrase.phraseid and context.contextid=phrasebelongstocontext.contextid";
				rs = stmt.executeQuery("SELECT " + select + " FROM " + from + " where "+ where);
				if (rs.next()) {
					individual[x] = new Individual(Integer.parseInt(queryhas[x][0]), queryhas[x][1],rs.getString(1), rs.getString(2),rs.getString(3),rs.getInt(4),rs.getInt(5));
				}
				
				//System.out.println("Individual id: " +individual[x].getIndividualid() + ", phraseString: " +individual[x].getPhrasestring() + ", context: " +individual[x].getContextname() + ", contextclass: " +individual[x].getContextclass() + ", classid: " +individual[x].getClassid() +", phraseid: " +individual[x].getPhraseid());
			}
			
			//Adding modifiers to the individuals
			Modifier m[];			
			ArrayList<Modifier> modifier;// = new ArrayList<Modifier>();
			for (int x = 0; x < individual.length; x++) {				
				modifier = new ArrayList<Modifier>();
				rs = stmt.executeQuery("SELECT modifier.modifierid, modifier.modifierstring, modifier.rank FROM modifier, hasmodifier where hasmodifier.individualid="+individual[x].getIndividualid()+"and hasmodifier.modifierid =modifier.modifierid");
				while (rs.next()) {
					modifier.add(new Modifier(rs.getInt(1),rs.getString(2),rs.getInt(3)));
					//System.out.println("modifier is: " + rs.getString(2)+ "modifier id is: " + rs.getInt(1));

				}
				//Modifier(int modifierid, String modifierstring, int rank)
				m = modifier.toArray(new Modifier[] {});
				//System.out.println("DB Modifier length is: " + m.length);
				individual[x].setModifiers(m);				
				
			}
			
			//Adding individual to the query
			query.setIndividual(individual);
			query.setRealm(realm);
			rs.close();
			stmt.close();
			return query;
			
		} catch (Exception e){ 
			e.printStackTrace();
			return null;
		}
	}
	
	public static Individual getIndividual(int individualid) {
		return null;
	}
/**
 * Testing method
 * @param args
 */
	public static void main(String args[]) {
		openDatabaseConnection();
		Query query = DBReader.getQuery(476);
		Individual[] i = query.getIndividual();		
		for (Individual q: i) {
			System.out.println("Individual id: " + q.getIndividualid());
			System.out.println("	Phrase String: " + q.getPhrasestring());
			System.out.println("	Phraseid: " + q.getPhraseid());
			System.out.println("	Modifiers: " + q.getModifier().length);
			System.out.println("	ContextName: " + q.getContextname());
			System.out.println("	ContextClass: " + q.getContextclass());
		}
	}
}
