package queryrecorderservice;

import javax.xml.bind.annotation.XmlRootElement;

//This class represents the data that will be send to the client as a JSON object
@XmlRootElement
public class GETData {
	private String id; //id of the query

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
