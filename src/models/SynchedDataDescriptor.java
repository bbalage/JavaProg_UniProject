package models;

import java.sql.Timestamp;
import java.util.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

public class SynchedDataDescriptor {
	
	private String dataTypeName;
	private ArrayList<Class<?>> types;
	private ArrayList<String> names;
	//private ArrayList<Integer> preferredWidths;
	
	public SynchedDataDescriptor(String dataTypeName, ArrayList<Class<?>> types, ArrayList<String> names) {
		this.dataTypeName = dataTypeName;
		this.types = types;
		this.names = names;
	}
	
	public SynchedDataDescriptor(ResultSetMetaData rsmd, String tablename) throws SQLException{
		this.types = new ArrayList<Class<?>>();
		this.names = new ArrayList<String>();
		for(int i = 1; i <= rsmd.getColumnCount(); i++) {
			switch(rsmd.getColumnType(i)) {
			case Types.NUMERIC:
			case Types.INTEGER:
				types.add(Integer.class);
				break;
			case Types.DATE:
				types.add(Date.class);
				break;
			case Types.TIMESTAMP:
			case Types.TIME:
				types.add(Timestamp.class);
				break;
			case Types.VARCHAR:
			case Types.LONGNVARCHAR:
			case Types.CHAR:
				types.add(String.class);
				break;
			}
			this.names.add(rsmd.getColumnName(i));
		}
		if(this.types.size() > 0) this.dataTypeName = tablename;
		else throw new SQLException("No columns in the given table!");
	}

	public String getDataTypeName() {
		return dataTypeName;
	}

	public ArrayList<String> getNames() {
		return names;
	}

	public ArrayList<Class<?>> getTypes() {
		return types;
	}
	
}
