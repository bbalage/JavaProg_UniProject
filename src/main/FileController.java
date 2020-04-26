package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

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
	
	public SynchedDataDescriptor loadFile(File source, int opt) {
		SynchedDataDescriptor sddesc = null;
		try {
			sddesc = flm.readFromFile(source, opt);
			
		}
		catch(MyAppException exc) {
			sendMessage("Sikertelen olvasás: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		catch(IOException exc) {
			sendMessage("Sikertelen olvasás: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (ParserConfigurationException exc) {
			sendMessage("Sikertelen olvasás: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (SAXException exc) {
			sendMessage("Sikertelen olvasás: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		this.sddesc = sddesc;
		return sddesc;
	}
	
	public void setupFileInterface() {
		this.mainView.dbPanel(false);
		this.mainView.getBtnSave().setVisible(true);
		buildTablesFromSDDesc();
	}
	
	public void endFileSession(){
		this.flm.clearSaveSession(); //Some more specific cleaning code might be necessary.
		this.mainView.getTableFieldNames().setModel(new NameTableModel());
		this.mainView.getTableInput().setModel(new DefaultTableModel());
		this.mainView.getTableOutput().setModel(new OutputTableModel());
		this.mainView.switchToHomeCard();
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
		boolean ok = true;
		try {
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int ret = jfc.showSaveDialog(this.mainView);
			File targetDir;
			if(ret == JFileChooser.APPROVE_OPTION) {
				targetDir = new File(jfc.getSelectedFile().getAbsolutePath());
			}
			else return;
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
	
	public void buildTablesFromSDDesc() {
		//ArrayList<Object> objs = new ArrayList<Object>();
		Object[] names = this.sddesc.getNames();
		String[] types = this.sddesc.getTypes();
		JTable it = this.mainView.getTableInput();
		it.setModel(new InputTableModel(names));
		OutputTableModel otm = new OutputTableModel(names);
		JTable ot = this.mainView.getTableOutput();
		ot.setModel(otm);
		for(Object[] values : this.sddesc.getData()) {
			otm.addRow(values);
		}
		GeneralController.setTablePreferredWidths(ot, types);
		GeneralController.setTablePreferredWidths(it, types);
	}
	
	public void cancelSaveAs() {
		this.sp.dispose();
	}
	
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Fájl kontroll üzenet.", opt);
	}
}
