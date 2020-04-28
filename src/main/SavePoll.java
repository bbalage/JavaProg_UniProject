package main;


import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

public class SavePoll extends JDialog {
	private JTextField textFileName;

	
	public SavePoll(JFrame owner, FileController fc) {
		super(owner, "Fájltípus választás", true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(null);
		
		JLabel lblVlasszaKiMilyen = new JLabel("Válassza ki, milyen típusú fájlba kíván menteni!");
		lblVlasszaKiMilyen.setBounds(12, 12, 416, 27);
		getContentPane().add(lblVlasszaKiMilyen);
		
		JButton btnSave = new JButton(" Mentés");
		
		btnSave.setBounds(12, 233, 117, 25);
		getContentPane().add(btnSave);
		
		JButton btnCancel = new JButton("Mégse");
		
		btnCancel.setBounds(311, 233, 117, 25);
		getContentPane().add(btnCancel);
		
		ButtonGroup btg = new ButtonGroup();
		
		JRadioButton rdbtnCsv = new JRadioButton("csv fájlba");
		rdbtnCsv.setBounds(12, 50, 149, 23);
		getContentPane().add(rdbtnCsv);
		
		JRadioButton rdbtnXml = new JRadioButton("xml fájlba");
		rdbtnXml.setBounds(12, 80, 149, 23);
		getContentPane().add(rdbtnXml);
		
		textFileName = new JTextField();
		textFileName.setBounds(203, 187, 225, 19);
		getContentPane().add(textFileName);
		textFileName.setColumns(10);
		
		JLabel lblFjlnv = new JLabel("Fájlnév:");
		lblFjlnv.setBounds(208, 156, 220, 15);
		getContentPane().add(lblFjlnv);
		
		JRadioButton rdbtnJson = new JRadioButton("json fájlba");
		rdbtnJson.setBounds(12, 110, 149, 23);
		getContentPane().add(rdbtnJson);
		
		btg.add(rdbtnCsv);
		btg.add(rdbtnXml);
		btg.add(rdbtnJson);
		rdbtnXml.setSelected(true);
		
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int opt;
				if(rdbtnCsv.isSelected()) opt = 0;
				else if(rdbtnXml.isSelected()) opt = 1;
				else if(rdbtnJson.isSelected()) opt = 2;
				else opt = -1;
				fc.saveAsFile(opt, textFileName.getText());
			}
		});
		
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.cancelSaveAs();
			}
		});
	}
}
