package xmlparser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/*
 * This class was created for testing only to get the xml from a file into a string
 */
public class FileReader {
	
	public String xml;
	public FileReader(String path) {
		
		try {
			xml = readFileAsString(path);
		} catch (IOException e) {		
			e.printStackTrace();
		} 
	}
	private static String readFileAsString(String filePath) throws java.io.IOException{
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(filePath));
	        f.read(buffer);
	    } finally {
	        if (f != null) try { f.close(); } catch (IOException ignored) { }
	    }
	    return new String(buffer);
	}
	public static void main(String args[]) {
		FileReader x = new FileReader("C:/xml/all.xml");
		System.out.println(x.xml);
	}
}
