package models;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseAPI {

	private Connection conn; //193.6.5.58:1521:XE
	
	/*public void connectToOracle(String username, String password) throws SQLException, ClassNotFoundException{
		connectToOracle(username, password, "193.6.5.58:1521:XE");
	}*/
	
	public void connectToOracle(String username, String password, String URL) throws SQLException, ClassNotFoundException{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		conn = DriverManager.getConnection("jdbc:oracle:thin:@"+URL,username,password);
		//simpleQuery = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
	
	public String[] getTableNames() throws SQLException{
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getTables(null, "H20_N5IF3V", "%", null);
		ArrayList<String> tableNameList = new ArrayList<String>();
		while(rs.next()) {
			tableNameList.add(rs.getString("TABLE_NAME"));
		}
		return tableNameList.toArray(new String[0]);
	}
}
