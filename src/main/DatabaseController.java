package main;

import java.io.File;
import java.sql.SQLException;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

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
		this.mainView.dbPanel(true);
		this.mainView.getBtnSave().setVisible(false);
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
			for(String ft : fieldTypes) {
				if(dbapi.getMode() != 2) break;
				if(ft.equalsIgnoreCase("TIMESTAMP")) {
					sendMessage("SQLite adatbáziskezelésnél a TIMESTAMP típus használta nem javasolt!\n"
							+ "Az adatbázis nem következetes a metaadatok visszaadásánál, ezért hibát fog eredményezni a használata.\n"
							+ "Ne kezelje ezt a táblát, vagy tárolja másképp az adatbázisban!", JOptionPane.ERROR_MESSAGE);
				}
			}
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
			dbapi.prepareInsert(this.sddesc.getDataTypeName(), this.sddesc.getNames());
		}
		catch(SQLException exc) {
			sendMessage("Sikertelen lekérdezés az adatbázisból - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
			return null;
		}
		return this.sddesc;
	}
	
	public void insert() {
		try {
			Object[] values = GeneralController.getRow(this.mainView.getTableInput(), 0);
			values = gc.formatRow(values, this.sddesc.getTypes());
			dbapi.baseInsert(this.sddesc.getTypes(), values);
			buildTableFromResultSet(2);
			sendMessage("Felvitel sikeres.", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(MyAppException exc) {
			sendMessage("Felvitel sikertelen! - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(SQLException exc) {
			sendMessage("Felvitel sikertelen! - "+exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void update() {
		try {
			int sel = GeneralController.getSelectedIndeces(this.mainView.getTableOutput());
			Object[] inputValues = GeneralController.getRow(this.mainView.getTableInput(), 0);
			Object[] oldValues = GeneralController.getRow(this.mainView.getTableOutput(), sel);
			inputValues = gc.formatRow(inputValues, sddesc.getTypes());
			oldValues = gc.formatRow(oldValues, sddesc.getTypes());
			dbapi.update(this.sddesc, inputValues, oldValues);
			buildTableFromResultSet(2);
		}
		catch(MyAppException exc) {
			sendMessage("Frissítés sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(SQLException exc) {
			sendMessage("Frissítés sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		sendMessage("Frissítés sikeres.", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void delete() {
		try {
			int sel = GeneralController.getSelectedIndeces(this.mainView.getTableOutput());
			Object [] conds = GeneralController.getRow(this.mainView.getTableOutput(), sel);
			conds = gc.formatRow(conds, sddesc.getTypes());
			dbapi.delete(this.sddesc, conds);
			buildTableFromResultSet(2);
		}
		catch(MyAppException exc) {
			sendMessage("Törlés sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		catch(SQLException exc) {
			sendMessage("Törlés sikertelen! - " + exc.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		sendMessage("Törlés sikeres.", JOptionPane.INFORMATION_MESSAGE);
	}
	
	//Mode is 0: Sets up input table.
	//Mode is 1: Sets up output table from the current result set.
	//Mode is 2: Sets up output table after a query on the current data type name.
	private void buildTableFromResultSet(int mode) {
		Object[] names = this.sddesc.getNames();
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
		String[] types = this.sddesc.getTypes();
		GeneralController.setTablePreferredWidths(jt, types);
		if(mode != 0) {
			try {
				if(mode == 2) dbapi.doSimpleQuery(this.sddesc.getDataTypeName());
				while(true) {
					Object[] rows = dbapi.getResultSetNextRow();
					if(rows != null) {
						System.out.println("Reading from database...");
						rows = gc.formatRow(rows, types);
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
	
	
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(this.mainView, msg, "Adatbázis kontroll üzenet.", opt);
	}
}
