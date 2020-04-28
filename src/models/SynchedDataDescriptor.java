package models;

import java.sql.Timestamp;
import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

public class SynchedDataDescriptor implements Serializable{
	
	private String dataTypeName;
	private String[] types;
	private String[] names;
	private boolean typesSet;
	private ArrayList<Object[]> data = null;
	
	public SynchedDataDescriptor(String dataTypeName, String[] types, String[] names, boolean set, ArrayList<Object[]> data) {
		this.dataTypeName = dataTypeName;
		this.types = types;
		this.names = names;
		this.typesSet = set;
		this.data = data;
	}
	
	public SynchedDataDescriptor(ResultSetMetaData rsmd, String tablename) throws SQLException{
		ArrayList<String> types = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		for(int i = 1; i <= rsmd.getColumnCount(); i++) {
			switch(rsmd.getColumnType(i)) {
			case Types.NUMERIC:
			case Types.INTEGER:
				types.add(Integer.class.getCanonicalName());
				break;
			case Types.DATE:
				types.add(java.sql.Date.class.getCanonicalName());
				break;
			case Types.TIMESTAMP:
			case Types.TIME:
				if(rsmd.getColumnTypeName(i).equals("DATE")) types.add(java.sql.Date.class.getCanonicalName());
				else types.add(Timestamp.class.getCanonicalName());
				break;
			case Types.VARCHAR:
			case Types.LONGNVARCHAR:
			case Types.CHAR:
				types.add(String.class.getCanonicalName());
				break;
			}
			names.add(rsmd.getColumnName(i));
		}
		if(types.size() > 0) this.dataTypeName = tablename;
		else throw new SQLException("Nincs oszlop az adott táblában!");
		this.typesSet = true;
		this.data = null;
		this.names = names.toArray(new String[0]);
		this.types = types.toArray(new String[0]);
	}

	public ArrayList<Object[]> getData() {
		return data;
	}

	public boolean areTypesSet() {
		return typesSet;
	}

	public String getDataTypeName() {
		return dataTypeName;
	}

	public String[] getNames() {
		return names;
	}

	public String[] getTypes() {
		return types;
	}

	@Override
	public String toString() {
		String ret = "SynchedDataDescriptor dataTypeName= "+this.dataTypeName + "datatypes: ";
		for(String s : this.types)
			ret = ret + s + ", ";
		return ret.substring(0, ret.length()-2);
	}
	
}
