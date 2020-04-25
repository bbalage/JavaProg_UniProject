package models;

import java.io.File;

import javax.swing.JOptionPane;
import javax.xml.parsers.*;

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
	
	public void startSaveSession(SynchedDataDescriptor sddesc, File targetDir, String targetName, int opt) throws MyAppException, ParserConfigurationException{
		String path = targetDir.getAbsolutePath()+separator+targetName;
		String appendix;
		switch(opt) {
		case 0: appendix = ".csv"; break;
		case 1: appendix = ".xml"; break;
		default:
			throw new MyAppException("Nem támogatott fájl mentési opció a mentés másként funkcióban.");
		}
		if(path.length() >= appendix.length()+1) {
			if(!path.substring(path.length()-appendix.length()-1, path.length()-1).equals(appendix)) path+=appendix;
		}
		this.targetFile = new File(path);
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
		}
	}
	
	public void appendRow(Object[] row) {
		
	}
	
	public void abortSaveSession() {
		this.dom = null;
		this.mode = -1;
		this.rootE = null;
		this.targetFile = null;
		this.instancename = null;
	}
}
