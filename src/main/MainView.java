package main;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.CardLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class MainView extends JFrame {

	private JPanel contentPane;
	private JPanel cardPanel;
	private SynchController synchController;
	private JTable tableFieldNames;
	private JTable tableInput;
	private JTable tableOutput;
	private JComboBox<String> comboBoxTableNames;
	private JPanel databasePanel;
	private JButton btnSave;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainView frame = new MainView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainView() {
		this.synchController = new SynchController(MainView.this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 1024, 768);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		cardPanel = new JPanel();
		cardPanel.setBounds(0, 0, 1024, 768);
		contentPane.add(cardPanel);
		cardPanel.setLayout(new CardLayout(0, 0));
		
		JPanel homePanel = new JPanel();
		cardPanel.add(homePanel, "homePanel");
		homePanel.setLayout(null);
		
		JButton btnSynchronize = new JButton("Szinkronizálás");
		btnSynchronize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.startSynchSession();
			}
		});
		btnSynchronize.setBounds(36, 136, 144, 25);
		homePanel.add(btnSynchronize);
		
		JPanel tablePanel = new JPanel();
		cardPanel.add(tablePanel, "tablePanel");
		tablePanel.setLayout(null);
		
		databasePanel = new JPanel();
		databasePanel.setBounds(10, 0, 772, 221);
		tablePanel.add(databasePanel);
		databasePanel.setLayout(null);
		
		comboBoxTableNames = new JComboBox<String>();
		comboBoxTableNames.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					synchController.tableSelected();
				}
			}
		});
		comboBoxTableNames.setBounds(5, 30, 321, 24);
		databasePanel.add(comboBoxTableNames);
		
		JScrollPane scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(360, 30, 400, 132);
		databasePanel.add(scrollPane);
		
		
		tableFieldNames = new JTable();
		tableFieldNames.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane.setViewportView(tableFieldNames);
		
		JLabel lblVlasszonMezt = new JLabel("Válasszon mezőt!");
		lblVlasszonMezt.setBounds(360, 10, 321, 15);
		databasePanel.add(lblVlasszonMezt);
		
		JButton btnLoad = new JButton("Betöltés");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.loadTables();
			}
		});
		btnLoad.setBounds(500, 184, 120, 25);
		databasePanel.add(btnLoad);
		
		JLabel lblVlasszonTblt = new JLabel("Válasszon táblát!");
		lblVlasszonTblt.setBounds(5, 10, 202, 15);
		databasePanel.add(lblVlasszonTblt);
		
		JScrollPane scrollPane_1 = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane_1.setBounds(15, 233, 993, 60);
		tablePanel.add(scrollPane_1);
		
		tableInput = new JTable();
		tableInput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane_1.setViewportView(tableInput);
		
		JScrollPane scrollPane_2 = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane_2.setBounds(15, 300, 993, 358);
		tablePanel.add(scrollPane_2);
		
		tableOutput = new JTable();
		tableOutput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane_2.setViewportView(tableOutput);
		
		JButton btnInsert = new JButton("Felvisz");
		btnInsert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.dataModifyingAction(0);
			}
		});
		btnInsert.setBounds(814, 20, 140, 25);
		tablePanel.add(btnInsert);
		
		JButton btnUpdate = new JButton("Módosít");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.dataModifyingAction(1);
			}
		});
		btnUpdate.setBounds(814, 60, 140, 25);
		tablePanel.add(btnUpdate);
		
		JButton btnDelete = new JButton("Töröl");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.dataModifyingAction(2);
			}
		});
		btnDelete.setBounds(814, 100, 140, 25);
		tablePanel.add(btnDelete);
		
		JButton btnKapcsolatotLezr = new JButton("Kapcsolatot lezár");
		btnKapcsolatotLezr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.closeSynchSession();
			}
		});
		btnKapcsolatotLezr.setBounds(794, 173, 180, 25);
		tablePanel.add(btnKapcsolatotLezr);
		
		JButton btnSaveAs = new JButton("Mentés másként");
		btnSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.saveAs();
			}
		});
		btnSaveAs.setBounds(25, 678, 203, 25);
		tablePanel.add(btnSaveAs);
		
		JButton btnHelp = new JButton("Help");
		btnHelp.setBounds(891, 678, 117, 25);
		tablePanel.add(btnHelp);
		
		btnSave = new JButton("Mentés");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.save();
			}
		});
		btnSave.setBounds(50, 173, 170, 25);
		tablePanel.add(btnSave);
	}
	
	void switchToTableCard() {
		CardLayout cl = (CardLayout)cardPanel.getLayout();
		cl.show(cardPanel, "tablePanel");
	}
	
	void switchToHomeCard() {
		CardLayout cl = (CardLayout)cardPanel.getLayout();
		cl.show(cardPanel, "homePanel");
	}
	
	JComboBox<String> getComboBoxTableNames() {
		return comboBoxTableNames;
	}
	
	void dbPanel(boolean vis) {
		databasePanel.setVisible(vis);
	}
	
	JTable getTableFieldNames() {
		return tableFieldNames;
	}

	JTable getTableInput() {
		return tableInput;
	}

	JTable getTableOutput() {
		return tableOutput;
	}
	
	public JButton getBtnSave() {
		return btnSave;
	}
}
