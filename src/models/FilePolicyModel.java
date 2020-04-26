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
	private File synchedFile = null;
	private String separator = System.getProperty("file.separator");
	private int saveMode; //0: csv; 1: xml
	private int synchMode;
	private Document dom;
	private boolean areTypesSet;
	private Element rootE;
	private String instancename;
	private String[] classnames;
	private String[] columnnames;
	private GeneralChecker gc = new GeneralChecker();
	
	private void startSaveSession() {
		this.targetFile = this.synchedFile;
		this.saveMode = this.synchMode;
	}
	
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
		this.saveMode = opt;
		switch(opt) {
		case 0:
			//CSV
			break;
		case 1:
			startSaveAsXml(sddesc);
			break;
		}
	}
	
	public void save(SynchedDataDescriptor sddesc) throws ParserConfigurationException, IOException, TransformerException, MyAppException{
		startSaveSession();
		switch(this.saveMode) {
		case 1:
			startSaveAsXml(sddesc);
			for(Object[] row : sddesc.getData()) appendRow(row);
			finishSaveAsXml();
			break;
		default:
			throw new MyAppException("Save mode not supported.");
		}
	}
	
	public void saveAsCsv() {
		
	}
	
	private void startSaveAsXml(SynchedDataDescriptor sddesc) throws ParserConfigurationException{
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
	
	private void finishSaveAsXml() throws ParserConfigurationException, IOException, TransformerException{
		dom.appendChild(this.rootE);
		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-2");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		tr.transform(new DOMSource(this.dom), new StreamResult(new FileOutputStream(this.targetFile.getAbsolutePath())));
	}
	
	public void finishSave() throws ParserConfigurationException, IOException, TransformerException, MyAppException{
		switch(this.saveMode) {
		case 0:
			
			break;
		case 1:
			finishSaveAsXml();
			break;
		default:
			clearSaveSession();
			throw new MyAppException("Unsupported save option.");
		}
	}
	
	public void clearSaveSession() {
		this.dom = null;
		this.saveMode = -1;
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
		this.synchMode = opt;
		this.synchedFile = source;
		return sddesc;
	}
	
	public SynchedDataDescriptor readXml(File source) throws ParserConfigurationException, IOException, SAXException, MyAppException{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom  = db.parse(source);
		NodeList nodeList = dom.getChildNodes();
		if(nodeList.getLength() != 1) throw new MyAppException("Number of root elements in xml is invalid.");
		Element rootE = (Element)nodeList.item(0);
		NodeList rowNodes = rootE.getChildNodes();
		NodeList fieldNodes = null;
		for(int i = 0; i < rowNodes.getLength(); i++) {
			fieldNodes = getFieldNodesFromXml(rowNodes, i);
			if(fieldNodes != null) {
				System.out.println("Was not null: "+fieldNodes.toString());
				break;
			}
		}
		System.out.println(fieldNodes.toString());
		String[] names = getFieldNamesFromXml(fieldNodes);
		//for(String nm : names) System.out.println("Name: "+nm);
		String[] types = getFieldTypesFromXml(fieldNodes);
		boolean typesSet = types != null;
		int fields = names.length;
		//for(String ts : types) System.out.println("Types: "+ts);
		ArrayList<Object[]> values = new ArrayList<Object[]>();
		for(int i = 0; i < rowNodes.getLength(); i++) {
			fieldNodes = getFieldNodesFromXml(rowNodes, i);
			if(fieldNodes != null) {
				System.out.println("Was not null: "+fieldNodes.toString());
				String[] tempvals = getValuesFromXml(fieldNodes,fields);
				if(tempvals != null) values.add(tempvals);
			}
		}
		/*for(String[] row : values) {
			for(String field : row) {
				System.out.print(field);
				System.out.print(" ");
			}
			System.out.println();
		}*/
		if(typesSet) {
			if(!gc.checkIfCanonicalNames(types)) throw new MyAppException("Type attributes are not all canonical names.");
		}
		String dataTypeName = rootE.getTagName();
		SynchedDataDescriptor sddesc = null;
		if(typesSet) sddesc = new SynchedDataDescriptor(dataTypeName, types, names, typesSet, values);
		else sddesc = new SynchedDataDescriptor(dataTypeName, null, names, typesSet, values);
		return sddesc;
	}
	
	private String[] getValuesFromXml(NodeList fieldNodes, int fields) throws MyAppException{
		Element fieldE;
		ArrayList<String> values = new ArrayList<String>();
		for(int i = 0; i < fieldNodes.getLength(); i++) {
			try {
				fieldE = (Element)fieldNodes.item(i);
				values.add(fieldE.getTextContent());
			}
			catch(ClassCastException exc) {
				continue;
			}
		}
		if(values.size()!=fields) throw new MyAppException("File content does not match table content structure.");
		return values.toArray(new String[0]);
	}
	
	private String[] getFieldTypesFromXml(NodeList fieldNodes) {
		Element fieldE;
		ArrayList<String> types = new ArrayList<String>();
		for(int i = 0; i < fieldNodes.getLength(); i++) {
			try {
				fieldE = (Element)fieldNodes.item(i);
				types.add(fieldE.getAttribute("mytype"));
			}
			catch(ClassCastException exc) {
				continue;
			}
		}
		if(types.size()==0) return null;
		String[] ret =  types.toArray(new String[0]);
		for(String ts : ret) if(ts == null) return null;
		return ret;
	}
	
	private String[] getFieldNamesFromXml(NodeList fieldNodes) {
		Element fieldE;
		ArrayList<String> names = new ArrayList<String>();
		for(int i = 0; i < fieldNodes.getLength(); i++) {
			try {
				fieldE = (Element)fieldNodes.item(i);
				names.add(fieldE.getTagName());
			}
			catch(ClassCastException exc) {
				continue;
			}
		}
		return names.toArray(new String[0]);
	}
	
	private NodeList getFieldNodesFromXml(NodeList rowNodes, int index) {
		try{
			Element rowE = (Element)rowNodes.item(index);
			if(rowE == null) System.out.println("Was null inside function getFieldNodesFromXml");
			System.out.println("Was NOT null inside function getFieldNodesFromXml");
			return rowE.getChildNodes();
		}
		catch(ClassCastException exc) {
			return null;
		}
	}
}
