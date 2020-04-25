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
	
	public void startReadSession(File source, int opt) throws MyAppException, IOException, ParserConfigurationException, SAXException{
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
			if(fileName.substring(fileName.length()-appendix.length(), fileName.length()).equals(appendix)) throw new MyAppException("File does not have xml extension.");
		}
		else throw new MyAppException("File does not have xml extension.");
		switch(opt) {
		case 0:
			//CSV
			break;
		case 1:
			readXml(source);
			break;
		default:
			throw new MyAppException("Unsupported option for read session.");
		}
	}
	
	public void readXml(File source) throws ParserConfigurationException, IOException, SAXException, MyAppException{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom  = db.parse(source);
		NodeList nodeList = dom.getChildNodes();
		if(nodeList.getLength() != 1) throw new MyAppException("Number of root elements in xml is invalid.");
		Element rootE = (Element)nodeList.item(0);
		nodeList = rootE.getChildNodes();
		String[] types = null;
		String[] names = null;
		ArrayList<String[]> values = new ArrayList<String[]>();
		boolean typesSet = true;
		for(int i = 0; i < nodeList.getLength(); i++) {
			Element rowE = (Element)nodeList.item(i);
			NodeList fieldNodes = rowE.getChildNodes();
			int fields = fieldNodes.getLength();
			if(i == 0) types = new String[fields];
			if(i == 0) names = new String[fields];
			String[] textNodes = new String[fields];
			for(int j = 0; j < fields; j++) {
				Element fieldE = (Element)fieldNodes.item(j);
				if(i == 0) names[j] = fieldE.getTagName();
				else {
					if(!names[j].equals(fieldE.getTagName())) throw new MyAppException("Name mismatch in xml.");
				}
				Attr attr = fieldE.getAttributeNode("mytype");
				if(typesSet) {
					if(attr == null) {
						typesSet = false;
						types = null;
					}
					else {
						if(types[j] == null) types[j] = attr.getTextContent();
						else {
							if(!types[j].equals(attr.getTextContent())) throw new MyAppException("Type mismatch in xml.");
						}
					}
				}
				textNodes[j] = fieldE.getTextContent();
			}
			values.add(textNodes);
		}
		String dataTypeName = rootE.getTagName();
		ArrayList<String> datanames = new ArrayList<String>();
		for(String s : names) datanames.add(s);
		ArrayList<Class<?>> cls = new ArrayList<Class<?>>();
		//if()
		//SynchedDataDescriptor sddesc = new SynchedDataDescriptor(dataTypeName, types, datanames, typesSet, values);
	}
}
