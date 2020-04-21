package main;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import models.*;
import utilities.*;

public class DatabaseController {

	private DatabaseAPI dbapi = new DatabaseAPI();
	private static final String DEFAULTURL = "193.6.5.58:1521:XE";
	private MainView mainView;
	private SynchedDataDescriptor sddesc;
	
	public DatabaseController(MainView mainView) {
		this.mainView = mainView;
	}
	
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
	
	public void setupDBInterface() throws SQLException {
		JComboBox<String> cb = this.mainView.getComboBoxTableNames();
		String[] tableNames = dbapi.getTableNames();
		cb.removeAllItems();
		for(String str : tableNames) cb.addItem(str);
		this.mainView.switchToTableCard();
	}
	
	public void endDBSession() {
		try {
			dbapi.closeConnection();
		}
		catch(SQLException exc) {
			sendMessage("Kapcsolat lezárása sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		this.mainView.getTableFieldNames().setModel(new NameTableModel());
		this.mainView.getTableInput().setModel(new DefaultTableModel());
		this.mainView.getTableOutput().setModel(new OutputTableModel());
		this.mainView.switchToHomeCard();
	}
	
	public void tableSelected() {
		NameTableModel ntm = new NameTableModel();
		this.mainView.getTableFieldNames().setModel(ntm);
		this.mainView.getTableFieldNames().getColumnModel().getColumn(0).setPreferredWidth(40);
		this.mainView.getTableFieldNames().getColumnModel().getColumn(1).setPreferredWidth(360);
		this.mainView.getTableInput().setModel(new DefaultTableModel());
		this.mainView.getTableOutput().setModel(new OutputTableModel());
		try {
			String[] fieldNames = dbapi.getColumnNamesAndType((String)this.mainView.getComboBoxTableNames().getSelectedItem());
			for(String fn : fieldNames) {
				ntm.addRow(new Object[] {false, fn});
			}
		}
		catch(SQLException exc) {
			sendMessage("Nem sikerült kiolvasni a mezőket! - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public SynchedDataDescriptor loadTable() {
		int[] selected = getSelectedFromNameTable();
		
		try {
			dbapi.doSimpleQuery((String)this.mainView.getComboBoxTableNames().getSelectedItem(), selected);
			System.out.println("Did simple query.");
			this.sddesc = new SynchedDataDescriptor(dbapi.getResultSetMetadata());
			System.out.println("Created data descriptor.");
			Object[] names = this.sddesc.getNames().toArray(new Object[0]);
			DefaultTableModel itm = new DefaultTableModel(names, 1);
			OutputTableModel otm = new OutputTableModel(names, 0);
			JTable ot = this.mainView.getTableOutput();
			ot.setModel(otm);
			JTable it = this.mainView.getTableInput();
			it.setModel(itm);
			Class<?>[] cls = this.sddesc.getTypes().toArray(new Class<?>[0]);
			for(int i = 0; i < cls.length; i++) {
				//System.out.println(i + ": class - " + cls[i].toString());
				if(cls[i].equals(Integer.class)) {
					it.getColumnModel().getColumn(i).setPreferredWidth(120);
					ot.getColumnModel().getColumn(i).setPreferredWidth(120);
				}
				else if(cls[i].equals(Date.class) || cls[i].equals(Timestamp.class)) {
					it.getColumnModel().getColumn(i).setPreferredWidth(200);
					ot.getColumnModel().getColumn(i).setPreferredWidth(200);
				}
				else if(cls[i].equals(String.class)) {
					it.getColumnModel().getColumn(i).setPreferredWidth(300);
					ot.getColumnModel().getColumn(i).setPreferredWidth(300);
				}
				else {
					it.getColumnModel().getColumn(i).setPreferredWidth(50);
					ot.getColumnModel().getColumn(i).setPreferredWidth(50);
				}
			}
			while(true) {
				Object[] rows = dbapi.getResultSetNextRow();
				if(rows != null) {
					otm.addRow(rows);
				}
				else break;
			}
		}
		catch(SQLException exc) {
			sendMessage("Querying from database failed. - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
			return null;
		}
		
		return this.sddesc;
	}
	
	private int[] getSelectedFromNameTable() {
		NameTableModel nmt = (NameTableModel)this.mainView.getTableFieldNames().getModel();
		ArrayList<Integer> selList = new ArrayList<Integer>();
		for(int i = 0; i < nmt.getRowCount(); i++) {
			if((Boolean)nmt.getValueAt(i, 0)) {
				selList.add(i);
			}
		}
		if(selList.size() == 0) return null;
		Integer[] integers = selList.toArray(new Integer[0]);
		int[] ints = new int[integers.length];
		for(int i = 0; i < ints.length; i++) {
			ints[i] = integers[i];
		}
		return ints;
	}
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Szinkronizáció üzenet.", opt);
	}
}
