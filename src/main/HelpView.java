package main;


import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JTextArea;

import utilities.HelpOptions;

import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class HelpView extends JDialog {

	private Window caller;
	private JTextArea textManual;
	private HelpOptions ho;

	public HelpView(JDialog caller2, HelpController hc, HelpOptions ho) {
		super(caller2, "Help szöveg", true);
		helpViewConstruct(caller2, hc, ho);
	}
	
	public HelpView(JFrame caller, HelpController hc, HelpOptions ho){
		super(caller, "Help szöveg", true);
		helpViewConstruct(caller, hc, ho);
	}

	private void helpViewConstruct(Window caller, HelpController hc, HelpOptions ho) {
		Rectangle rec = caller.getBounds();
		int x = rec.x+rec.width+5;
		int y = rec.y+rec.height+5;
		this.caller = caller;
		this.ho = ho;
		setBounds(x, y, 450, 700);
		getContentPane().setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 32, 426, 550);
		getContentPane().add(scrollPane);
		
		textManual = new JTextArea();
		scrollPane.setViewportView(textManual);
		
		JButton btnClose = new JButton("Bezár");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hc.closeHelp(HelpView.this);
			}
		});
		btnClose.setBounds(306, 633, 117, 25);
		getContentPane().add(btnClose);
	}

	public HelpOptions getHo() {
		return ho;
	}

	public JTextArea getTextManual() {
		return textManual;
	}
	
	public Window getCaller() {
		return caller;
	}
}
