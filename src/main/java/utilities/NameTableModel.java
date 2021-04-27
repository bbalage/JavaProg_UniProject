package utilities;

import javax.swing.table.DefaultTableModel;

public class NameTableModel extends DefaultTableModel {

	public NameTableModel() {
		super(new Object[] {"Mezőnév", "Típus"}, 0);
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	@Override
	public Class<?> getColumnClass(int index){
		if(index <= 1) return String.class;
		else return String.class;
	}
}
