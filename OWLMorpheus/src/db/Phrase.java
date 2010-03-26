package db;

public class Phrase {
	private int phraseid;
	private String phrasestring;	
	
	public Phrase(int id, String phrase) {
		phraseid = id;
		phrasestring = phrase;
	}
	
	public int getPhraseid() {
		return phraseid;
	}
	
	public String getPhrasestring() {
		return phrasestring.replaceAll(" ", "_");
	}	
	
}
