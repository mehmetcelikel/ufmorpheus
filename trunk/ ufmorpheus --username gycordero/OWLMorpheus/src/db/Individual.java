package db;

public class Individual {
	private int individualid;
	private String io;
	private String phrasestring;
	private String contextname;
	private String contextclass;
	private int classid;
	private int phraseid;
	private Modifier[] modifiers;
	
	public Individual(int individualid,String io, String phrasestring,String context,String contextclass, int classid, int phraseid) {
		this.individualid = individualid;
		this.io = io;
		this.phrasestring = phrasestring;
		this.contextname = context;
		this.contextclass = contextclass;
		this.classid = classid;
		this.phraseid = phraseid;
	}
	public void setModifiers(Modifier[] modifier) {
		this.modifiers = modifier;
	}
	public Modifier[] getModifier() {
		return modifiers;
	}
	public int getIndividualid() {
		return individualid;
	}
	public String getIo() {
		return io;
	}
	public String getPhrasestring() {
		return phrasestring.replaceAll(" ", "_");
	}
	public String getContextclass() {
		return contextclass.replaceAll(" ", "_");
	}
	public String getContextname() {
		return contextname.replaceAll(" ", "_");
	}
	public int getClassid() {
		return classid;
	}
	public int getPhraseid() {
		return phraseid;
	}
}
