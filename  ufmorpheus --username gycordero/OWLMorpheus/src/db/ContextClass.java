package db;

public class ContextClass {
	private int classid;
	private int contextid;
	private String name;
	
	public ContextClass(int classid, int contextid, String name) {
		this.classid = classid;
		this.contextid = contextid;
		this.name = name;
	}
	public int getClassid() {
		return classid;
	}
	public int getContextid() {
		return contextid;
	}
	public String getname() {
		return name.replaceAll(" ", "_");
		
	}
	
}
