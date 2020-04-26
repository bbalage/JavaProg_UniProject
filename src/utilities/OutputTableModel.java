package utilities;

import javax.swing.table.DefaultTableModel;

public class OutputTableModel extends DefaultTableModel {

	//Class<?>[] types;
	
	public OutputTableModel(Object[] fieldnames, int rows) {
		super(fieldnames, rows);
		//this.types = types;
	}

	public OutputTableModel(Object[] fieldnames) {
		super(fieldnames, 0);
		//this.types = types;
	}
	
	public OutputTableModel() {
		super();
	}
	
	/*@Override
	public Class<?> getColumnClass(int index){
		return types[index];
	}*/
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
}
