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
		rdbtnOracle.setBounds(18, 18, 162, 23);
		contentPanel.add(rdbtnOracle);
		
		JRadioButton rdbtnSQLite = new JRadioButton("SQLite adatbázis");
		rdbtnSQLite.setBounds(18, 49, 162, 23);
		contentPanel.add(rdbtnSQLite);
		
		ButtonGroup rdbtnGroup = new ButtonGroup();
		rdbtnGroup.add(rdbtnOracle);
		rdbtnGroup.add(rdbtnSQLite);
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
						
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						synchController.cancelSynchSession();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
