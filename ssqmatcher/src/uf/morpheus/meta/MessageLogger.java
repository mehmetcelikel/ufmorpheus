/**
 * 
 */
package uf.morpheus.meta;

import java.io.IOException;
import java.util.logging.*;
/**
 * @author Clint
 *
 */
public class MessageLogger {
    public Logger logger = null;
    private static MessageLogger instance = null;
	
	private MessageLogger() {
		try {
		
	      FileHandler fh = new FileHandler(String.valueOf(System.currentTimeMillis()) + ".log");
	      fh.setFormatter(new Formatter() {
	         public String format(LogRecord rec) {
	            StringBuffer buf = new StringBuffer(1000);
	            buf.append(new java.util.Date());
	            buf.append(' ');
	            buf.append(rec.getLevel());
	            buf.append(' ');
	            buf.append(formatMessage(rec));
	            buf.append('\n');
	            return buf.toString();
	            }
	          });
	      logger = Logger.getLogger("uf.morpheus");
	      logger.addHandler(fh);			
			
		} catch (SecurityException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	public static MessageLogger getInstance(){
		if(instance == null) {
	         instance = new MessageLogger();
	      }
	      return instance;
	}
	
	public void disableConsoleHandler(){
		Handler[] handlers = logger.getHandlers();
		if (handlers[0] instanceof ConsoleHandler) {
			logger.removeHandler(handlers[0]);
		}
	}
}
