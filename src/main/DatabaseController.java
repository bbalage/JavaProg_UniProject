package main;

import java.sql.SQLException;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
		cb.addItem("Válasszon táblát!");
		for(String str : tableNames) cb.addItem(str);
		mainView.switchToTableCard();
	}
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Szinkronizáció üzenet.", opt);
	}
	
	public void tableSelected(MainView mainView) {
		
	}
}
