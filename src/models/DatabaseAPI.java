package models;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseAPI {

	private Connection conn; //193.6.5.58:1521:XE
	private String userSpace = null;
	private ResultSet mrs = null;
	private Statement simpleQuery; 
	
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
	
	public ResultSetMetaData getResultSetMetadata() throws SQLException{
		return mrs.getMetaData();
	}
}
