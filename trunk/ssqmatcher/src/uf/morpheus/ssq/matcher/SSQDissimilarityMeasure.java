/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.util.ArrayList;

import uf.morpheus.meta.Constants;
import uf.morpheus.meta.Constants.MeasureMethod;
import uf.morpheus.meta.Constants.SSQContexts;

/**
 * This class represents the matching measure between the SSQs 
 *  
 * @author Clint P. George
 * 
 */
public class SSQDissimilarityMeasure {
	
	// Member classes 
	public class ContextMeasure{
		public boolean isApplicable() {
			return isApplicable;
		}
		public void setApplicable(boolean isApplicable) {
			this.isApplicable = isApplicable;
		}
		public double getValue() {
			return value;
		}
		public void setValue(double value) {
			this.value = value;
		}
		public Constants.SSQContexts getContext() {
			return context;
		}
		public void setContext(Constants.SSQContexts context) {
			this.context = context;
		}
		private boolean isApplicable = false;
		private double value = 0.0;
		private Constants.SSQContexts context;
	}
	
	// Variables and Properties 
	private Constants.MeasureMethod measureMethod; 
	private double realmMeasure;
	private ArrayList<ContextMeasure> contextMeasures = new ArrayList<ContextMeasure>();
	
	public Constants.MeasureMethod getMeasureMethod() {
		return measureMethod;
	}

	public SSQDissimilarityMeasure(Constants.MeasureMethod measure) {
		this.measureMethod = measure;
	}

	public double getRealmMeasure() {
		return realmMeasure;
	}

	public void setRealmMeasure(double realmMeasure) {
		this.realmMeasure = realmMeasure;
	}

	public void addContext(
			boolean isApplicable, 
			double measure, 
			Constants.SSQContexts context){
		ContextMeasure cm = new ContextMeasure();
		cm.setApplicable(isApplicable);
		cm.setValue(measure);
		cm.setContext(context);
		
		this.contextMeasures.add(cm);
	}
	
	public ContextMeasure getContextMeasure(
			Constants.SSQContexts context){
		
		ContextMeasure c = null;
		for( ContextMeasure cm : this.contextMeasures)
		{
			if (cm.getContext() == context)
				return cm;
		}
		return c;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		String mes = this.getMeasureMethod() == MeasureMethod.CLASS_DIVERGENCE ? "divergence"
				: "dissimilarity";

		sb.append("The " + mes + " between the realm classes: "
				+ this.getRealmMeasure() + "\n");

		// Calculates the divergences for all the valid
		// contexts defined in the Context Enum
		for (SSQContexts c : SSQContexts.values()) {
			if (c != SSQContexts.NONE) {
				if (this.getContextMeasure(c) != null
						&& this.getContextMeasure(c).isApplicable())
					sb.append("The " + mes
							+ " between the classes in the context " + c
							+ " : " + this.getContextMeasure(c).getValue()
							+ "\n");
			}
		}

		return sb.toString();

	}
}
