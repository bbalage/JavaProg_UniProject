package main;

import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.JTable;

import utilities.InputTableModel;
import utilities.MyAppException;

public class GeneralController {

	public static void fetchToInput(JTable input, JTable output) throws MyAppException{
		int selected = getSelectedIndeces(output);
		Object[] row = getRow(output, selected);
		InputTableModel itm = (InputTableModel)input.getModel();
		for(int i = 0; i < input.getColumnCount(); i++) {
			itm.setValueAt(row[i], 0, i);
		}
	}
	
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
		if(selected.length == 0) throw new MyAppException("Nem volt sor kiválasztva!");
		if(selected.length > 1) throw new MyAppException("Túl sok sor kiválasztva! (egyszerre egyet lehet)");
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
