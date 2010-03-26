/**
 * 
 */
package uf.morpheus.ssq.matcher;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.Store;

import uf.morpheus.db.SDBHelper;
import uf.morpheus.meta.Constants;
import uf.morpheus.meta.MessageLogger;
import uf.morpheus.meta.Constants.SSQContexts;

/**
 * This class represents SSQ ontology in OWL format 
 * 
 * @author Clint P. George
 * 
 */
public class SSQOntologySDB {
	/**
	 * Variable declarations
	 * 
	 */
	private Set<BaseContextSDB> baseContexts = new HashSet<BaseContextSDB>();
	private Set<ModifiedContextSDB> modifiedContexts = new HashSet<ModifiedContextSDB>();
	private MessageLogger msg = MessageLogger.getInstance();
	private String realm = "";
	private Store store = null; 
	private String ssqId = "";
	
	/**
	 * Property definitions ...
	 * 
	 */
	public String getSsqId() {
		return ssqId;
	}
	public String getRealm() {
		return realm;
	}
	public Set<BaseContextSDB> getBaseContexts() {
		return baseContexts;
	}

	public Set<ModifiedContextSDB> getModifiedContexts() {
		return modifiedContexts;
	}

	/**
	 * This function returns a base Context if it matches with the given context
	 * name of the SSQ
	 * 
	 * @param contextName
	 *            SSQ context name
	 */
	public BaseContextSDB getBaseContext(SSQContexts context) {
		for (BaseContextSDB ct : this.baseContexts)
			if (ct.getContext() == context)
				return ct;

		return null;
	}

	/**
	 * This function returns a modified Context if it matches with the given
	 * context name of the SSQ
	 * 
	 * @param contextName
	 *            SSQ context name
	 */
	public ModifiedContextSDB getModifiedContext(SSQContexts context) {
		for (ModifiedContextSDB ct : this.modifiedContexts)
			if (ct.getContext() == context)
				return ct;

		return null;
	}

	/**
	 * This function adds the base context details
	 * 
	 * @param contextName
	 *            SSQ context name
	 * @param ranges
	 *            context ranges (OWL Classes' URIs)
	 * 
	 */
	public BaseContextSDB addBaseContextRanges(SSQContexts context,
			Set<String> ranges) {
		BaseContextSDB c = this.getBaseContext(context);

		if (c != null) {
			for (String ind : ranges)
				c.addRanges(ind);
		} else {
			c = new BaseContextSDB(context);
			c.setRange(ranges);
			this.baseContexts.add(c);
		}

		return c;
	}
	
	/**
	 * This function adds the base context details
	 * 
	 * @param contextName
	 *            SSQ context name
	 * @param ranges
	 *            context range (OWL Class)
	 * 
	 */
	public BaseContextSDB addBaseContextRange(SSQContexts context,
			String range) {
		BaseContextSDB c = this.getBaseContext(context);

		if (c != null) {
			c.addRanges(range);
		} else {
			c = new BaseContextSDB(context);
			c.addRanges(range);
			this.baseContexts.add(c);
		}

		return c;
	}

	/**
	 * Class constructor
	 * 
	 */
	public SSQOntologySDB(String ssqId, Store store) {
		
		this.ssqId = ssqId;
		this.store = store; 
	}
	
	/**
	 * Loads the SSQ details from the loaded OWL SSQ Ontology...
	 * 
	 */
	public boolean loadSSQDetails() {

		
		if (!isExistsSSQ()){
			msg.logger.severe("SSQ is not exists in the data base!");
			return false; 
		}
		
			
		// Query class
		// this.queryClass = this.getClass(Constants.SSQ_QUERY_CLASS_NAME);

		String queryString = "SELECT * WHERE { <" + this.ssqId + "> ?p ?o }";
		
		ResultSet rs = SDBHelper.execSelect(queryString, store);

		for (; rs.hasNext();) {
			QuerySolution soln = rs.nextSolution();
			
			// Check whether the RDF node is resource...
			if (!(soln.get("p").isResource() && soln.get("o").isResource()))
				continue; 

			Resource p = soln.getResource("p");
			Resource o = soln.getResource("o");

			String pty = p.toString().split("#")[p.toString().split("#").length-1];
			
			if (pty.equals(Constants.SSQ_REALM_PROPERTY_NAME))
				this.realm = o.toString().split("#")[p.toString().split("#").length-1];

			else {
				SSQContexts c = SSQContexts.getContext(pty);
				if (c != null){ // i.e. the triple represents a context 
				
					msg.logger.log(Level.INFO, "loaded the SSQ context " + pty);
					
					String bc = "<http://zion.cise.ufl.edu/ontology/Properties/OWLProperties.xml#belongsToClass>";
					String qs = "SELECT * WHERE { <" + o.toString() + "> " + bc + " ?o }";
				
					ResultSet rsType = SDBHelper.execSelect(qs, store);
					Set <String> types = new HashSet<String>();
					for (; rsType.hasNext();) {
						QuerySolution s = rsType.nextSolution();
						String temp = s.getResource("o").toString();

						types.add(temp.split("#")[temp.split("#").length-1]);
					}
					this.addBaseContextRanges(c, types);
				}
			}
		}
		
		return true; 
	}
	

	/**
	 * Gets the class object in the ontology based on the given class name
	 * 
	 */
	public String getClass(String name) {
		String owlClass = null;

		// This for loop will look on the ontology for the class given by name
		owlClass = SDBHelper.getOWLClass(Constants.NS_SSQ + name, store); 
		
		return owlClass;
	}
	
	
	public boolean isExistsSSQ(){
		
		boolean ret = false; 
		
		// This is same as checking the type of SSQ-0
		// TODO: Hard coded value ; needs to removed 
		ret = SDBHelper.isATypeOf(
				this.ssqId, 
				"http://zion.cise.ufl.edu/ontology/classes/OWLClasses.xml#SSQ", 
				store);
		
		return ret; 
	}
	
}
