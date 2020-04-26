package main;

import java.sql.Timestamp;

import javax.swing.JTable;

public class GeneralController {

	public static void setTablePreferredWidths(JTable jt, String[] types) {
		for(int i = 0; i < types.length; i++) {
			if(types[i].equals(Integer.class.getCanonicalName())) {
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
}
