package main;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.CardLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainView extends JFrame {

	private JPanel contentPane;
	private SynchController synchController = new SynchController();
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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 1024, 768);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel cardPanel = new JPanel();
		cardPanel.setBounds(0, 0, 1024, 768);
		contentPane.add(cardPanel);
		cardPanel.setLayout(new CardLayout(0, 0));
		
		JPanel homePanel = new JPanel();
		cardPanel.add(homePanel, "homePanel");
		homePanel.setLayout(null);
		
		JButton btnSynchronize = new JButton("Szinkronizálás");
		btnSynchronize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.startSynchSession(MainView.this);
			}
		});
		btnSynchronize.setBounds(36, 136, 144, 25);
		homePanel.add(btnSynchronize);
		
		JPanel tablePanel = new JPanel();
		cardPanel.add(tablePanel, "tablePanel");
		tablePanel.setLayout(null);
	}
}
