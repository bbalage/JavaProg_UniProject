package main;

import java.sql.SQLException;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import models.DatabaseAPI;
import utilities.*;

public class DatabaseController {

	private DatabaseAPI dbapi = new DatabaseAPI();
	private static final String DEFAULTURL = "193.6.5.58:1521:XE";
	
	
	
	public boolean connectToOracle(String username, String password) {
		return connectToOracle(username, password, DEFAULTURL);
	}
	
	public boolean connectToOracle(String username, String password, String URL) {
		System.out.println("DatabaseController.");
		try {
			dbapi.connectToOracle(username, password, URL);
		}
		catch(SQLException exc) {
			sendMessage("Nem sikerült a login az Oracle adatbázisra: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch(ClassNotFoundException exc) {
			sendMessage("Nem találtunk drivert az Oracle adatbázishoz: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	public void setupDBInterface(MainView mainView) throws SQLException {
		JComboBox<String> cb = mainView.getComboBoxTableNames();
		String[] tableNames = dbapi.getTableNames();
		cb.removeAllItems();
		cb.addItem("Válasszon táblát!");
		for(String str : tableNames) cb.addItem(str);
		mainView.switchToTableCard();
	}
	
	public void endDBSession(MainView mainView) {
		try {
			dbapi.closeConnection();
		}
		catch(SQLException exc) {
			sendMessage("Kapcsolat lezárása sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		mainView.getTableFieldNames().setModel(new NameTableModel());
		mainView.getTableInput().setModel(new DefaultTableModel());
		mainView.getTableOutput().setModel(new OutputTableModel());
		mainView.switchToHomeCard();
	}
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Szinkronizáció üzenet.", opt);
	}
	
	public void tableSelected(MainView mainView) {
		NameTableModel ntm = new NameTableModel();
		mainView.getTableFieldNames().setModel(ntm);
		mainView.getTableFieldNames().getColumnModel().getColumn(0).setPreferredWidth(40);
		mainView.getTableFieldNames().getColumnModel().getColumn(1).setPreferredWidth(360);
		mainView.getTableInput().setModel(new DefaultTableModel());
		mainView.getTableOutput().setModel(new OutputTableModel());
		try {
			String[] fieldNames = dbapi.getColumnNamesAndType((String)mainView.getComboBoxTableNames().getSelectedItem());
			for(String fn : fieldNames) {
				ntm.addRow(new Object[] {false, fn});
			}
		}
		catch(SQLException exc) {
			sendMessage("Nem sikerült kiolvasni a mezőket! - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}
}
