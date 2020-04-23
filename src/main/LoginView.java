package main;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;

public class LoginView extends JDialog {
	private JTextField textURL;
	private JTextField textUsername;
	private JButton btnLogin;
	private JButton btnCancel;
	private JButton btnHelp;
	private JCheckBox checkBoxDefaultServer;
	private JPasswordField passwordField;
	private JTextField textWorkspace;
	private JCheckBox checkBoxWorkspace;

	public JCheckBox getCheckBoxWorkspace() {
		return checkBoxWorkspace;
	}

	JTextField getTextURL() {
		return textURL;
	}

	JTextField getTextUsername() {
		return textUsername;
	}

	JCheckBox getCheckBoxDefaultServer() {
		return checkBoxDefaultServer;
	}

	JPasswordField getPasswordField() {
		return passwordField;
	}

	/**
	 * Create the dialog.
	 */
	public LoginView(JDialog owner, SynchController synchController) {
		super(owner, "Bejelentkezés Oracle adatbázisba.", true);
		setBounds(100, 100, 450, 450);
		getContentPane().setLayout(null);
		
		textURL = new JTextField();
		textURL.setBounds(41, 35, 347, 19);
		getContentPane().add(textURL);
		textURL.setColumns(10);
		
		textUsername = new JTextField();
		textUsername.setBounds(41, 120, 279, 19);
		textUsername.setText("H20_N5IF3V");
		getContentPane().add(textUsername);
		textUsername.setColumns(10);
		
		JLabel lblUrl = new JLabel("Adatbázis azonosító (URL és leírók):");
		lblUrl.setBounds(41, 10, 279, 15);
		getContentPane().add(lblUrl);
		
		JLabel lblFelhasznlnv = new JLabel("Felhasználónév:");
		lblFelhasznlnv.setBounds(41, 95, 279, 15);
		getContentPane().add(lblFelhasznlnv);
		
		JLabel lblJelsz = new JLabel("Jelszó:");
		lblJelsz.setBounds(41, 258, 279, 15);
		getContentPane().add(lblJelsz);
		
		btnLogin = new JButton("Bejelentkezés");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.loginToOracle();
			}
		});
		btnLogin.setBounds(41, 330, 149, 25);
		getContentPane().add(btnLogin);
		
		btnCancel = new JButton("Mégse");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchController.cancelSynchWithOracle();
			}
		});
		btnCancel.setBounds(234, 330, 149, 25);
		getContentPane().add(btnCancel);
		
		checkBoxDefaultServer = new JCheckBox("Használd az alapértelmezett Oracle szervert!");
		checkBoxDefaultServer.setBounds(41, 60, 387, 23);
		getContentPane().add(checkBoxDefaultServer);
		
		btnHelp = new JButton("Help");
		btnHelp.setBounds(266, 383, 117, 25);
		getContentPane().add(btnHelp);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(41, 283, 175, 19);
		getContentPane().add(passwordField);
		
		JLabel lblWorkspace = new JLabel("Workspace:");
		lblWorkspace.setBounds(41, 145, 117, 15);
		getContentPane().add(lblWorkspace);
		
		textWorkspace = new JTextField();
		textWorkspace.setColumns(10);
		textWorkspace.setBounds(41, 170, 279, 19);
		getContentPane().add(textWorkspace);
		
		checkBoxWorkspace = new JCheckBox("<html>Használd a felhasználónevet workspace</br>\nazonosítónak!</html>");
		checkBoxWorkspace.setBounds(41, 190, 387, 48);
		getContentPane().add(checkBoxWorkspace);

	}

	public JTextField getTextWorkspace() {
		return textWorkspace;
	}
}
