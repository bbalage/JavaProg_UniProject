package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;

import models.*;
import utilities.*;

public class FileController {

	private MainView mainView;
	private SavePoll sp;
	private SynchedDataDescriptor sddesc;
	private FilePolicyModel flm = new FilePolicyModel();
	
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
		File targetDir;
		if(ret == JFileChooser.APPROVE_OPTION) {
			targetDir = new File(jfc.getSelectedFile().getAbsolutePath());
		}
		else return;
		boolean ok = true;
		try {
			flm.startSaveSession(this.sddesc, targetDir, targetFileName, opt, false);
			boolean dataInMemory = this.sddesc.getData() != null;
			if(dataInMemory) {
				System.out.println("Data was in memory.");
				flm.clearSaveSession();
			}
			else {
				JTable jt = this.mainView.getTableOutput();
				int trows = jt.getRowCount();
				for(int i = 0; i < trows; i++) {
					flm.appendRow(SynchController.getRow(jt, i));
				}
				flm.finishSaveAsXml();
			}
		}
		catch(MyAppException exc) {
			sendMessage("Nem sikerült a mentés másként művelet: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			ok = false;
		}
		catch(ParserConfigurationException exc) {
			sendMessage("Nem sikerült a mentés másként művelet: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			ok = false;
		}
		catch(IOException exc) {
			sendMessage("Nem sikerült a mentés másként művelet: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			ok = false;
		}
		catch(TransformerException exc) {
			sendMessage("Nem sikerült a mentés másként művelet: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			ok = false;
		}
		if(!ok) {
			flm.clearSaveSession();
		}
		else {
			sendMessage("Sikeres mentés!", JOptionPane.INFORMATION_MESSAGE);
			this.sp.dispose();
		}
	}
	
	public void cancelSaveAs() {
		this.sp.dispose();
	}
	
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Fájl kontroll üzenet.", opt);
	}
}
