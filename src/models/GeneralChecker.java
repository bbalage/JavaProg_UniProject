package models;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import utilities.MyAppException;

public class GeneralChecker {
	
	private SimpleDateFormat[] sdfs = new SimpleDateFormat[4];
	private SimpleDateFormat[] sdfts = new SimpleDateFormat[8];
	
	{
		this.sdfs[0] = new SimpleDateFormat("yyyy.MM.dd");
		this.sdfs[1] = new SimpleDateFormat("yyyy.MM.dd.");
		this.sdfs[2] = new SimpleDateFormat("yyyy/MM/dd");
		this.sdfs[3] = new SimpleDateFormat("yyyy-MM-dd");
		this.sdfts[0] = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.SS");
		this.sdfts[1] = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.S");
		this.sdfts[2] = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
		this.sdfts[3] = new SimpleDateFormat("yyyy.MM.dd hh:mm");
		this.sdfts[4] = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		this.sdfts[5] = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		this.sdfts[6] = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		this.sdfts[7] = new SimpleDateFormat("yyyy/MM/dd hh:mm");
	}
	
	public Object[] formatRow(Object[] row, String[] types) throws MyAppException{
		Object[] ret = new Object[row.length];
		for(int i = 0; i < row.length; i++) {
			if(row[i] == null) ret[i] = null;
			else if(row[i].getClass().getCanonicalName().equals(types[i])) {
				ret[i] = row[i];
			}
			else if(row[i] instanceof String) {
				if(types[i].equals(Integer.class.getCanonicalName())) try{
					ret[i] = Integer.parseInt(row[i].toString());
				}
				catch(NumberFormatException exc) {
					throw new MyAppException("Could not convert to number: "+exc.getMessage());
				}
				else if(types[i].equals(String.class.getCanonicalName())) ret[i] = row[i];
				else if(types[i].equals(Timestamp.class.getCanonicalName())) {ret[i] = convertToTimestamp(row[i].toString()); System.out.println("Timestamp string: "+row[i]+"; timestamp: "+ret[i]);}
				else if(types[i].equals(java.util.Date.class.getCanonicalName())) {ret[i] = convertToDate(row[i].toString()); System.out.println("Date string: "+row[i]+"; date: "+ret[i]);}
				else if(types[i].equals(java.sql.Date.class.getCanonicalName())) ret[i] = convertToSQLDate(row[i].toString());
				else throw new MyAppException("Type of the row was not the application can convert: "+types[i] + " at "+ i);
			}
			else if(row[i] instanceof BigDecimal) ret[i] = ((BigDecimal)row[i]).intValue();
			else if(row[i] instanceof Timestamp && types[i].equals(java.sql.Date.class.getCanonicalName())) 
			{
				ret[i] = new java.sql.Date(((Timestamp)row[i]).getTime());
				System.out.println("Timestamp: "+row[i]+"; sqldate: "+ret[i]);}
			else if(row[i] instanceof Timestamp && types[i].equals(java.util.Date.class.getCanonicalName()))
			{
				ret[i] = new java.util.Date(((Timestamp)row[i]).getTime());
				System.out.println("Timestamp: "+row[i]+"; utildate: "+ret[i]);}
			//else if(row[i] instanceof Long && cls[i].equals(java.util.Date.class)) {ret[i] = new java.util.Date(((Timestamp)row[i]).getTime());}
			else if(row[i] instanceof oracle.sql.TIMESTAMP && types[i].equals(java.sql.Timestamp.class.getCanonicalName())) {
				try {
					ret[i] = new java.sql.Timestamp(((oracle.sql.TIMESTAMP)row[i]).dateValue().getTime());
					System.out.println("Oracle timestamp: "+row[i]+"; timestamp: "+ret[i]);
				}
				catch(SQLException exc) {
					throw new MyAppException("Conversion of Oracle timestamp failed: "+exc.getMessage());
				}
			}
			else if(row[i] instanceof Long && types[i].equals(Integer.class.getCanonicalName())) {
				try {
					ret[i] = (Integer)row[i];
				}
				catch(ClassCastException exc) {
					throw new MyAppException("Could not cast Long into Integer: "+exc.getMessage());
				}
			}
			else throw new MyAppException("Input type was none of types we are ready to handle: "+row[i].getClass().getCanonicalName() + " at "+ i);
		}
		return ret;
	}
	
	public void checkIfConvertable(Object[] row, String[] types) throws MyAppException{
		formatRow(row, types);
	}
	
	public Timestamp convertToTimestamp(String text) throws MyAppException{
		java.util.Date dt = null;
		for(int i = 0; i < sdfts.length; i++) {
			try {
				dt = sdfts[i].parse(text);
				break;
			}
			catch(ParseException exc) {
				continue;
			}
		}
		if(dt != null) return new Timestamp(dt.getTime());
		for(int i = 0; i < sdfs.length; i++) {
			try {
				dt = sdfs[i].parse(text);
				break;
			}
			catch(ParseException exc) {
				continue;
			}
		}
		if(dt == null) throw new MyAppException("Could not convert to timestamp.");
		else return new Timestamp(dt.getTime());
	}
	
	public java.util.Date convertToDate(String text) throws MyAppException{
		java.util.Date date = null;
		for(int i = 0; i < sdfs.length; i++) {
			try {
				date = sdfs[i].parse(text);
				break;
			}
			catch(ParseException exc) {
				continue;
			}
		}
		if(date == null) throw new MyAppException("Could not convert to date.");
		else return date;
	}
	
	public java.sql.Date convertToSQLDate(String text) throws MyAppException{
		java.util.Date dt = convertToDate(text);
		return new java.sql.Date(dt.getTime());
	}
}
