package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAPI {

	private Connection conn; //193.6.5.58:1521:XE
	
	public void connectToOracle(String username, String password) throws SQLException, ClassNotFoundException{
		connectToOracle(username, password, "193.6.5.58:1521:XE");
	}
	
	public void connectToOracle(String username, String password, String URL) throws SQLException, ClassNotFoundException{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		conn = DriverManager.getConnection("jdbc:oracle:thin:@"+URL,username,password);
		//simpleQuery = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}
	
	public void closeConnection() throws SQLException{
		if(conn != null) {
			//conn.commit();
			//this.insertrsmd = null;
			//this.insertstmt = null;
			//this.mrs = null;
			//this.simpleQuery = null;
			conn.close();
		}
	}
}
