package models;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

public class DatabaseAPI {

	private Connection conn;
	private static final String DEFAULTURL = "193.6.5.58:1521:XE";
	private String userSpace = null;
	private ResultSet mrs = null;
	private Statement simpleQuery;
	private PreparedStatement insertstmt;
	private ResultSetMetaData insertrsmd;
	private int mode; //0 for none, 1 for oracle, 2 for SQLite
	/*
		insertrsmd is problem source: desynchronization between resultset and synched data descriptor might cause crash (bad network connection).
		Checking synchronization or using only one should be implemented in further versions.
		
		Timestamp values in SQLite are named Timestamp but treated like strings.
		This can cause crashes.
	*/
	public void connectToSQLite(File db) throws ClassNotFoundException, SQLException{
		String URL = "jdbc:sqlite:"+db.getAbsolutePath();
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection(URL);
		this.userSpace = null;
		simpleQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		this.mode = 2;
	}
	
	public void connectToOracle(String username, String password, String workspace) throws SQLException, ClassNotFoundException{
		connectToOracle(username, password, workspace, DEFAULTURL);
	}
	
	public void connectToOracle(String username, String password, String workspace, String URL) throws SQLException, ClassNotFoundException{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		conn = DriverManager.getConnection("jdbc:oracle:thin:@"+URL,username,password);
		this.userSpace = workspace;
		simpleQuery = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		this.mode = 1;
	}
	
	public void closeConnection() throws SQLException{
		if(conn != null) {
			conn.close();
			this.insertrsmd = null;
			this.insertstmt = null;
			this.mrs = null;
			this.simpleQuery = null;
			this.mode = 0;
		}
	}
	
	public String[] getColumnNamesOrType(String tablename, int what) throws SQLException{
		String query = "";
		if(what == 0) query = "COLUMN_NAME";
		else query = "TYPE_NAME";
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getColumns(null, this.userSpace, tablename, null);
		ArrayList<String> columns = new ArrayList<String>();
		while(rs.next()) {
			columns.add(rs.getString(query));
		}
		return columns.toArray(new String[0]);
	}
	
	

	public String[] getTableNames() throws SQLException{
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getTables(null, this.userSpace, "%", null);
		ArrayList<String> tableNameList = new ArrayList<String>();
		while(rs.next()) {
			tableNameList.add(rs.getString("TABLE_NAME"));
		}
		return tableNameList.toArray(new String[0]);
	}
	
	public void doSimpleQuery(String tablename) throws SQLException {
		String sql = "SELECT * FROM "+tablename;
		this.mrs = simpleQuery.executeQuery(sql);
	}
	
	public Object[] getResultSetNextRow() throws SQLException{
		if(mrs.next()) {
			ResultSetMetaData rsmd = mrs.getMetaData();
			ArrayList<Object> objs = new ArrayList<Object>();
			for(int i = 1; i <= rsmd.getColumnCount(); i++) {
				objs.add(mrs.getObject(i));
			}
			return objs.toArray();
		}
		else return null;
	}
	
	public void baseInsert(String[] rowtypes, Object[] values) throws SQLException{
		for(int i = 0; i < rowtypes.length; i++) {
			if(values[i] == null) insertstmt.setNull(i+1, insertrsmd.getColumnType(i+1));
			else if(rowtypes[i].equals(Integer.class.getCanonicalName())) insertstmt.setInt(i+1, (Integer)values[i]);
			else if(rowtypes[i].equals(java.sql.Date.class.getCanonicalName())) {
				if(mode == 1)insertstmt.setDate(i+1, (java.sql.Date)values[i]);
				else if(mode == 2) {
					insertstmt.setString(i+1, ((java.sql.Date)values[i]).toString());
				}
			}
			else if(rowtypes[i].equals(Timestamp.class.getCanonicalName())) {
				if(mode == 1) insertstmt.setTimestamp(i+1, (Timestamp)values[i]);
				else if(mode == 2) {
					insertstmt.setString(i+1, ((java.sql.Timestamp)values[i]).toString());
				}
			}
			else if(rowtypes[i].equals(String.class.getCanonicalName())) insertstmt.setString(i+1, (String)values[i]);
			else throw new SQLException("Bind value none of the types we are ready to handle: "+rowtypes[i]);
		}
		insertstmt.execute();
	}
	
	public void prepareInsert(String tablename, String[] rownames) throws SQLException{
		String sql = "INSERT INTO " + tablename + " values(";
		for(int i = 0; i < rownames.length; i++) {
			sql = sql+"?, ";
		}
		sql = sql.substring(0, sql.length()-2) + ")";
		System.out.println("Insert statement to be prepared: "+sql);
		insertstmt = conn.prepareStatement(sql);
		insertrsmd = mrs.getMetaData();
	}
	
	public void update(SynchedDataDescriptor sddesc, Object[] newvalues, Object[] oldvalues) throws SQLException{
		String sql = "UPDATE " + sddesc.getDataTypeName() + " SET ";
		String[] rownames = sddesc.getNames();
		for(int i = 0; i < rownames.length; i++) {
			sql = sql + rownames[i] + "= ?, ";
		}
		sql = sql.substring(0,sql.length()-2) + " WHERE ";
		for(int i = 0; i < rownames.length; i++) {
			if(oldvalues[i] != null) {
				sql = sql + rownames[i] + "= ? AND ";
			}
			else {
				sql = sql + rownames[i] + " IS NULL AND ";
			}
		}
		sql = sql.substring(0, sql.length()-5);
		System.out.println("Update to be prepared: "+sql);
		PreparedStatement updatestmt = conn.prepareStatement(sql);
		String[] rowtypes = sddesc.getTypes();
		for(int i = 0, j = rowtypes.length+1; i < rowtypes.length; i++) {
			System.out.println("i: "+i+" j: "+j);
			if(rowtypes[i].equals(Integer.class.getCanonicalName())) {
				if(newvalues[i] != null) updatestmt.setInt(i+1, (Integer)newvalues[i]);
				else updatestmt.setNull(i+1, insertrsmd.getColumnType(i+1));
				if(oldvalues[i] != null) updatestmt.setInt(j++, (Integer)oldvalues[i]);
			}
			else if(rowtypes[i].equals(java.sql.Date.class.getCanonicalName())) {
				if(newvalues[i] != null) {
					if(mode == 1) updatestmt.setDate(i+1, (Date)newvalues[i]);
					else if(mode == 2) updatestmt.setString(i+1, newvalues[i].toString());
				}
				else updatestmt.setNull(i+1, insertrsmd.getColumnType(i+1));
				if(oldvalues[i] != null) {
					if(mode == 1) updatestmt.setDate(j++, (Date)oldvalues[i]);
					else if(mode == 2) updatestmt.setString(j++, oldvalues[i].toString());
				}
			}
			else if(rowtypes[i].equals(Timestamp.class.getCanonicalName())) {
				if(newvalues[i] != null) {
					if(mode == 1) updatestmt.setTimestamp(i+1, (Timestamp)newvalues[i]);
					else if(mode == 2) updatestmt.setString(i+1, newvalues[i].toString());
				}
				else updatestmt.setNull(i+1, insertrsmd.getColumnType(i+1));
				if(oldvalues[i] != null) {
					if(mode == 1) updatestmt.setTimestamp(j++, (Timestamp)oldvalues[i]);
					else if(mode == 2) updatestmt.setString(j++, oldvalues[i].toString());
				}
			}
			else if(rowtypes[i].equals(String.class.getCanonicalName())) {
				if(newvalues[i] != null) updatestmt.setString(i+1, (String)newvalues[i]);
				else updatestmt.setNull(i+1, insertrsmd.getColumnType(i+1));
				if(oldvalues[i] != null) updatestmt.setString(j++, (String)oldvalues[i]);
			}
		}
		updatestmt.execute();
	}
	
	public void delete(SynchedDataDescriptor sddesc, Object[] conds) throws SQLException{
		String sql = "DELETE FROM " + sddesc.getDataTypeName() + " WHERE ";
		String[] columnnames = sddesc.getNames();
		String[] rowtypes = sddesc.getTypes();
		for(int i = 0; i < columnnames.length; i++) {
			if(conds[i] != null) {
				sql = sql + columnnames[i] + "= ? AND ";
			}
			else {
				sql = sql + columnnames[i] + " IS NULL AND ";
			}
		}
		sql = sql.substring(0, sql.length()-5);
		System.out.println(sql);
		PreparedStatement deletestmt = conn.prepareStatement(sql);
		for(int i = 0; i < rowtypes.length; i++) {
			if(conds[i] == null) continue;
			else if(rowtypes[i].equals(Integer.class.getCanonicalName())) deletestmt.setInt(i+1, (Integer)conds[i]);
			else if(rowtypes[i].equals(java.sql.Date.class.getCanonicalName())) {
				if(mode == 1) deletestmt.setDate(i+1, (java.sql.Date)conds[i]);
				else if(mode == 2) deletestmt.setString(i+1, conds[i].toString());
			}
			else if(rowtypes[i].equals(Timestamp.class.getCanonicalName())) {
				if(mode == 1) deletestmt.setTimestamp(i+1, (Timestamp)conds[i]);
				else if(mode == 2) deletestmt.setString(i+1, conds[i].toString());
			}
			else if(rowtypes[i].equals(String.class.getCanonicalName())) deletestmt.setString(i+1, (String)conds[i]);
			else throw new SQLException("Bind value none of the types we are ready to handle: "+rowtypes[i]);
		}
		deletestmt.execute();
	}
	
	
	public int getMode() {
		return mode;
	}
	
	public ResultSetMetaData getResultSetMetadata() throws SQLException{
		return mrs.getMetaData();
	}
}
