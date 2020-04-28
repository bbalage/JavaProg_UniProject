package main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import utilities.*;

public class SynchPoll extends JDialog {

	private final JPanel contentPanel = new JPanel();

	
	
	public SynchPoll(JFrame owner, SynchController synchController) {
		super(owner, "Szinkronizációs objektum kiválasztása", true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JRadioButton rdbtnOracle = new JRadioButton("Oracle adatbázis");
		rdbtnOracle.setBounds(20, 20, 162, 23);
		contentPanel.add(rdbtnOracle);
		
		JRadioButton rdbtnSQLite = new JRadioButton("SQLite adatbázis");
		rdbtnSQLite.setBounds(20, 50, 162, 23);
		contentPanel.add(rdbtnSQLite);
		
		JRadioButton rdbtnXml = new JRadioButton("Lokális xml fájl");
		rdbtnXml.setBounds(20, 80, 149, 23);
		contentPanel.add(rdbtnXml);

		JRadioButton rdbtnJson = new JRadioButton("Lokális json fájl");
		rdbtnJson.setBounds(20, 110, 149, 23);
		contentPanel.add(rdbtnJson);
		
		JRadioButton rdbtnCsv = new JRadioButton("Lokális csv fájl");
		rdbtnCsv.setBounds(20, 140, 149, 23);
		contentPanel.add(rdbtnCsv);
		
		JRadioButton rdbtnDat = new JRadioButton("Lokális dat fájl");
		rdbtnDat.setBounds(20, 170, 149, 23);
		contentPanel.add(rdbtnDat);
		
		ButtonGroup rdbtnGroup = new ButtonGroup();
		rdbtnGroup.add(rdbtnOracle);
		rdbtnGroup.add(rdbtnSQLite);
		rdbtnGroup.add(rdbtnXml);
		rdbtnGroup.add(rdbtnJson);
		rdbtnGroup.add(rdbtnCsv);
		rdbtnGroup.add(rdbtnDat);
		rdbtnOracle.setSelected(true);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(rdbtnOracle.isSelected()) synchController.synchWithType(SynchOption.ORACLE);
						else if (rdbtnSQLite.isSelected()) synchController.synchWithType(SynchOption.SQLITE);
						else if (rdbtnXml.isSelected()) synchController.synchWithType(SynchOption.XML);
						else if(rdbtnJson.isSelected()) synchController.synchWithType(SynchOption.JSON);
						else if(rdbtnCsv.isSelected()) synchController.synchWithType(SynchOption.CSV);
						else if(rdbtnDat.isSelected()) synchController.synchWithType(SynchOption.DAT);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Mégse");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						synchController.cancelSynchSession();
					}
				});
				cancelButton.setActionCommand("Mégse");
				buttonPane.add(cancelButton);
			}
		}
	}
}
