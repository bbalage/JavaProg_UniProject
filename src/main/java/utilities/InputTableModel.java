package utilities;

import javax.swing.table.DefaultTableModel;

public class InputTableModel extends DefaultTableModel {

	public InputTableModel() {
		super();
	}
	
	public InputTableModel(Object[] fieldnames) {
		super(fieldnames, 1);
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}
	
	@Override
	public Class<?> getColumnClass(int index){
		return String.class;
	}
}
