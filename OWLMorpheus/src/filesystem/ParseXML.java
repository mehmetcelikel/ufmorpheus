package filesystem;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseXML{
	public static String targetURL = "http:/zion.cise.ufl.edu/ontology/ssq/";
	
	public static String url = "http://zion.cise.ufl.edu//ontology//ssq//index.xml";
	//Add a class to the file
	public static void addClass(String realm, String cls) {
		int r = searchRealm(realm);
		
		if (r == -1) {
			try {
				addRealm(realm, cls);
			} catch (Exception e ) {
				e.printStackTrace();
			}
		}
		else {
			
		}
	}
	public static int searchClass(String cls, int realmIndex) {
		int index = -1;
		Document d = parseURL();
		
		NodeList nl = d.getElementsByTagName("realm");
		Node realm = nl.item(realmIndex);
		
		System.out.println("Realm: " + realm.getTextContent());
		
		//realm.
		Node classe = realm.getFirstChild();
		System.out.println("First class " + classe.getTextContent());
		/*
		Node root = d.getFirstChild();
		System.out.println("Root: " + root.getTextContent());
		
		NodeList realms = root.getChildNodes();
		
		for (int x = 0; x < realms.getLength(); x++ ) {
			System.out.println("Realm " + x + " = " + realms.item(x).getTextContent());
		}
		
		System.out.println("There are realms: " + realms.getLength());
		Node node = realms.item(realmIndex);
		
		System.out.println("Realm : " +node.getTextContent());
		
		NodeList classes = node.getChildNodes();

		/*
		for (int x = 0; x < classes.getLength(); x++) {
			System.out.println("Class " + x +": "+classes.item(x).getTextContent());
			if (classes.item(x).getTextContent().compareTo(cls) == 0) {
				index = x;
				System.out.println("Class founded: "+classes.item(x).getFirstChild().getTextContent());
				break;
			}
		}
		*/
		return index;
	}
	/**
	 * Search for the realm and return the index of the node or -1 if not found
	 * @param url
	 * @param realm
	 * @return node index
	 */
	public static int searchRealm(String realm) {
		int index = -1;
		Document d = parseURL();
		NodeList nl = d.getElementsByTagName("realm");	
		
		// Searching for the realm on the NodeList
		for (int x = 0; x < nl.getLength(); x++) {
			if (nl.item(x).getAttributes().getNamedItem("name").getNodeValue().compareTo(realm) == 0) {
				index = x;
				break;
			}
		}		
		
		return index;
	}
	/**
	 * Read xml file from url and return a document
	 * 
	 * @param url
	 * @return
	 */
	
	public static Document parseURL() {
		System.out.println("Parsing XML file... " + url);
		DocumentBuilder docBuilder;
		Document doc = null;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Wrong parser configuration: " + e.getMessage());
			return null;
		}
		
		//File sourceFile = new File(url);
		URL xmlUrl = null;

		try {
			xmlUrl = new URL(url);
			
			
		} catch (MalformedURLException e1) {
			
			e1.printStackTrace();
		}
		
		InputStream in = null;
		try {
			in = xmlUrl.openStream();
		} catch (IOException e1) {			
			e1.printStackTrace();
		}
		
		try {
			doc = docBuilder.parse(in);
		} catch (SAXException e) {
			System.out.println("Wrong XML file structure: " + e.getMessage());
			return null;
		} catch (IOException e) {
			System.out.println("Could not read source file: " + e.getMessage());
		}
		System.out.println("XML file parsed");
		
		
		return doc;
		
	}

	public static void addRealm(String realm, String cls) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
		//Document doc = parseURL(url);
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse("C:/XML/index.xml");
		
		//Get the first child and add the realm
		Node classes = doc.getFirstChild();		
		Node rlm = doc.createElement("realm");
		NamedNodeMap rlmAttributes = rlm.getAttributes();
		Attr name = doc.createAttribute("name");
		name.setValue(realm);
		rlmAttributes.setNamedItem(name);
		
		//Add class element to the realm
		Node realmClass = doc.createElement("class");			
		rlm.appendChild(realmClass);
		
		//Add name to the class
		Node className = doc.createElement("name");		
		className.setTextContent(cls);
		realmClass.appendChild(className);
		
		//Add url element to the class
		Node realmUrl = doc.createElement("url");		
		realmUrl.setTextContent(targetURL+cls+".xml");
		realmClass.appendChild(realmUrl);
		
		//adding the new class to the realm
		classes.appendChild(rlm);
		
		System.out.println("The first child is: " + rlm.getAttributes().getLength());

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		//initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);

		String xmlString = result.getWriter().toString();
		
		//Save File
		saveXML(url, xmlString);		
	}

	public static void saveXML(String url, String xml) {
		
		File xmlfile= new File("C:\\XML\\index.xml");
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter(new FileWriter(xmlfile));  
		}
		catch (IOException e) {
		    e.printStackTrace();
		} 
		//this is the bit where you write your XML to the file
		try {
		    writer.write(xml);
		}
		catch(IOException e) {
		    e.printStackTrace();
		}

		//close the file 
		try {
		    writer.close();
		}
		catch(IOException e) {
		    e.printStackTrace();
		}
		
		// Save in URL
	}

	public static void main(String args[]) {
		String url = "http:/zion.cise.ufl.edu/ontology/ssq/index.xml";
		//System.out.println("Realm Index "+searchRealm("game"));
		
		//Testing add Realm
		try {
			//addRealm("game", "starcraft2");
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
		searchClass("starcraft2", 0);
	}
}
