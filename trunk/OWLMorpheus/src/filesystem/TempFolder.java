package filesystem;

public class TempFolder {
	private String ontologyPath;
	private String uploadPath;
	public TempFolder() {
		//This is the property name for accessing OS temporary directory or
		
		// folder.
		
		String property = "java.io.tmpdir";	 
		
		// Get the temporary directory and print it.
		
		String tempDir = System.getProperty(property);
				
		uploadPath = tempDir;
		ontologyPath = "file:/"+tempDir.replace("\\","/");
		
	}
	public static void main(String[] args)
	
	{
	
	TempFolder folder = new TempFolder();
	
	System.out.println("OS current temporary directory is " + folder.getUploadPath());
	System.out.println("Upload Path " + folder.getOntologyPath());
	
	}
	public String getUploadPath() {
		return uploadPath;
	}
	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}
	public String getOntologyPath() {
		return ontologyPath;
	}
	public void setOntologyPath(String ontologyPath) {
		this.ontologyPath = ontologyPath;
	}
	
}
