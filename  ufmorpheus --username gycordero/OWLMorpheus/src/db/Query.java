package db;

public class Query {
	private int queryid;
	private String querystring;
	private int realmid;
	private Realm realm;	
	private Individual[] individual;
	
	public Query(int queryid, String querystring, int realmid) {
		this.queryid = queryid;
		this.querystring = querystring;
		this.realmid = realmid;
	}
	public void setRealm(Realm realm) {
		this.realm = realm;
	}
	public void setIndividual(Individual[] individual) {
		this.individual = individual;
	}
	public int getQueryid() {
		return queryid;
	}
	public String getQuerystring() {
		return querystring;
	}
	public Realm getRealm() {
		return realm;
	}
	public Individual[] getIndividual() {
		return individual;
	}
	public int getRealmid() {
		return realmid;
	}
}
