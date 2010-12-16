package xmlparser;

import java.sql.SQLException;

import db.DBQueries;

public class TestClass {
	public static void main(String args[]) throws SQLException {
		
		//Test for XMLParser
		FileReader x = new FileReader("C:/xml/c.xml");
		XMLParser xml = new XMLParser(x.xml);
		
		/*
		//Test for XMLParser
		FileReader x = new FileReader("C:/xml/all.xml");
		XMLParser xml = new XMLParser(x.xml);
		*/
		
		//Test updating the database with this data
		DBQueries.updateDatabase(xml);
		
		/*
		//Test Inputs
		x = new FileReader("C:/xml/inputs.xml");
		XMLParser inputs = new XMLParser(x.xml);
		
		//Test Forms
		x = new FileReader("C:/xml/forms.xml");
		XMLParser forms = new XMLParser(x.xml);
		*/
		
	}
}
