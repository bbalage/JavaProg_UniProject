package utilities;

import javax.swing.table.DefaultTableModel;

public class OutputTableModel extends DefaultTableModel {

	public OutputTableModel(Object[] fieldnames, int rows) {
		super(fieldnames, rows);
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
}
