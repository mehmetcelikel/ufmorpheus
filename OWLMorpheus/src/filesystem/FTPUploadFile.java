package filesystem;
import java.io.OutputStream;
import java.io.PrintStream;

import com.zehon.FileTransferStatus;
	import com.zehon.exception.FileTransferException;
import com.zehon.sftp.SFTP;

	/**
	 * Upload the ontology to the zion server
	 * @author Guillermo
	 *
	 */
	public class FTPUploadFile{
		/**
		 * Method to transfer the local file to the Zion server
		 * @param file Where the file is locally located
		 * @param destination	the address of the zion folder
		 */
		public static void transferZion(String file, String destination) {
			
			/*
			//Disable Printing to the screen
			PrintStream printStreamOriginal=System.out; //Standard output
			PrintStream printStreamError=System.err;	//Standard error
			System.setErr(new PrintStream(new OutputStream(){
	        	public void write(int b) {
				}
			}));
	        System.setOut(new PrintStream(new OutputStream(){
	        	public void write(int b) {
				}
			}));
			*/
			
			String host = "zion.cise.ufl.edu";
			String username = "researcher";
			String password = "jopete";
			
			try {
				
				int status = SFTP.sendFile(file, destination, host, username, password);
				if(FileTransferStatus.SUCCESS == status){
					//System.out.println(file + " got sftp-ed successfully to  folder "+destination);
				}
				else if(FileTransferStatus.FAILURE == status){
					//System.out.println("Fail to ssftp  to  folder "+destination);
				}
			} catch (FileTransferException e) {
				e.printStackTrace();
			}
			
			/*
			//Enabling printing to the screen again
			System.setOut(printStreamOriginal);
			System.setErr(printStreamError);
			*/
			
		}
		
		public static void main(String[] args) {
			/*
			String file = "C:\\Users\\Guillermo\\workspace\\OWLMorpheus\\OntologyFiles\\412.xml";
			String destination = "/var/www/ontology/test";
			*/
			TempFolder folder = new TempFolder();
			String file = folder.getUploadPath()+"test.txt";
			String destination = "/var/www/ontology/test";
			
			
			
			FTPUploadFile.transferZion(file, destination);

		}
	}
	

