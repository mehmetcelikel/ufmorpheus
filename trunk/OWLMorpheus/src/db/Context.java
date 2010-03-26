package db;

public class Context {
	private int ContextId;
	private String ContextName;
	
	public Context(int id, String descriptor) {
		this.ContextId = id;
		this.ContextName = descriptor;
	}
	
	public int getContextId() {
		return ContextId;
	}
	
	public String getContextName() {
		return ContextName.replaceAll(" ", "_");
		
	}
}
