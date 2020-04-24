package models;

import java.sql.Timestamp;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

public class SynchedDataDescriptor {
	
	private String dataTypeName;
	private ArrayList<Class<?>> types;
	private ArrayList<String> names;
	private boolean typesSet;
	//private ArrayList<Integer> preferredWidths;
	
	public SynchedDataDescriptor(String dataTypeName, ArrayList<Class<?>> types, ArrayList<String> names, boolean set) {
		this.dataTypeName = dataTypeName;
		this.types = types;
		this.names = names;
		this.typesSet = set;
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
				types.add(java.sql.Date.class);
				break;
			case Types.TIMESTAMP:
			case Types.TIME:
				if(rsmd.getColumnTypeName(i).equals("DATE")) types.add(java.sql.Date.class);
				else types.add(Timestamp.class);
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
		this.typesSet = true;
		System.out.println(this);
	}

	public boolean areTypesSet() {
		return typesSet;
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

	@Override
	public String toString() {
		String ret = "SynchedDataDescriptor dataTypeName= "+this.dataTypeName + "datatypes: ";
		for(Class<?> cls : this.types)
			ret = ret + cls.getSimpleName() + ", ";
		return ret.substring(0, ret.length()-2);
	}
	
}
