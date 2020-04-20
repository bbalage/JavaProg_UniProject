package utilities;

import javax.swing.table.DefaultTableModel;

public class NameTableModel extends DefaultTableModel {

	public NameTableModel() {
		super(new Object[] {"Lekér", "Mezőnév"}, 0);
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		if(col == 0) return true;
		else return false;
	}
	
	@Override
	public Class<?> getColumnClass(int index){
		if(index == 0) return Boolean.class;
		else return String.class;
	}
}
