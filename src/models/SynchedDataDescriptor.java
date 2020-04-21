package models;

import java.sql.Timestamp;
import java.util.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

public class SynchedDataDescriptor {
	
	private ArrayList<Class<?>> types;
	private ArrayList<String> names;
	//private ArrayList<Integer> preferredWidths;
	
	public SynchedDataDescriptor(ArrayList<Class<?>> types, ArrayList<String> names) {
		this.types = types;
		this.names = names;
	}
	
	public SynchedDataDescriptor(ResultSetMetaData rsmd) throws SQLException{
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
	}

	public ArrayList<String> getNames() {
		return names;
	}

	public ArrayList<Class<?>> getTypes() {
		return types;
	}
	
}
