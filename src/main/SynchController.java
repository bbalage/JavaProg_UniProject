package main;

import java.awt.Window;
import java.io.File;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import models.*;
import utilities.*;

public class SynchController {
	
	private SynchOption sOpt = SynchOption.NONE;
	private SynchPoll sPoll;
	private LoginView lView;
	private MainView mainView;
	private DatabaseController dbController;
	private FileController fController;
	private SynchedDataDescriptor sddesc;
	private HelpController hc = new HelpController();
	
	SynchController(MainView mv){
		this.dbController = new DatabaseController(mv);
		this.fController = new FileController(mv);
		this.mainView = mv;
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
		case JSON:
			synchWithJson();
			break;
		case CSV:
			synchWithCsv();
			break;
		case NONE:
			sendMessage("Miscarried functioning! Synchoption was NONE! Revise program! syncWithType", JOptionPane.ERROR_MESSAGE);
			break;
		}
		}
	
	public void startSynchSession() {
		this.sPoll = new SynchPoll(this.mainView, SynchController.this);
		this.sPoll.setVisible(true);
		
	}
	
	public void cancelSynchSession() {
		this.sPoll.dispose();
	}
	
	public void cancelSynchWithOracle() {
		this.lView.dispose();
	}
	
	private void synchWithCsv() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Válassza ki a csv fájlt, amivel szinkronizálni szeretne!");
		int ret = jfc.showOpenDialog(this.sPoll);
		if(ret == JFileChooser.APPROVE_OPTION) {
			File csvFile = jfc.getSelectedFile();
			this.sddesc = this.fController.loadFile(csvFile, 0);
		}
		if(this.sddesc != null) {
			sendMessage("Sikeres kapcsolódás a csv fájllal.", JOptionPane.INFORMATION_MESSAGE);
			this.sOpt = SynchOption.CSV;
			this.sPoll.dispose();
			loadSynchedSession();
		}
	}
	
	private void synchWithJson() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Válassza ki a json fájlt, amivel szinkronizálni szeretne!");
		int ret = jfc.showOpenDialog(this.sPoll);
		if(ret == JFileChooser.APPROVE_OPTION) {
			File jsonFile = jfc.getSelectedFile();
			this.sddesc = this.fController.loadFile(jsonFile, 2);
		}
		if(this.sddesc != null) {
			sendMessage("Sikeres kapcsolódás a json fájllal.", JOptionPane.INFORMATION_MESSAGE);
			this.sOpt = SynchOption.JSON;
			this.sPoll.dispose();
			loadSynchedSession();
		}
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
		String password = GeneralController.charsToString(lView.getPasswordField().getPassword());
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
			sendMessage("Nem volt adat, amin a műveletet el lehetett volna végezni!", JOptionPane.ERROR_MESSAGE);
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
		case XML:
		case JSON:
		case CSV:
			switch(type) {
			case 0:	this.fController.insert(); break;
			case 1:	this.fController.update(); break;
			case 2: this.fController.delete(); break;
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
				dbController.setupDBInterface();
			}
			catch(SQLException exc) {
				this.sOpt = SynchOption.NONE;
				this.mainView.switchToHomeCard();
			}
			break;
		case XML:
		case JSON:
		case CSV:
			fController.setupFileInterface();
			break;
		case NONE:
			sendMessage("No option was present for loading synched session. Revise program!", JOptionPane.INFORMATION_MESSAGE);
			break;
		}
	}
	
	public void closeSynchSession() {
		switch(this.sOpt) {
		case ORACLE:
		case SQLITE:
			this.dbController.endDBSession();
			break;
		case XML:
		case JSON:
		case CSV:
			this.fController.endFileSession();
			this.sddesc = null;
			break;
		case NONE:
			sendMessage("No synch was active. Revise program!", JOptionPane.INFORMATION_MESSAGE);
			break;
		}
		this.sOpt = SynchOption.NONE;
		this.sddesc = null;
	}
	
	public void save() {
		fController.saveBySDDesc();
	}
	
	public void tableSelected() {
		this.dbController.tableSelected();
	}
	
	public void saveAs() {
		if(this.mainView.getTableOutput().getColumnCount() <= 0 || this.mainView.getTableInput().getColumnCount() <= 0) {
			sendMessage("Nem volt adat, amin a műveletet végre lehetett volna hajtani!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.fController.saveAs(this.sddesc);
	}
		
	public void fetchHelp(Window caller, int mode) {
		HelpOptions ho = null;
		if(mode == 0) ho = HelpOptions.ORACLE_LOGIN;
		else if(mode == 1) ho = this.sOpt == SynchOption.ORACLE || this.sOpt == SynchOption.SQLITE ? HelpOptions.DATABASE_HELP : HelpOptions.FILE_HELP;
		else {
			sendMessage("Nem található ilyen segítség!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		hc.getHelpWindow(caller, ho);
	}
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Szinkronizáció üzenet.", opt);
	}
	
	public SynchOption getsOpt() {
		return sOpt;
	}
}
