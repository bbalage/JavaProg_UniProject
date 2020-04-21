package main;

import java.sql.SQLException;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import models.*;
import utilities.*;

public class SynchController {
	
	private SynchOption sOpt = SynchOption.NONE;
	private SynchPoll sPoll;
	private LoginView lView;
	private MainView mainView;
	private DatabaseController dbController;
	private SynchedDataDescriptor sddesc;
	
	SynchController(DatabaseController dbc){
		this.dbController = dbc;
	}
	
	public void synchWithType(SynchOption sOpt) {
		if(sOpt == SynchOption.ORACLE) {
			synchWithOracle();
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
	
	public void synchWithOracle() {
		this.lView = new LoginView(sPoll, SynchController.this);
		lView.setVisible(true);
	}
	
	public void loginToOracle(/*JTextField usernameField, JTextField passwordField, JTextField URLField, JCheckBox defser*/) {
		String username = lView.getTextUsername().getText();
		String password = charsToString(lView.getPasswordField().getPassword());
		boolean ok;
		if(lView.getCheckBoxDefaultServer().isSelected()) {
			ok = dbController.connectToOracle(username, password);
		}
		else {
			String URL = lView.getTextURL().getText();
			ok = dbController.connectToOracle(username, password, URL);
		}
		if(ok) {
			sendMessage("Sikeres bejelentkezés az Oracle adatbázisra.", JOptionPane.INFORMATION_MESSAGE);
			this.sOpt = SynchOption.ORACLE;
			this.lView.dispose();
			loadSynchedSession();
		}
	}
	
	public void insertAction() {
		switch(this.sOpt) {
		case ORACLE:
			this.dbController.insert();
			break;
		}
	}
	
	
	public void loadTables() {
		this.sddesc = dbController.loadTable();
	}
	
	private void loadSynchedSession() {
		this.sPoll.dispose();
		this.mainView.switchToTableCard();
		mainView.dbPanel(false);
		switch(this.sOpt) {
		case ORACLE:
		//case SQLITE:
			try {
				System.out.println("Synch session with oracle loading.");
				dbController.setupDBInterface();
				mainView.dbPanel(true);
			}
			catch(SQLException exc) {
				this.dbController = null;
				this.sOpt = SynchOption.NONE;
				this.mainView.switchToHomeCard();
			}
			break;
		}
	}
	
	public void closeSynchSession() {
		switch(this.sOpt) {
		case ORACLE:
		//case SQLITE:
			this.dbController.endDBSession();
			break;
		}
		this.sOpt = SynchOption.NONE;
		this.sddesc = null;
	}
	
	public void setSddesc(SynchedDataDescriptor sddesc) {
		this.sddesc = sddesc;
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
