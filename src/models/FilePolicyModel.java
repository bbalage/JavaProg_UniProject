package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import org.json.*;

import utilities.*;

public class FilePolicyModel {

	private File targetFile;
	private File synchedFile = null;
	private String separator = System.getProperty("file.separator");
	private int saveMode; //0: csv; 1: xml; 2: json; 3: dat
	private int synchMode;
	private Document dom;
	private boolean areTypesSet;
	private Element rootE;
	private JSONObject jRoot;
	private JSONArray jArray;
	private StringBuilder csvFileContent;
	private String dataName;
	private String instancename;
	private String[] classnames;
	private String[] columnnames;
	private GeneralChecker gc = new GeneralChecker();
	
	private void initLocalSaveSessionData(SynchedDataDescriptor sddesc) {
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
	}
	
	private void startSaveSession(SynchedDataDescriptor sddesc) {
		this.targetFile = this.synchedFile;
		this.saveMode = this.synchMode;
		initLocalSaveSessionData(sddesc);
	}
	
	public void startSaveSession(SynchedDataDescriptor sddesc, File targetDir, String targetName, int opt, boolean overWrite) throws MyAppException, ParserConfigurationException, JSONException{
		String appendix;
		switch(opt) {
		case 0: appendix = ".csv"; break;
		case 1: appendix = ".xml"; break;
		case 2: appendix = ".json"; break;
		case 3: appendix = ".dat"; break;
		default:
			throw new MyAppException("Nem támogatott fájl mentési opció a mentés másként funkcióban.");
		}
		if(targetName.length() >= appendix.length()+1) {
			if(!targetName.substring(targetName.length()-appendix.length(), targetName.length()).equals(appendix)) targetName+=appendix;
		}
		else {
			targetName += appendix;
		}
		String path = targetDir.getAbsolutePath()+separator+targetName;
		this.targetFile = new File(path);
		if(!overWrite && this.targetFile.exists()) throw new MyAppException("Ez a fájl már létezik! A felülírás nem engedélyezett a mentés másként funkcióban.");
		this.saveMode = opt;
		initLocalSaveSessionData(sddesc);
		switch(opt) {
		case 0:
			startSaveAsCsv(sddesc);
			break;
		case 1:
			startSaveAsXml(sddesc);
			break;
		case 2:
			startSaveAsJson(sddesc);
			break;
		case 3:
			//No operation necessary. Logic handled in file controller.
			break;
		}
	}
	
	public void save(SynchedDataDescriptor sddesc) throws ParserConfigurationException, IOException, TransformerException, MyAppException, JSONException{
		startSaveSession(sddesc);
		switch(this.saveMode) {
		case 0:
			startSaveAsCsv(sddesc);
			for(Object[] row : sddesc.getData()) appendRowCsv(row);
			finishSaveAsCsv();
			break;
		case 1:
			startSaveAsXml(sddesc);
			for(Object[] row : sddesc.getData()) appendRowXml(row);
			finishSaveAsXml();
			break;
		case 2:
			startSaveAsJson(sddesc);
			for(int i = 0; i < sddesc.getData().size(); i++) {
				if(sddesc.getData().get(i) == null) System.out.println("Row is null at: " +i);
				appendRowJson(sddesc.getData().get(i));
			}
			finishSaveAsJson();
			break;
		case 3:
			saveToDat(sddesc);
			break;
		default:
			clearSaveSession();
			throw new MyAppException("Save mode not supported. Revise program!");
		}
		clearSaveSession();
	}
	
	public void saveToDat(SynchedDataDescriptor sddesc) throws IOException{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.targetFile));
		out.writeObject(sddesc);
		out.close();
	}
	
	private void startSaveAsCsv(SynchedDataDescriptor sddesc) {
		this.csvFileContent = new StringBuilder();
		if(this.areTypesSet) {
			this.csvFileContent.append("mytypes\n");
			for(String cls : this.classnames) this.csvFileContent.append(cls+";");
			this.csvFileContent.setCharAt(this.csvFileContent.length()-1, '\n');
		}
		for(String cls : this.columnnames) this.csvFileContent.append(cls+";");
		this.csvFileContent.setCharAt(this.csvFileContent.length()-1, '\n');
	}
	
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
			appendRowCsv(row);
			break;
		case 1:
			appendRowXml(row);
			break;
		case 2:
			appendRowJson(row);
			break;
		}
	}
	
	private void appendRowCsv(Object[] row) {
		for(int i = 0; i < row.length; i++) {
			String toAppend;
			if(row[i] != null) toAppend = row[i].toString();
			else toAppend = "";
			this.csvFileContent.append(toAppend+";");
		}
		this.csvFileContent.setCharAt(this.csvFileContent.length()-1, '\n');
	}
	
	private void appendRowJson(Object[] row) throws JSONException{
		JSONArray jAr = new JSONArray();
		for(int i = 0; i < row.length; i++) {
			JSONObject jObj = new JSONObject();
			Object input = row[i] == null ? "" : row[i];
			jObj.put(this.columnnames[i], input);
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
	
	private void finishSaveAsCsv() throws IOException{
		PrintStream out = new PrintStream(new FileOutputStream(this.targetFile));
		out.print(this.csvFileContent.substring(0, this.csvFileContent.length()-1).toString());
		this.csvFileContent = null;
		out.close();
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
		PrintStream toJsonFile = new PrintStream(new FileOutputStream(this.targetFile));
		toJsonFile.println(jRoot.toString(2));
		toJsonFile.close();
	}
	
	
	public void finishSave() throws ParserConfigurationException, IOException, TransformerException, MyAppException, JSONException{
		switch(this.saveMode) {
		case 0:
			finishSaveAsCsv();
			break;
		case 1:
			finishSaveAsXml();
			break;
		case 2:
			finishSaveAsJson();
			break;
		default:
			clearSaveSession();
			throw new MyAppException("Unsupported save option. Revise program!");
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
	
	public SynchedDataDescriptor readFromFile(File source, int opt) throws MyAppException, IOException, ParserConfigurationException, SAXException, JSONException, ClassNotFoundException{
		if(source.isDirectory()) throw new MyAppException("Választott fájl jegyzék volt!");
		String fileName = source.getName();
		String appendix;
		switch(opt) {
		case 0: appendix = ".csv"; break;
		case 1: appendix = ".xml"; break;
		case 2: appendix = ".json"; break;
		case 3: appendix = ".dat"; break;
		default:
			throw new MyAppException("Nem támogatott fájl mentési opció a mentés másként funkcióban.");
		}
		if(fileName.length() > appendix.length()) {
			if(!fileName.substring(fileName.length()-appendix.length(), fileName.length()).equals(appendix)) throw new MyAppException("A fájl kiterjesztése nem "+appendix);
		}
		else throw new MyAppException("Fájl kiterjesztése nem " + appendix);
		SynchedDataDescriptor sddesc = null;
		switch(opt) {
		case 0:	sddesc = readCsv(source); break;
		case 1:	sddesc = readXml(source); break;
		case 2:	sddesc = readJson(source); break;
		case 3:	sddesc = readDat(source); break;
		default:
			throw new MyAppException("Unsupported option for read session. Revise program!");
		}
		this.synchMode = opt;
		this.synchedFile = source;
		return sddesc;
	}
	
	private SynchedDataDescriptor readDat(File source) throws IOException, MyAppException, ClassNotFoundException{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(source));
		SynchedDataDescriptor sddesc = null;
		try {
			sddesc = (SynchedDataDescriptor) in.readObject();
		}
		catch(ClassCastException exc) {
			in.close();
			throw new MyAppException("Retrieved class was not SynchedDataDescriptor: "+exc.getMessage());
		}
		in.close();
		return sddesc;
	}
	
	private SynchedDataDescriptor readCsv(File source) throws IOException, MyAppException{
		LineNumberReader in = new LineNumberReader(new InputStreamReader(new FileInputStream(source)));
		String line = in.readLine();
		if(line == null) {
			in.close();
			throw new MyAppException("Üres fájl!");
		}
		String[] types = null;
		boolean typesSet = false;
		if(line.equals("mytypes")) {
			line = in.readLine();
			if(line == null) {
				in.close();
				throw new MyAppException("Hiányos fájl!");
			}
			types = line.split(";");
			typesSet = true;
			if(!gc.checkIfCanonicalNames(types)) {
				in.close();
				throw new MyAppException("Fájlban lévő típusok nem a kezelhető kanonikus nevek!");
			}
		}
		line = in.readLine();
		if(line == null) {
			in.close();
			throw new MyAppException("Hiányos fájl!");
		}
		String[] names = line.split(";");
		ArrayList<Object[]> values = new ArrayList<Object[]>();
		while((line = in.readLine())!= null) {
			Object[] temp = line.split(";");
			for(int i = 0; i < temp.length; i++) {
				temp[i] = temp[i].toString().length() == 0 ? null : temp[i];
			}
			values.add(temp);
		}
		in.close();
		SynchedDataDescriptor sddesc = null;
		if(typesSet) sddesc = new SynchedDataDescriptor(dataName, types, names, typesSet, values);
		else sddesc = new SynchedDataDescriptor(dataName, null, names, typesSet, values);
		return sddesc;
	}
	
	private SynchedDataDescriptor readJson(File source) throws IOException, JSONException, MyAppException{
		StringBuilder jsonData = new StringBuilder();
		LineNumberReader in = new LineNumberReader(new InputStreamReader(new FileInputStream(source)));
		String inline;
		while((inline = in.readLine()) != null)
			jsonData.append(inline);
		in.close();
		JSONObject jRoot = new JSONObject(jsonData.toString());
		JSONArray jRootName= jRoot.names();
		if(jRootName.length() != 1) throw new MyAppException("Fájlstruktúra nem táblastruktúrát mutat!");
		JSONArray jRootArray = jRoot.getJSONArray(jRootName.getString(0));
		String dataName = jRootName.getString(0);
		String[] types = getTypesFromJson(jRootArray);
		boolean typesSet;
		if(types != null) typesSet = true;
		else typesSet = false;
		if(typesSet) {
			if(!gc.checkIfCanonicalNames(types)) throw new MyAppException("Fájlban lévő típusok nem a kezelhető kanonikus nevek!");
		}
		String[] names = getNamesFromJson(jRootArray, typesSet);
		ArrayList<Object[]> values = new ArrayList<Object[]>();
		for(int i = typesSet ? 1 : 0; i < jRootArray.length(); i++) {
			values.add(getValuesFromJsonAt(jRootArray, names, i));
		}
		SynchedDataDescriptor sddesc = null;
		if(typesSet) sddesc = new SynchedDataDescriptor(dataName, types, names, typesSet, values);
		else sddesc = new SynchedDataDescriptor(dataName, null, names, typesSet, values);
		return sddesc;
	}
	
	private SynchedDataDescriptor readXml(File source) throws ParserConfigurationException, IOException, SAXException, MyAppException{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom  = db.parse(source);
		NodeList nodeList = dom.getChildNodes();
		if(nodeList.getLength() != 1) throw new MyAppException("Helytelen mennyiségű gyökér elem az xml fájlban!");
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
			if(!gc.checkIfCanonicalNames(types)) throw new MyAppException("Fájlban lévő típusok nem a kezelhető kanonikus nevek!");
		}
		String dataTypeName = rootE.getTagName();
		SynchedDataDescriptor sddesc = null;
		if(typesSet) sddesc = new SynchedDataDescriptor(dataTypeName, types, names, typesSet, values);
		else sddesc = new SynchedDataDescriptor(dataTypeName, null, names, typesSet, values);
		return sddesc;
	}
	
	private String[] getValuesFromJsonAt(JSONArray jRootArray, String[] names, int index) throws MyAppException{
		ArrayList<String> values = new ArrayList<String>();
		try {
			JSONObject jRowObj = jRootArray.getJSONObject(index);
			JSONArray jValNames = jRowObj.names();
			if(jValNames.length() != 1) throw new MyAppException("Inkompatibilis fájlstruktúra detektálva json fájl olvasásakor!");
			JSONArray jFields = jRowObj.getJSONArray(jValNames.getString(0));
			for(int i = 0; i < jFields.length(); i++) {
				JSONObject jField = jFields.getJSONObject(i);
				values.add(jField.getString(names[i]));
			}
		}
		catch(JSONException exc) {
			throw new MyAppException("Nem sikerült visszakapni az értékeket: "+exc.getMessage());
		}
		return values.toArray(new String[0]);
	}
	
	private String[] getNamesFromJson(JSONArray jRootArray, boolean typesSet) throws MyAppException {
		ArrayList<String> names = new ArrayList<String>();
		int index = typesSet ? 1 : 0;
		try {
			JSONObject jValObj = jRootArray.getJSONObject(index);
			JSONArray jValNames = jValObj.names();
			if(jValNames.length() != 1) throw new MyAppException("Inkompatibilis fájlstruktúra detektálva json fájl olvasásakor!");
			JSONArray jVals = jValObj.getJSONArray(jValNames.getString(0));
			for(int i = 0; i < jVals.length(); i++) {
				JSONObject jObj = jVals.getJSONObject(i);
				JSONArray jArr = jObj.names();
				if(jArr.length() != 1) throw new MyAppException("Inkompatibilis fájlstruktúra detektálva json fájl olvasásakor!");
				String name = jArr.getString(0);
				if(name.length() == 0) throw new MyAppException("Oszlopnév hossza nulla!");
				names.add(name);
			}
		}
		catch(JSONException exc) {
			throw new MyAppException("Nem sikerült a neveket visszakapni: "+exc.getMessage());
		}
		return names.toArray(new String[0]);
	}
	
	private String[] getTypesFromJson(JSONArray jRootArray) {
		ArrayList<String> types = new ArrayList<String>();
		try {
			JSONObject jTypeObj = jRootArray.getJSONObject(0);
			JSONArray jTypes = jTypeObj.getJSONArray("mytypes");
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
		if(values.size()!=fields) throw new MyAppException("Fájltartalom nem tábla struktúrájú!");
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
