package xmlparser;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class parse a string that contains xml to specific input types
 * @author Guillermo *
 */
public class XMLParser {
	private String xml;	
	private ArrayList<Input> inputs; //Contains all inputs types from the XML
	private String query;
	private String realm;
	
	
	public XMLParser(String xml) {		
		this.xml = xml;
		inputs = new ArrayList<Input>();
		parseXML();
		
	}
	/*
	 * This method gets the xml string and converts it to an InputSource. That way it can be parsed. 
	 */
	
	public void parseXML() {
		try {
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));

	        Document doc = db.parse(is);
	        
	        NodeList parent = doc.getElementsByTagName("morpheus");
	        Element morpheus = (Element) parent.item(0);
	        query = morpheus.getAttribute("query");
	        realm = morpheus.getAttribute("realm");
	        
	        NodeList nodes = doc.getElementsByTagName("input");

	        //System.out.println("There are " + nodes.getLength() + " inputs");
	        
	        // iterate the inputs
	        for (int i = 0; i < nodes.getLength(); i++) {
	           Element element = (Element) nodes.item(i);
	           
	           String type = getText("type", element).trim();
	           
	           //System.out.println(type);
	           if(type.compareTo("highlight") == 0) {	        	   
	        	   parseHighlight(element); 
	        	   System.out.println("Highlight parsed");
	           }
	           else if (type.compareTo("form") == 0 ) {
	        	   parseForm(element);
	        	   System.out.println("Form parsed");
	           }
	        }
	        
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	

	/*
	 *This method get the first child of the element, and have some species cases like anchor and focus 
	 */
	public String getText(String n, Element element) {	
		try {
			if (n.compareTo("anchor")==0) {
				NodeList name = element.getElementsByTagName("xpath");
		        Element anchor = (Element) name.item(0);
		        
		        
		        //System.out.println("The Anchor is: " + anchor.getFirstChild().getTextContent());
				return anchor.getFirstChild().getTextContent().trim();
			}
			else if (n.compareTo("focus")==0){
				NodeList name = element.getElementsByTagName("xpath");
		        
		        Element focus = (Element) name.item(1);
		        
		        //System.out.println("The focus is : " + focus.getFirstChild().getTextContent());
		        
				return focus.getFirstChild().getTextContent().trim();
			}
			else {
				//System.out.println(n);
				NodeList name = element.getElementsByTagName(n);
		        Element line = (Element) name.item(0);
		        
		        //Handle null elements on the XML
		        if (line.hasChildNodes()) {
			        //System.out.println("The first child is: " + line.getFirstChild().getTextContent());
			        
					return line.getFirstChild().getTextContent().trim();
		        }
		        else {
		        	return "";
		        }

				
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
        	
	}
	
	/*
	 * Get an element type highlight and parse it into a Highlight object. 
	 * It also add the object to the inputs ArrayList
	 */
	public void parseHighlight(Element element) {
				
		String type = "highlight";
		String time = getText("time", element);
		String url = getText("url", element);
		String meetpoint = getText("meetpoint", element);
		String xpathAnchor = getText("anchor", element);
		String xpathFocus = getText("focus", element);
		String start = getText("start", element);
		String end = getText("end", element); 
		String answer = getText("answer", element);
		String selection = getText("selection", element);
		String hClass = getText("class", element);
		String page = getText("page", element);
		
		Highlight h = new Highlight( type,  time,  url,  meetpoint,  xpathAnchor,  xpathFocus,  start,  end,  answer,  selection,  hClass,  page) ;
		//System.out.println(h);
		
		addInput("highlight", (Object) h);
		
	}
	/*
	 * Parse forms
	 */
	private void parseForm(Element element) {
		
		String type = "form";
		String time = getText("time", element);
		String url = getText("url", element);
		String xpath = getText("xpath", element);
		String inputList = getText("inputlist", element);
		String node = getText("node", element);
		String page = getText("page", element);
		
		Form f = new Form(type, time, url, xpath, inputList,node, page);
		
		addInput("form", (Object) f);
		//System.out.println(f);
		
	}
	/*
	 * Add inputs to the ArrayList inputs
	 */

	public void addInput(String type, Object o) {
		Input i = new Input(type, o);		
		inputs.add(i);
	}

	
	/*
	 * Setters and Getters
	 */
	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public ArrayList<Input> getInputs() {
		return inputs;
	}

	public void setInputs(ArrayList<Input> inputs) {
		this.inputs = inputs;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}
	
	
}
