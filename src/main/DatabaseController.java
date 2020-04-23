package main;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.sql.Date;

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
	private MainView mainView;
	private SynchedDataDescriptor sddesc;
	private GeneralChecker gc = new GeneralChecker();
	
	public DatabaseController(MainView mainView) {
		this.mainView = mainView;
	}
	
	public boolean connectToSQLite(File db) {
		try {
			dbapi.connectToSQLite(db);
		}
		catch(SQLException exc) {
			sendMessage("Nem sikerült csatlakozni az SQLite adatbázisra: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch(ClassNotFoundException exc) {
			sendMessage("Nem találtunk drivert az SQLite adatbázishoz: "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	public boolean connectToOracle(String username, String password, String workspace) {
		return connectToOracle(username, password, workspace, null, true);
	}
	
	public boolean connectToOracle(String username, String password, String workspace, String URL) {
		return connectToOracle(username, password, workspace, URL, false);
	}
	
	public boolean connectToOracle(String username, String password, String workspace, String URL, boolean usedef) {
		System.out.println("DatabaseController.");
		try {
			if(usedef) dbapi.connectToOracle(username, password, workspace);
			else dbapi.connectToOracle(username, password, workspace, URL);
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
		this.mainView.getTableFieldNames().getColumnModel().getColumn(0).setPreferredWidth(200);
		this.mainView.getTableFieldNames().getColumnModel().getColumn(1).setPreferredWidth(200);
		this.mainView.getTableInput().setModel(new DefaultTableModel());
		this.mainView.getTableOutput().setModel(new OutputTableModel());
		try {
			String[] fieldNames = dbapi.getColumnNamesOrType((String)this.mainView.getComboBoxTableNames().getSelectedItem(), 0);
			String[] fieldTypes = dbapi.getColumnNamesOrType((String)this.mainView.getComboBoxTableNames().getSelectedItem(), 1);
			for(int i = 0; i < fieldNames.length; i++) {
				ntm.addRow(new Object[] {fieldNames[i], fieldTypes[i]});
			}
		}
		catch(SQLException exc) {
			sendMessage("Nem sikerült kiolvasni a mezőket! - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public SynchedDataDescriptor loadTable() {
		try {
			dbapi.doSimpleQuery((String)this.mainView.getComboBoxTableNames().getSelectedItem());
			this.sddesc = new SynchedDataDescriptor(dbapi.getResultSetMetadata(),(String)this.mainView.getComboBoxTableNames().getSelectedItem());
			buildTableFromResultSet(0);
			buildTableFromResultSet(1);
			dbapi.prepareInsert(this.sddesc.getDataTypeName(), this.sddesc.getNames().toArray(new String[0]));
		}
		catch(SQLException exc) {
			sendMessage("Querying from database failed. - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
			return null;
		}
		return this.sddesc;
	}
	
	public void insert() {
		try {
			Object[] values = getRow(this.mainView.getTableInput(), 0);
			values = gc.formatRow(values, this.sddesc.getTypes().toArray(new Class<?>[0]));
			dbapi.baseInsert(this.sddesc.getTypes().toArray(new Class<?>[0]), values);
			buildTableFromResultSet(2);
			sendMessage("Insertion successful.", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(MyAppException exc) {
			sendMessage("Insertion failed! - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(SQLException exc) {
			sendMessage("Insertion failed! - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void update() {
		try {
			int sel = SynchController.getSelectedIndeces(this.mainView.getTableOutput());
			Object[] inputValues = getRow(this.mainView.getTableInput(), 0);
			Object[] oldValues = getRow(this.mainView.getTableOutput(), sel);
			inputValues = gc.formatRow(inputValues, sddesc.getTypes().toArray(new Class<?>[0]));
			oldValues = gc.formatRow(oldValues, sddesc.getTypes().toArray(new Class<?>[0]));
			dbapi.update(this.sddesc, inputValues, oldValues);
			buildTableFromResultSet(2);
		}
		catch(MyAppException exc) {
			sendMessage("Frissítés sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(SQLException exc) {
			sendMessage("Frissítés sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void delete() {
		try {
			int sel = SynchController.getSelectedIndeces(this.mainView.getTableOutput());
			Object [] conds = getRow(this.mainView.getTableOutput(), sel);
			conds = gc.formatRow(conds, sddesc.getTypes().toArray(new Class<?>[0]));
			dbapi.delete(this.sddesc, conds);
			buildTableFromResultSet(2);
		}
		catch(MyAppException exc) {
			sendMessage("Törlés sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(SQLException exc) {
			sendMessage("Törlés sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	//Mode is 0: Sets up input table.
	//Mode is 1: Sets up output table from the current result set.
	//Mode is 2: Sets up output table after a query on the current data type name.
	private void buildTableFromResultSet(int mode) {
		Object[] names = this.sddesc.getNames().toArray(new Object[0]);
		DefaultTableModel dtm;
		JTable jt;
		if(mode == 0) {
			dtm = new InputTableModel(names);
			jt = this.mainView.getTableInput();
			jt.setModel(dtm);
		}
		else{
			dtm = new OutputTableModel(names, 0);
			jt = this.mainView.getTableOutput();
			jt.setModel(dtm);
		}
		Class<?>[] cls = this.sddesc.getTypes().toArray(new Class<?>[0]);
		for(int i = 0; i < cls.length; i++) {
			//System.out.println(i + ": class - " + cls[i].toString());
			if(cls[i].equals(Integer.class)) jt.getColumnModel().getColumn(i).setPreferredWidth(120);
			else if(cls[i].equals(java.util.Date.class) || cls[i].equals(Timestamp.class) || cls[i].equals(java.sql.Date.class)) jt.getColumnModel().getColumn(i).setPreferredWidth(200);
			else if(cls[i].equals(String.class)) jt.getColumnModel().getColumn(i).setPreferredWidth(300);
			else jt.getColumnModel().getColumn(i).setPreferredWidth(50);
		}
		if(mode != 0) {
			try {
				if(mode == 2) dbapi.doSimpleQuery(this.sddesc.getDataTypeName());
				while(true) {
					Object[] rows = dbapi.getResultSetNextRow();
					if(rows != null) {
						rows = gc.formatRow(rows, this.sddesc.getTypes().toArray(new Class<?>[0]));
						dtm.addRow(rows);
					}
					else break;
				}
			}
			catch(MyAppException exc) {
				sendMessage("A kiolvasás az adatbázisból sikertelen! - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			}
			catch(SQLException exc) {
				sendMessage("A kiolvasás az adatbázisból sikertelen! - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public Object[] getRow(JTable jt, int row) {
		ArrayList<Object> oblist = new ArrayList<Object>();
		for(int i = 0; i < jt.getColumnCount(); i++) {
			Object obj = jt.getValueAt(row, i);
			if(obj != null) obj = obj.toString();
			oblist.add(obj);
		}
		return oblist.toArray();
	}
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Adatbázis kontroll üzenet.", opt);
	}
}
