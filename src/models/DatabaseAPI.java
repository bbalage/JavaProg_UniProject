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
import java.sql.Timestamp;
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
			conn.close();
			this.insertrsmd = null;
			this.insertstmt = null;
			this.mrs = null;
			this.simpleQuery = null;
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
	
	public boolean doSimpleQuery(String tablename) throws SQLException {
		String sql = "SELECT * FROM "+tablename;
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
	
	public void baseInsert(Class<?>[] rowtypes, Object[] values) throws SQLException{
		for(int i = 0; i < rowtypes.length; i++) {
			if(values[i] == null) {
				insertstmt.setNull(i+1, insertrsmd.getColumnType(i+1));
				continue;
			}
			if(rowtypes[i].equals(Integer.class)) insertstmt.setInt(i+1, Integer.parseInt(((String)values[i])));
			if(rowtypes[i].equals(java.util.Date.class) || rowtypes[i].equals(Timestamp.class)) insertstmt.setDate(i+1, Date.valueOf((String)values[i]));
			if(rowtypes[i].equals(String.class)) insertstmt.setString(i+1, (String)values[i]);
		}
		insertstmt.execute();
	}
	
	public void prepareInsert(String tablename, String[] rownames) throws SQLException{
		String sql = "INSERT INTO " + tablename + "(";
		insertrsmd = mrs.getMetaData();
		int column = 1;
		for(String names : rownames) {
			sql = sql+names+", ";
			for(;column<=insertrsmd.getColumnCount()+1; column++) {
				if(column > insertrsmd.getColumnCount()) {
					throw new SQLException("ResultSet doesn't match insert pattern.");
				}
				if(names.equals(insertrsmd.getColumnName(column))) {
					break;
				}
			}
		}
		sql = sql.substring(0, sql.length()-2) + ") values(";
		for(int i = 0; i < rownames.length; i++) {
			sql = sql+"?, ";
		}
		sql = sql.substring(0, sql.length()-2) + ")";
		System.out.println("Insert statement to be prepared: "+sql);
		insertstmt = conn.prepareStatement(sql);
	}
	
	public void update(SynchedDataDescriptor sddesc, Object[] newvalues, Object[] oldvalues) throws SQLException{
		String sql = "UPDATE " + sddesc.getDataTypeName() + " SET ";
		String[] rownames = sddesc.getNames().toArray(new String[0]);
		ResultSetMetaData updatersmd = mrs.getMetaData();
		for(int i = 0, column = 1; i < rownames.length; i++) {
			sql = sql + rownames[i] + "= ?, ";
			for(;column<=updatersmd.getColumnCount()+1; column++) {
				if(column > updatersmd.getColumnCount()) {
					throw new SQLException("ResultSet doesn't match input pattern.");
				}
				if(rownames[i].equals(updatersmd.getColumnName(column))) {
					break;
				}
			}
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
		int columns = updatersmd.getColumnCount();
		Class<?>[] rowtypes = sddesc.getTypes().toArray(new Class<?>[0]);
		for(int i = 0, j = columns+1; i < rowtypes.length; i++) {
			/*if(newvalues[i] == null) {
				updatestmt.setNull(i+1, insertrsmd.getColumnType(i+1));
			}*/
			if(rowtypes[i].equals(Integer.class)) {
				if(newvalues[i] != null) updatestmt.setInt(i+1, Integer.parseInt(((String)newvalues[i])));
				else updatestmt.setNull(i+1, insertrsmd.getColumnType(i+1));
				if(oldvalues[i] != null) updatestmt.setInt(j++, (Integer)oldvalues[i]);
			}
			else if(rowtypes[i].equals(java.util.Date.class)) {
				if(newvalues[i] != null) updatestmt.setDate(i+1, Date.valueOf((String)newvalues[i]));
				else updatestmt.setNull(i+1, insertrsmd.getColumnType(i+1));
				if(oldvalues[i] != null) updatestmt.setDate(j++, (Date)oldvalues[i]);
			}
			else if(rowtypes[i].equals(Timestamp.class)) {
				if(newvalues[i] != null) updatestmt.setTimestamp(i+1, Timestamp.valueOf((String)newvalues[i]));
				else updatestmt.setNull(i+1, insertrsmd.getColumnType(i+1));
				if(oldvalues[i] != null) updatestmt.setTimestamp(j++, (Timestamp)oldvalues[i]);
			}
			else if(rowtypes[i].equals(String.class)) {
				if(newvalues[i] != null) updatestmt.setString(i+1, (String)newvalues[i]);
				else updatestmt.setNull(i+1, insertrsmd.getColumnType(i+1));
				if(oldvalues[i] != null) updatestmt.setString(j++, (String)oldvalues[i]);
			}
		}
		/*for(int i = 0, j = 1; i < columns; i++) {
			for(; j <= columns; j++) {
				if(rownames[i].equals(updatersmd.getColumnName(j))) {
					if(newvalues[i] == null) {
						updatestmt.setNull(i+1, updatersmd.getColumnType(j));
						break;
					}
					switch(updatersmd.getColumnType(j)) {
					case Types.NUMERIC:
					case Types.INTEGER:
						System.out.println("Setting newvalue: " + (String)newvalues[i]);
						updatestmt.setInt(i+1, Integer.parseInt(((String)newvalues[i])));
						break;
					case Types.DATE:
					case Types.TIMESTAMP:
					case Types.TIME:
						System.out.println("Setting newvalue: " + (String)newvalues[i]);
						updatestmt.setDate(i+1, Date.valueOf((String)newvalues[i]));
						break;
					case Types.VARCHAR:
					case Types.LONGNVARCHAR:
					case Types.CHAR:
						System.out.println("Setting newvalue: " + (String)newvalues[i]);
						updatestmt.setString(i+1, (String)newvalues[i]);
						break;
					}
					break;
				}
			}
		}
		for(int i = 0, j = 1, conds = 1; i < columns; i++) {
			for(; j <= columns; j++) {
				if(rownames[i].equals(updatersmd.getColumnName(j))) {
					if(conditions[i] == null) {
						break;
					}
					updatestmt.setObject(conds+columns, conditions[i]);
					switch(updatersmd.getColumnType(j)) {
					case Types.NUMERIC:
					case Types.INTEGER:
						System.out.println("Setting oldvalue: " + (Integer)conditions[i]);
						updatestmt.setInt(conds+columns, (((Integer)conditions[i])));
						break;
					case Types.TIMESTAMP:
						System.out.println("Setting oldvalue: " + (Timestamp)conditions[i]);
						updatestmt.setTimestamp(conds+columns, (Timestamp)conditions[i]);
						break;
					case Types.TIME:
					case Types.DATE:
						System.out.println("Setting oldvalue: " + (String)conditions[i]);
						updatestmt.setDate(conds+columns, Date.valueOf((String)conditions[i]));
						break;
					case Types.VARCHAR:
					case Types.LONGNVARCHAR:
					case Types.CHAR:
						System.out.println("Setting oldvalue: " + (String)conditions[i]);
						updatestmt.setString(conds+columns, (String)conditions[i]);
						break;
					}
					conds++;
					break;
				}
			}
		}*/
		updatestmt.execute();
	}
	
	public ResultSetMetaData getResultSetMetadata() throws SQLException{
		return mrs.getMetaData();
	}
}
