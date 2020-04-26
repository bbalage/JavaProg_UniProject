package main;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import models.*;
import oracle.net.aso.f;
import utilities.*;

public class SynchController {
	
	private SynchOption sOpt = SynchOption.NONE;
	private SynchPoll sPoll;
	private LoginView lView;
	private MainView mainView;
	private DatabaseController dbController;
	private FileController fController;
	private SynchedDataDescriptor sddesc;
	
	SynchController(DatabaseController dbc, FileController fc){
		this.dbController = dbc;
		this.fController = fc;
	}
	
	public void synchWithType(SynchOption sOpt) {
		switch(sOpt) {
		case ORACLE:
			synchWithOracle();
			break;
		case SQLITE:
			synchWithSQLite();
			break;
		case XML:
			synchWithXml();
			break;
		case NONE:
			sendMessage("Miscarried functioning! Synchoption was NONE! Revise program! syncWithType", JOptionPane.ERROR_MESSAGE);
			break;
		}
		}
	
	public void startSynchSession(JFrame owner) {
		this.mainView = (MainView) owner;
		this.sPoll = new SynchPoll(owner, SynchController.this);
		this.sPoll.setVisible(true);
		
	}
	
	public void cancelSynchSession() {
		this.sPoll.dispose();
	}
	
	public void cancelSynchWithOracle() {
		this.lView.dispose();
	}
	
	private void synchWithXml() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Válassza ki az xml fájlt, amivel szinkronizálni szeretne!");
		int ret = jfc.showOpenDialog(this.sPoll);
		if(ret == JFileChooser.APPROVE_OPTION) {
			File xmlFile = jfc.getSelectedFile();
			this.sddesc = this.fController.loadFile(xmlFile, 1);
		}
		if(this.sddesc != null) {
			sendMessage("Sikeres kapcsolódás az xml fájllal.", JOptionPane.INFORMATION_MESSAGE);
			this.sOpt = SynchOption.XML;
			this.sPoll.dispose();
			loadSynchedSession();
		}
	}
	
	private void synchWithSQLite() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Válassza ki a lokális SQLite adatbázist, amit kezelni szeretne!");
		int ret = jfc.showOpenDialog(this.sPoll);
		boolean ok = false;
		if(ret == JFileChooser.APPROVE_OPTION) {
			File db = jfc.getSelectedFile();
			ok = dbController.connectToSQLite(db);
		}
		if(ok) {
			sendMessage("Sikeres kapcsolódás az SQLite adatbázissal.", JOptionPane.INFORMATION_MESSAGE);
			this.sOpt = SynchOption.SQLITE;
			this.sPoll.dispose();
			loadSynchedSession();
		}
	}
	
	private void synchWithOracle() {
		this.lView = new LoginView(sPoll, SynchController.this);
		lView.setVisible(true);
	}
	
	//Where is sPoll disposed of?
	public void loginToOracle() {
		String username = lView.getTextUsername().getText();
		String password = charsToString(lView.getPasswordField().getPassword());
		String workspace = lView.getCheckBoxWorkspace().isSelected() ? username : lView.getTextWorkspace().getText();
		boolean ok;
		if(lView.getCheckBoxDefaultServer().isSelected()) {
			ok = dbController.connectToOracle(username, password, workspace);
		}
		else {
			String URL = lView.getTextURL().getText();
			ok = dbController.connectToOracle(username, password, workspace, URL);
		}
		if(ok) {
			sendMessage("Sikeres bejelentkezés az Oracle adatbázisra.", JOptionPane.INFORMATION_MESSAGE);
			this.sOpt = SynchOption.ORACLE;
			this.lView.dispose();
			loadSynchedSession();
		}
	}
	
	public void dataModifyingAction(int type) {
		if(this.mainView.getTableInput().getColumnCount() <= 0 || this.mainView.getTableOutput().getColumnCount() <= 0) {
			sendMessage("No data structure present to execute the action on.", JOptionPane.ERROR_MESSAGE);
			return;
		}
		switch(this.sOpt) {
		case ORACLE:
		case SQLITE:
			switch(type) {
			case 0:	this.dbController.insert(); break;
			case 1:	this.dbController.update(); break;
			case 2: this.dbController.delete(); break;
			}
			break;
		case NONE:
			sendMessage("Synch option was null, where it should have had an option! Revise program! dataModifyingAction", JOptionPane.ERROR_MESSAGE);
			break;
		}
	}
	
	public void loadTables() {
		this.sddesc = dbController.loadTable();
	}
	
	//sPoll is disposed of here.
	private void loadSynchedSession() {
		this.sPoll.dispose();
		this.mainView.switchToTableCard();
		switch(this.sOpt) {
		case ORACLE:
		case SQLITE:
			try {
				System.out.println("Synch session with oracle/SQLite loading.");
				dbController.setupDBInterface();
			}
			catch(SQLException exc) {
				this.dbController = null;
				this.sOpt = SynchOption.NONE;
				this.mainView.switchToHomeCard();
			}
			break;
		case NONE:
			sendMessage("No option was present for loading synched session.", JOptionPane.INFORMATION_MESSAGE);
			break;
		}
	}
	
	public void closeSynchSession() {
		switch(this.sOpt) {
		case ORACLE:
		case SQLITE:
			this.dbController.endDBSession();
			break;
		case NONE:
			sendMessage("No synch was active.", JOptionPane.INFORMATION_MESSAGE);
			break;
		}
		this.sOpt = SynchOption.NONE;
		this.sddesc = null;
	}
	
	public void setSddesc(SynchedDataDescriptor sddesc) {
		this.sddesc = sddesc;
	}
	
	public void saveAs() {
		if(this.mainView.getTableOutput().getColumnCount() <= 0 || this.mainView.getTableInput().getColumnCount() <= 0) {
			sendMessage("No data structure present to execute the action on.", JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.fController.saveAs(this.sddesc);
	}
	
	public static Object[] getRow(JTable jt, int row) {
		ArrayList<Object> oblist = new ArrayList<Object>();
		for(int i = 0; i < jt.getColumnCount(); i++) {
			Object obj = jt.getValueAt(row, i);
			if(obj != null) obj = obj.toString();
			oblist.add(obj);
		}
		return oblist.toArray();
	}
	
	public static int getSelectedIndeces(JTable jt) throws MyAppException{
		int[] selected = jt.getSelectedRows();
		if(selected.length == 0) throw new MyAppException("No rows selected!");
		if(selected.length > 1) throw new MyAppException("Too many rows selected!");
		return selected[0];
	}
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Szinkronizáció üzenet.", opt);
	}
	
	private String charsToString(char[] chs) {
		if(chs == null) return null;
		if(chs.length == 0) return null;
		StringBuilder strb = new StringBuilder();
		for(char c : chs) {
			strb.append(c);
		}
		return strb.toString();
	}
	
	public SynchOption getsOpt() {
		return sOpt;
	}
}
