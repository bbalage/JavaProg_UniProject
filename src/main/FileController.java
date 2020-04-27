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

import org.json.JSONException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import models.*;
import utilities.*;

public class FileController {

	private MainView mainView;
	private SavePoll sp;
	private SynchedDataDescriptor sddesc;
	private FilePolicyModel flm = new FilePolicyModel();
	private GeneralChecker gc = new GeneralChecker();
	
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
		catch(JSONException exc) {
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
		this.sddesc = null;
		this.mainView.getTableFieldNames().setModel(new NameTableModel());
		this.mainView.getTableInput().setModel(new DefaultTableModel());
		this.mainView.getTableOutput().setModel(new OutputTableModel());
		this.mainView.switchToHomeCard();
	}
	
	public void saveBySDDesc(){
		try{
			this.flm.save(this.sddesc);
		}
		catch(MyAppException exc) {
			sendMessage("Nem sikerült a mentés művelet: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(ParserConfigurationException exc) {
			sendMessage("Nem sikerült a mentés művelet: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(IOException exc) {
			sendMessage("Nem sikerült a mentés művelet: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(TransformerException exc) {
			sendMessage("Nem sikerült a mentés művelet: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(JSONException exc) {
			sendMessage("Nem sikerült a mentés művelet: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		sendMessage("Sikeres mentés!", JOptionPane.INFORMATION_MESSAGE);
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
				for(int i = 0; i < this.sddesc.getData().size(); i++) {
					flm.appendRow(this.sddesc.getData().get(i));
				}
				flm.finishSave();
			}
			else {
				JTable jt = this.mainView.getTableOutput();
				int trows = jt.getRowCount();
				for(int i = 0; i < trows; i++) {
					flm.appendRow(GeneralController.getRow(jt, i));
				}
				flm.finishSave();
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
		catch(JSONException exc) {
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
	
	private Object[] fetchFromInputTable() {
		JTable it = this.mainView.getTableInput();
		Object[] values = GeneralController.getRow(it, 0);
		boolean empty = true;
		for(Object obj : values) if(obj != null) {empty = false; break;}
		if(empty) {
			sendMessage("There was no input value!", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if(this.sddesc.areTypesSet()) {
			try {
				gc.checkIfConvertable(values, this.sddesc.getTypes());
			}
			catch(MyAppException exc) {
				sendMessage("Could not convert: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		return values;
	}
	
	public void insert() {
		Object[] values = fetchFromInputTable();
		if(values == null) return;
		this.sddesc.getData().add(values);
		((OutputTableModel)this.mainView.getTableOutput().getModel()).addRow(values);
		sendMessage("Insert sikeres! Mentsen, hogy a változás a fájlba kerüljön!", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void update() {
		Object[] values = fetchFromInputTable();
		if(values == null) return;
		JTable ot = this.mainView.getTableOutput();
		int selected;
		try{
			selected = GeneralController.getSelectedIndeces(ot);
		}
		catch(MyAppException exc) {
			sendMessage("Update failed: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.sddesc.getData().set(selected, values);
		OutputTableModel otm = (OutputTableModel)ot.getModel();
		for(int i = 0; i < values.length; i++) {
			otm.setValueAt(values[i], selected, i);
		}
		sendMessage("Update sikeres! Mentsen, hogy a változás a fájlba kerüljön!", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void delete() {
		JTable ot = this.mainView.getTableOutput();
		int selected;
		try{
			selected = GeneralController.getSelectedIndeces(ot);
		}
		catch(MyAppException exc) {
			sendMessage("Update failed: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.sddesc.getData().remove(selected);
		((OutputTableModel)ot.getModel()).removeRow(selected);
		sendMessage("Delete sikeres! Mentsen, hogy a változás a fájlba kerüljön!", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void buildTablesFromSDDesc() {
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
