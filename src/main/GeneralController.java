package main;

import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.JTable;

import utilities.MyAppException;

public class GeneralController {

	public static void setTablePreferredWidths(JTable jt, String[] types) {
		for(int i = 0; i < jt.getColumnCount(); i++) {
			if(types == null) {
				jt.getColumnModel().getColumn(i).setPreferredWidth(300);
			}
			else if(types[i].equals(Integer.class.getCanonicalName())) {
				jt.getColumnModel().getColumn(i).setPreferredWidth(120);
			}
			else if(types[i].equals(java.util.Date.class.getCanonicalName()) || types[i].equals(Timestamp.class.getCanonicalName()) || types[i].equals(java.sql.Date.class.getCanonicalName())) {
				jt.getColumnModel().getColumn(i).setPreferredWidth(200);
			}
			else if(types[i].equals(String.class.getCanonicalName())) {
				jt.getColumnModel().getColumn(i).setPreferredWidth(300);
			}
			else jt.getColumnModel().getColumn(i).setPreferredWidth(50);
		}
	}
	
	public static int getSelectedIndeces(JTable jt) throws MyAppException{
		int[] selected = jt.getSelectedRows();
		if(selected.length == 0) throw new MyAppException("No rows selected!");
		if(selected.length > 1) throw new MyAppException("Too many rows selected!");
		return selected[0];
	}
	
	public static String charsToString(char[] chs) {
		if(chs == null) return null;
		if(chs.length == 0) return null;
		StringBuilder strb = new StringBuilder();
		for(char c : chs) {
			strb.append(c);
		}
		return strb.toString();
	}
	
	public static Object[] getRow(JTable jt, int row) {
		ArrayList<Object> oblist = new ArrayList<Object>();
		for(int i = 0; i < jt.getColumnCount(); i++) {
			Object obj = jt.getValueAt(row, i);
			if(obj != null) obj = obj.toString();
			oblist.add(obj);
		}
		return oblist.toArray();
	}
	
}
