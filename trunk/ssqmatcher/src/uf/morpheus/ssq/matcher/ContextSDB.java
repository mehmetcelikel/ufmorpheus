/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.util.HashSet;
import java.util.Set;

import uf.morpheus.meta.Constants.SSQContexts;

/**
 * This is a base class which represents a SSQ context 
 * 
 * @author Clint P. George
 *
 */
public class ContextSDB 
{
	protected SSQContexts context = SSQContexts.NONE; 
	protected Set <String> ranges = new HashSet<String>();

	public SSQContexts getContext() {
		return context;
	}
	public Set <String> getRanges() {
		return ranges;
	}
	
	public ContextSDB(SSQContexts context) {
		this.context = context;
	}
	
	public void addRanges(String rangeClass) {
		this.ranges.add(rangeClass);
	}
	
	public void setRange(Set <String> rangeClasses) {
		this.ranges = rangeClasses;
	}
}

