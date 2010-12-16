package queryrecorderservice;

import java.sql.SQLException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;

import com.sun.jersey.api.json.JSONWithPadding;

import db.DBQueries;

import xmlparser.XMLParser;

/*
 * This class is the interface of the service and performs post and get
 */

@Path("/todo")
public class QueryRecorderInterface {
	
	/**
	 * Gets the xml from the client, parse it and update the database
	 * @param xml from the client
	 * @return the string
	 * @throws SQLException 
	 */	
	//TODO: Handle answers
    @POST
    @Path("/post")
    @Consumes("text/plain")
    @Produces("text/plain")
    public String postIt(String xml) throws SQLException {
        //System.out.println("In POST: xml= \n" + xml );
        
        
        //Parse the xml
        XMLParser data = new XMLParser(xml);         
        System.out.println("The query is : " + data.getQuery() + "\nThe realm is: " + data.getRealm());        
                
        //Update the database  
        DBQueries.updateDatabase(data);      
        System.out.println("database updated");
        
        return null;
    }
    
	
	/*
//This post does not work because the form XML for some reason has a limit on the size of the string
    @POST
    @Path("/post")
    @Consumes("text/plain")
    @Produces("text/plain")
    public String postIt(
    		@FormParam("xml") String xml,
    		@FormParam("id") String id
    
    ) {
        System.out.println("In POST: xml= " + xml + " id= " + id);
        
        //Parse the xml
        //XMLParser inputs = new XMLParser(xml);
        
        
        return xml;
    }
	*/
    /*
	@GET
	@Path("/get")
	@Produces( { "application/x-javascript"})
	public JSONWithPadding getJaxbCollection(@QueryParam("query") String query,@QueryParam("realm") String realm,@QueryParam("callback") String callback) throws Exception {
		
		System.out.println("The query is: " + query + " The realm is : " + realm + "callback: " + callback);
		GETData data = new GETData();
		
		data.setId("82");
		
		return new JSONWithPadding(new GenericEntity<GETData>(data) {}, callback);
		
	}
	*/
    /*
     * The return of the get is not working on the javascript side.
     */
    /*
	@GET
	@Path("/get")
	@Produces( "text/html")
	public String getJaxbCollection(@QueryParam("query") String query,@QueryParam("realm") String realm,@QueryParam("callback") String callback) throws Exception {
		
		System.out.println("The query is: " + query + " The realm is : " + realm + "callback: " + callback);
		GETData data = new GETData();
		
		data.setId("82");
		
		return "TEST";
		
	}	
	*/
}
