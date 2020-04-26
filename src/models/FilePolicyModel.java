package models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import utilities.*;

public class FilePolicyModel {

	private File targetFile;
	private String separator = System.getProperty("file.separator");
	private int mode; //0: csv; 1: xml
	private Document dom;
	private boolean areTypesSet;
	private Element rootE;
	private String instancename;
	private String[] classnames;
	private String[] columnnames;
	
	public void startSaveSession(SynchedDataDescriptor sddesc, File targetDir, String targetName, int opt, boolean overWrite) throws MyAppException, ParserConfigurationException{
		String appendix;
		switch(opt) {
		case 0: appendix = ".csv"; break;
		case 1: appendix = ".xml"; break;
		default:
			throw new MyAppException("Nem támogatott fájl mentési opció a mentés másként funkcióban.");
		}
		if(targetName.length() >= appendix.length()+1) {
			if(!targetName.substring(targetName.length()-appendix.length(), targetName.length()).equals(appendix)) targetName+=appendix;
		}
		String path = targetDir.getAbsolutePath()+separator+targetName;
		this.targetFile = new File(path);
		if(!overWrite && this.targetFile.exists()) throw new MyAppException("Ez a fájl már létezik! A felülírás nem engedélyezett a mentés másként funkcióban.");
		this.mode = opt;
		switch(opt) {
		case 0:
			//CSV
			break;
		case 1:
			startSaveAsXml(sddesc);
			
			break;
		
		}
	}
	
	public void saveAsCsv() {
		
	}
	
	public void startSaveAsXml(SynchedDataDescriptor sddesc) throws ParserConfigurationException{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		this.dom = db.newDocument();
		String dataName = sddesc.getDataTypeName();
		dataName = dataName != null ? dataName : "Untitled";
		this.rootE = dom.createElement(dataName);
		this.instancename = dataName+"instance";
		this.areTypesSet = sddesc.areTypesSet();
		if(this.areTypesSet) {
			this.classnames = sddesc.getTypes(); 
			this.columnnames = sddesc.getNames();
		}
	}
	
	public void appendRow(Object[] row) {
		Element rowE = dom.createElement(this.instancename);
		for(int i = 0; i < row.length; i++) {
			Element fieldE = dom.createElement(this.columnnames[i]);
			if(this.areTypesSet) {
				fieldE.setAttribute("mytype", this.classnames[i]);
			}
			if(row[i] != null) {
				fieldE.appendChild(dom.createTextNode(row[i].toString()));
			}
			rowE.appendChild(fieldE);
		}
		this.rootE.appendChild(rowE);
	}
	
	public void finishSaveAsXml() throws ParserConfigurationException, IOException, TransformerException{
		dom.appendChild(this.rootE);
		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-2");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		tr.transform(new DOMSource(this.dom), new StreamResult(new FileOutputStream(this.targetFile.getAbsolutePath())));
	}
	
	public void clearSaveSession() {
		this.dom = null;
		this.mode = -1;
		this.rootE = null;
		this.targetFile = null;
		this.instancename = null;
	}
	
	public SynchedDataDescriptor readFromFile(File source, int opt) throws MyAppException, IOException, ParserConfigurationException, SAXException{
		//if(!source.exists()) throw new MyAppException("File does not exist!");
		if(source.isDirectory()) throw new MyAppException("Chosen file was a directory!");
		String fileName = source.getName();
		String appendix;
		switch(opt) {
		case 0: appendix = ".csv"; break;
		case 1: appendix = ".xml"; break;
		default:
			throw new MyAppException("Nem támogatott fájl mentési opció a mentés másként funkcióban.");
		}
		if(fileName.length() > appendix.length()) {
			if(!fileName.substring(fileName.length()-appendix.length(), fileName.length()).equals(appendix)) throw new MyAppException("File does not have xml extension.");
		}
		else throw new MyAppException("File does not have " + appendix + "extension.");
		SynchedDataDescriptor sddesc = null;
		switch(opt) {
		case 0:
			//CSV
			break;
		case 1:
			sddesc = readXml(source);
			break;
		default:
			throw new MyAppException("Unsupported option for read session.");
		}
		return sddesc;
	}
	
	public SynchedDataDescriptor readXml(File source) throws ParserConfigurationException, IOException, SAXException, MyAppException{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom  = db.parse(source);
		NodeList nodeList = dom.getChildNodes();
		if(nodeList.getLength() != 1) throw new MyAppException("Number of root elements in xml is invalid.");
		Element rootE = (Element)nodeList.item(0);
		nodeList = rootE.getChildNodes();
		int fields = 0;
		for(int i = 0; i < nodeList.getLength(); i++) {
			try{
				Element rowE = (Element)nodeList.item(i);
				fields = rowE.getChildNodes().getLength();
			}
			catch(ClassCastException exc) {
				continue;
			}
		}
		System.out.println("Fields: "+fields);
		classnames = new String[fields];
		columnnames = new String[fields];
		for(int i = 0; i < fields; i++) {
			classnames[i] = "";
		}
		String[] textNodes = new String[fields];
		ArrayList<Object[]> values = new ArrayList<Object[]>();
		boolean typesSet = true;
		boolean gotRow = false;
		for(int i = 0; i < nodeList.getLength(); i++) {
			Element rowE;
			try{
				rowE = (Element)nodeList.item(i);
			}
			catch(ClassCastException exc) {
				continue;
			}
			NodeList fieldNodes = rowE.getChildNodes();
			for(int j = 0; j < fields; j++) {
				Element fieldE;
				try{
					fieldE = (Element)fieldNodes.item(j);
				}
				catch(ClassCastException exc) {
					continue;
				}
				if(!gotRow) columnnames[j] = fieldE.getTagName();
				else {
					if(!columnnames[j].equals(fieldE.getTagName())) throw new MyAppException("Name mismatch in xml.");
				}
				Attr attr = fieldE.getAttributeNode("mytype");
				//System.out.println("Got to checking attribute.");
				if(typesSet) {
					if(attr == null) {
						System.out.println("Attribute was null.");
						typesSet = false;
						classnames = null;
					}
					else {
						//System.out.println("Before types[j] == 0 check "+attr.getTextContent());
						if(classnames[j].length() == 0) {
							System.out.println("Attribute text value: "+attr.getNodeValue());
							classnames[j] = attr.getNodeValue();
							System.out.println("classnames[j] value1: "+classnames[j]);
						}
						else {
							if(!classnames[j].equals(attr.getTextContent())) throw new MyAppException("Type mismatch in xml.");
						}
					}
					System.out.println("classnames[j] value2: "+classnames[j]);
				}
				System.out.println("classnames[j] value3: "+classnames[j]);
				textNodes[j] = fieldE.getTextContent();
			}
			System.out.println("classnames[0] value4: "+classnames[0]);
			values.add(textNodes);
			gotRow = true;
		}
		String dataTypeName = rootE.getTagName();
		System.out.println(values.get(0)[0]);
		System.out.println(dataTypeName);
		if(typesSet) {
			if(!checkIfCanonicalNames(classnames)) throw new MyAppException("Type attributes are not all canonical names.");
		}
		SynchedDataDescriptor sddesc;
		if(typesSet) sddesc = new SynchedDataDescriptor(dataTypeName, classnames, columnnames, typesSet, values);
		else sddesc = new SynchedDataDescriptor(dataTypeName, null, columnnames, typesSet, values);
		return sddesc;
	}
	
	public boolean checkIfCanonicalNames(String[] names) {
		for(int i = 0; i < names.length; i++) {
			System.out.println(names[i]);
			if(!(names[i].equals(String.class.getCanonicalName()) ||
					names[i].equals(Integer.class.getCanonicalName()) ||
					names[i].equals(java.util.Date.class.getCanonicalName()) ||
					names[i].equals(java.sql.Date.class.getCanonicalName()) ||
					names[i].equals(java.sql.Timestamp.class.getCanonicalName()))){
				return false;
			}
		}
		return true;
	}
}
