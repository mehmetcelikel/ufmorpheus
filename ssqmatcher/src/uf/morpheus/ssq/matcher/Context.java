/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLDescription;

import uf.morpheus.meta.Constants.SSQContexts;

/**
 * This is a base class which represents a SSQ context 
 * 
 * @author Clint P. George
 *
 */
public class Context 
{
	protected SSQContexts context = SSQContexts.NONE; 
	protected Set <OWLDescription> ranges = new HashSet<OWLDescription>();

	public SSQContexts getContext() {
		return context;
	}
	public Set<OWLDescription> getRanges() {
		return ranges;
	}
	
	public Context(SSQContexts context) {
		this.context = context;
	}
	
	public void addRanges(OWLDescription rangeClass) {
		this.ranges.add(rangeClass);
	}
	
	public void setRange(Set <OWLDescription> rangeClasses) {
		this.ranges = rangeClasses;
	}
}

