package db;

public class Realm {
	private int realmid;
	private String realm;
	
	public Realm(int id, String realm) {
		this.realmid = id;
		this.realm = realm;
	}
	
	public int getRealmid() {
		return realmid;
	}
	
	public String getRealm() {
		return realm.replaceAll(" ", "_");
	}
}
