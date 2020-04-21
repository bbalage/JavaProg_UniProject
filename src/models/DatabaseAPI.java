package models;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

public class DatabaseAPI {

	private Connection conn; //193.6.5.58:1521:XE
	private String userSpace = null;
	private ResultSet mrs = null;
	private Statement simpleQuery;
	private PreparedStatement insertstmt;
	private ResultSetMetaData insertrsmd;
	
	public void connectToOracle(String username, String password, String URL) throws SQLException, ClassNotFoundException{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		conn = DriverManager.getConnection("jdbc:oracle:thin:@"+URL,username,password);
		this.userSpace = username;
		simpleQuery = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}
	
	public void closeConnection() throws SQLException{
		if(conn != null) {
			//this.insertrsmd = null;
			//this.insertstmt = null;
			//this.mrs = null;
			//this.simpleQuery = null;
			conn.close();
		}
	}
	
	public String[] getColumnNamesAndType(String tablename) throws SQLException{
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getColumns(null, this.userSpace, tablename, null);
		ArrayList<String> columns = new ArrayList<String>();
		while(rs.next()) {
			columns.add(rs.getString("COLUMN_NAME") + " : " + rs.getString("TYPE_NAME"));
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
	
	public boolean doSimpleQuery(String tablename, int[] columns) throws SQLException {
		String sql = "SELECT ";
		if(columns != null) {
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet rs = dbmd.getColumns(null, this.userSpace, tablename, null);
			String cols = "";
			for(int i = 0, j = 0; i < columns.length; j++) {
				rs.next();
				if(columns[i] == j) {
					i++;
					cols += rs.getString("COLUMN_NAME") +", ";
				}
			}
			sql = sql + cols.substring(0, cols.length()-2);
		}
		else {
			sql = sql + "*";
		}
		sql = sql+" FROM "+tablename;
		this.mrs = simpleQuery.executeQuery(sql);
		boolean notEmpty = this.mrs.next();
		if(notEmpty) this.mrs.beforeFirst();
		return notEmpty;
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
	
	public void baseInsert(String[] rownames, Object[] values) throws SQLException{
		int columns = insertrsmd.getColumnCount();
		for(int i = 0, j = 1; i < columns; i++) {
			for(; j <= columns; j++) {
				System.out.println(insertrsmd.getColumnName(j));
				if(rownames[i].equals(insertrsmd.getColumnName(j))) {
					if(values[i] == null) {
						insertstmt.setNull(i+1, insertrsmd.getColumnType(j));
						break;
					}
					switch(insertrsmd.getColumnType(j)) {
					case Types.NUMERIC:
					case Types.INTEGER:
						insertstmt.setInt(i+1, Integer.parseInt(((String)values[i])));
						break;
					case Types.DATE:
					case Types.TIMESTAMP:
					case Types.TIME:
						insertstmt.setDate(i+1, Date.valueOf((String)values[i]));
						break;
					case Types.VARCHAR:
					case Types.LONGNVARCHAR:
					case Types.CHAR:
						insertstmt.setString(i+1, (String)values[i]);
						break;
					}
					break;
				}
			}
		}
		insertstmt.execute();
	}
	
	public void prepareInsert(String tablename, String[] rownames) throws SQLException{
		String sql = "INSERT INTO " + tablename + "(";
		insertrsmd = mrs.getMetaData();
		int column = 1;
		boolean[] setDefault = new boolean[insertrsmd.getColumnCount()];
		for(int i = 0; i < setDefault.length; i++) {
			sql = sql+rownames[i]+", ";
			for(;column<=insertrsmd.getColumnCount()+1; column++) {
				if(column > insertrsmd.getColumnCount()) {
					throw new SQLException("ResultSet doesn't match insert pattern.");
				}
				if(rownames[i].equals(insertrsmd.getColumnName(column))) {
					setDefault[i] = false;
					break;
				}
				else {
					setDefault[i] = true;
				}
			}
		}
		sql = sql.substring(0, sql.length()-2) + ") values(";
		for(int i = 0; i < rownames.length; i++) {
			if(setDefault[i]) sql = sql+"default, ";
			else sql = sql+"?, ";
		}
		sql = sql.substring(0, sql.length()-2) + ")";
		System.out.println("Insert statement to be prepared: "+sql);
		insertstmt = conn.prepareStatement(sql);
	}
	
	
	public ResultSetMetaData getResultSetMetadata() throws SQLException{
		return mrs.getMetaData();
	}
}
