package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import org.json.*;
import com.google.gson.*;

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
	private JSONObject jRoot;
	private JSONArray jArray;
	private String dataName;
	private String instancename;
	private String[] classnames;
	private String[] columnnames;
	private GeneralChecker gc = new GeneralChecker();
	
	private void startSaveSession() {
		this.targetFile = this.synchedFile;
		this.saveMode = this.synchMode;
	}
	
	public void startSaveSession(SynchedDataDescriptor sddesc, File targetDir, String targetName, int opt, boolean overWrite) throws MyAppException, ParserConfigurationException, JSONException{
		String appendix;
		switch(opt) {
		case 0: appendix = ".csv"; break;
		case 1: appendix = ".xml"; break;
		case 2: appendix = ".json"; break;
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
		this.dataName = sddesc.getDataTypeName();
		dataName = dataName != null ? dataName : "Untitled";
		this.instancename = dataName+"instance";
		this.areTypesSet = sddesc.areTypesSet();
		if(this.areTypesSet) {
			this.classnames = sddesc.getTypes(); 
		}
		else {
			this.classnames = null;
		}
		this.columnnames = sddesc.getNames();
		switch(opt) {
		case 0:
			//CSV
			break;
		case 1:
			startSaveAsXml(sddesc);
			break;
		case 2:
			startSaveAsJson(sddesc);
			break;
		}
	}
	
	public void save(SynchedDataDescriptor sddesc) throws ParserConfigurationException, IOException, TransformerException, MyAppException, JSONException{
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
	
	/*public void saveAsCsv() {
		
	}*/
	
	private void startSaveAsJson(SynchedDataDescriptor sddesc) throws JSONException{
		this.jRoot = new JSONObject();
		this.jArray = new JSONArray();
		if(this.areTypesSet) {
			JSONArray jArray = new JSONArray();
			for(int i = 0; i < this.classnames.length; i++) {
				JSONObject jObj = new JSONObject();
				jObj.put("mytype", this.classnames[i]);
				jArray.put(i, jObj);
			}
			JSONObject jTypes = new JSONObject();
			jTypes.put("mytypes", jArray);
			this.jArray.put(jTypes);
		}
	}
	
	private void startSaveAsXml(SynchedDataDescriptor sddesc) throws ParserConfigurationException{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		this.dom = db.newDocument();
		this.rootE = dom.createElement(dataName);
	}
	
	public void appendRow(Object[] row) throws JSONException{
		switch(this.saveMode) {
		case 0:
			//CSV
			break;
		case 1:
			appendRowXml(row);
			break;
		case 2:
			appendRowJson(row);
			break;
		}
	}
	
	private void appendRowJson(Object[] row) throws JSONException{
		JSONArray jAr = new JSONArray();
		for(int i = 0; i < row.length; i++) {
			JSONObject jObj = new JSONObject();
			jObj.put(this.columnnames[i], row[i]);
			jAr.put(i, jObj);
		}
		JSONObject jInstance = new JSONObject();
		jInstance.put(this.instancename, jAr);
		this.jArray.put(jInstance);
	}
	
	private void appendRowXml(Object[] row) {
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
	
	private void finishSaveAsJson() throws JSONException, IOException{
		this.jRoot.put(this.dataName, this.jArray);
		//Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//org.json.simple.JSONObject simpJson = 
		//String json = gson.toJson(this.jRoot.toJSONString());
		PrintStream toJsonFile = new PrintStream(new FileOutputStream(this.targetFile));
		//String[] jsonSplit = json.split("\n");
		//for(String out : jsonSplit)
		toJsonFile.println(jRoot.toString(2));
		toJsonFile.close();
	}
	
	
	public void finishSave() throws ParserConfigurationException, IOException, TransformerException, MyAppException, JSONException{
		switch(this.saveMode) {
		case 0:
			
			break;
		case 1:
			finishSaveAsXml();
			break;
		case 2:
			finishSaveAsJson();
			break;
		default:
			clearSaveSession();
			throw new MyAppException("Unsupported save option.");
		}
		clearSaveSession();
	}
	
	public void clearSaveSession() {
		this.dom = null;
		this.saveMode = -1;
		this.rootE = null;
		this.targetFile = null;
		this.instancename = null;
	}
	
	public SynchedDataDescriptor readFromFile(File source, int opt) throws MyAppException, IOException, ParserConfigurationException, SAXException, JSONException{
		if(source.isDirectory()) throw new MyAppException("Chosen file was a directory!");
		String fileName = source.getName();
		String appendix;
		switch(opt) {
		case 0: appendix = ".csv"; break;
		case 1: appendix = ".xml"; break;
		case 2: appendix = ".json"; break;
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
		case 2:
			sddesc = readJson(source);
			break;
		default:
			throw new MyAppException("Unsupported option for read session.");
		}
		this.synchMode = opt;
		this.synchedFile = source;
		return sddesc;
	}
	
	//Read Json.
	private SynchedDataDescriptor readJson(File source) throws IOException, JSONException{
		StringBuilder jsonData = new StringBuilder();
		LineNumberReader in = new LineNumberReader(new InputStreamReader(new FileInputStream(source)));
		String inline;
		while((inline = in.readLine()) != null)
			jsonData.append(inline);
		in.close();
		JSONObject jRoot = new JSONObject(jsonData.toString());
		JSONArray jRootData = jRoot.getJSONArray("TARSASHAZ");
		//String[] types = getTypesFromJson(jRootData);
		//if(types != null) for(String ts : types) System.out.println(ts);
		//else System.out.println("Types was null.");
		return null;
	}
	
	private SynchedDataDescriptor readXml(File source) throws ParserConfigurationException, IOException, SAXException, MyAppException{
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
				break;
			}
		}
		String[] names = getFieldNamesFromXml(fieldNodes);
		String[] types = getFieldTypesFromXml(fieldNodes);
		boolean typesSet = types != null;
		if(typesSet) {
			for(int i = 0; i < types.length; i++) if(types[i] == null) typesSet = false;
		}
		int fields = names.length;
		ArrayList<Object[]> values = new ArrayList<Object[]>();
		for(int i = 0; i < rowNodes.getLength(); i++) {
			fieldNodes = getFieldNodesFromXml(rowNodes, i);
			if(fieldNodes != null) {
				String[] tempvals = getValuesFromXml(fieldNodes,fields);
				if(tempvals != null) values.add(tempvals);
			}
		}
		if(typesSet) {
			if(!gc.checkIfCanonicalNames(types)) throw new MyAppException("Type attributes are not all canonical names.");
		}
		String dataTypeName = rootE.getTagName();
		SynchedDataDescriptor sddesc = null;
		if(typesSet) sddesc = new SynchedDataDescriptor(dataTypeName, types, names, typesSet, values);
		else sddesc = new SynchedDataDescriptor(dataTypeName, null, names, typesSet, values);
		return sddesc;
	}
	
	private String[] getTypesFromJson(JSONObject jRoot) {
		ArrayList<String> types = new ArrayList<String>();
		try {
			JSONArray jTypes = jRoot.getJSONArray("mytypes");
			System.out.println("Got mytypes.");
			for(int i = 0; i < jTypes.length(); i++) {
				JSONObject jObj = jTypes.getJSONObject(i);
				String temptype = jObj.getString("mytype");
				if(temptype.length() == 0) return null;
				types.add(temptype);
			}
		}
		catch(JSONException exc) {
			return null;
		}
		return types.toArray(new String[0]);
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
				String type = fieldE.getAttribute("mytype");
				type = type.length()==0 ? null : type;
				types.add(type);
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
			return rowE.getChildNodes();
		}
		catch(ClassCastException exc) {
			return null;
		}
	}
}
