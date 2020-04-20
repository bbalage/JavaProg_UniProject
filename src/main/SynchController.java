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
	private SynchPoll sPoll = null;
	private LoginView lView = null;
	private DatabaseAPI dbapi = new DatabaseAPI();
	private MainView mainView = null;
	
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
		String username = lView.textUsername.getText();
		String password = charsToString(lView.passwordField.getPassword());
		boolean ok = true;
		try {
			if(lView.checkBoxDefaultServer.isSelected()) {
				dbapi.connectToOracle(username, password);
			}
			else {
				String URL = lView.textURL.getText();
				dbapi.connectToOracle(username, password, URL);
			}
		}
		catch(SQLException exc) {
			sendMessage("Nem sikerült a login az Oracle adatbázisra: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			ok = false;
		}
		catch(ClassNotFoundException exc) {
			sendMessage("Nem találtunk drivert az Oracle adatbázishoz: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			ok = false;
		}
		if(ok) {
			sendMessage("Sikeres bejelentkezés az Oracle adatbázisra.", JOptionPane.INFORMATION_MESSAGE);
			this.sOpt = SynchOption.ORACLE;
			this.lView.dispose();
			loadSynchedSession();
		}
	}
	
	private void loadSynchedSession() {
		this.sPoll.dispose();
		this.mainView.switchToTableCard();
		switch(this.sOpt) {
		case ORACLE:
		//case SQLITE:
			System.out.println("Synch session with oracle loading.");
			break;
		}
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
