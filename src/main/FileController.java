package main;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;

import models.*;

public class FileController {

	private MainView mainView;
	private SavePoll sp;
	private SynchedDataDescriptor sddesc;
	private String separator = System.getProperty("file.separator");
	
	public FileController(MainView mainView) {
		super();
		this.mainView = mainView;
	}
	
	public void saveAs(SynchedDataDescriptor sddesc) {
		this.sddesc = sddesc;
		this.sp = new SavePoll(this.mainView, FileController.this);
		this.sp.setVisible(true);
	}
	
	public void saveAsFile(int opt, String targetFileName) {
		if(targetFileName.length() == 0) {
			sendMessage("Nem volt megadva filenév a mentésre!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ret = jfc.showSaveDialog(this.mainView);
		String path = "";
		if(ret == JFileChooser.APPROVE_OPTION) {
			File target = new File(jfc.getSelectedFile()+separator+targetFileName);
			path = target.getAbsolutePath();
		}
		else return;
		String appendix;
		switch(opt) {
		case 0: appendix = ".csv"; break;
		case 1: appendix = ".xml"; break;
		default:
			sendMessage("Nem támogatott fájl mentési opció a mentés másként funkcióban.", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(path.length() >= appendix.length()+1) {
			if(!path.substring(path.length()-appendix.length()-1, path.length()-1).equals(appendix)) path+=appendix;
		}
		sendMessage("Filenév: "+path, JOptionPane.INFORMATION_MESSAGE);
		
		/*switch(opt) {
		case 0:
			
			break;
		case 1:
			
			break;
		
		}*/
	}
	
	public void cancelSaveAs() {
		this.sp.dispose();
		this.sp = null;
	}
	
	public void saveAsCsv() {
		
	}
	
	/*public void saveAsXml(String targetFilename) {
		
		try {
			Document dom;
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			dom = db.newDocument();
			String dataName = this.sddesc.getDataTypeName();
			dataName = dataName != null ? dataName : "Untitled";
			Element rootE = dom.createElement(dataName);
			boolean areTypesSet = sddesc.areTypesSet();
			
		}
		catch(ParserConfigurationException exc) {
			sendMessage("Nem sikerült az XML mentése: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}*/
	
	public void saveXML(String path) {
		
	}
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Fájl kontroll üzenet.", opt);
	}
}
