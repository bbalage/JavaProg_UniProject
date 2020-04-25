package models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

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
		String path = targetDir.getAbsolutePath()+separator+targetName;
		String appendix;
		switch(opt) {
		case 0: appendix = ".csv"; break;
		case 1: appendix = ".xml"; break;
		default:
			throw new MyAppException("Nem támogatott fájl mentési opció a mentés másként funkcióban.");
		}
		if(path.length() >= appendix.length()+1) {
			if(!path.substring(path.length()-appendix.length(), path.length()).equals(appendix)) path+=appendix;
		}
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
			Class<?>[] cls = sddesc.getTypes().toArray(new Class<?>[0]);
			this.classnames = new String[cls.length];
			for(int i = 0; i < cls.length; i++) {
				this.classnames[i] = cls[i].getCanonicalName();
			}
			this.columnnames = sddesc.getNames().toArray(new String[0]);
		}
	}
	
	public void appendRow(Object[] row) {
		Element rowE = dom.createElement(this.instancename);
		for(int i = 0; i < row.length; i++) {
			Element fieldE = dom.createElement(this.columnnames[i]);
			if(this.areTypesSet) {
				fieldE.setAttribute("type", this.classnames[i]);
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
}
