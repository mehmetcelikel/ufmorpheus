/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.util.HashSet;
import java.util.Set;

import uf.morpheus.meta.Constants.SSQContexts;

/**
 * Modified context class which is used to 
 * represent the SSQ context after the processing (QRM) 
 * 
 * @author Clint P. George
 *
 */
public class ModifiedContext extends Context {

	public Set <String> modifiers = new HashSet<String>();
	public Set<String> getModifiers() {
		return modifiers;
	}
	public void setModifiers(Set<String> modifiers) {
		this.modifiers = modifiers;
	}
	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public ModifiedContext(SSQContexts context) {
		super(context);
		
		// TODO Auto-generated constructor stub
	}

}
