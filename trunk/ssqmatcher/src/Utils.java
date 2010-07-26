
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author clint
 *
 */
public class Utils 
{
	private static String logFile = 
		String.valueOf(System.currentTimeMillis()) + ".log";
	
	
	/**
	 * Show the log message in console 
	 * 
	 * @param message
	 */
	public static void showLog(String message)
	{
		System.out.println(getTime() + " " + message);
	}
	
	
	/**
	 * Gets current time in the string format  
	 * 
	 * @param msg
	 */
	public static String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(cal.getTime());

	}
	
	/**
	 * Log message into a file  
	 * 
	 * @param msg
	 */
	public static void log(String msg) {
		log(msg, false);
    }
	
	public static void log(String msg, boolean t) {
        try {
        	FileWriter aWriter = new FileWriter(logFile, true);
        	if (t){
				TimeZone tz = TimeZone.getTimeZone("EST"); 
				Date now = new Date();
				DateFormat df = new SimpleDateFormat ("yyyy.mm.dd hh:mm:ss ");
				df.setTimeZone(tz);
				String currentTime = df.format(now);

				aWriter.write(currentTime + " " + msg + "\n");
        	}
        	else {
        		aWriter.write(msg + "\n");
        	}
			aWriter.flush();
			aWriter.close();
		} 
        catch (IOException e) {} // Do nothing
    }
	
	

}
