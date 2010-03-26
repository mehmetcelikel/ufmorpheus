package db;

public class Modifier {
	private int modifierid;
	private String modifierstring;
	private int rank;
	public Modifier(int modifierid, String modifierstring, int rank){
		this.modifierid = modifierid;
		this.modifierstring = modifierstring;
		this.rank = rank;
	}
	public int getModifierid() {
		return modifierid;
	}
	public String getModifierstring() {
		return modifierstring.replaceAll(" ", "_");
	}
	public int getRank() {
		return rank;
	}
}
