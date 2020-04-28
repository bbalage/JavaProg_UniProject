package main;

import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.*;

import models.HelpModel;
import utilities.HelpOptions;
import utilities.MyAppException;

public class HelpController {

	
	private HelpModel hm = new HelpModel();
	
	public void getHelpWindow(Window caller, HelpOptions ho) {
		
		HelpView hv;
		if(caller instanceof JFrame) hv = new HelpView((JFrame)caller, HelpController.this, ho);
		else hv = new HelpView((JDialog)caller, HelpController.this, ho);
		try {
			String helpText = hm.fetchHelpText(ho);
			hv.getTextManual().setText(helpText);
			hv.setVisible(true);
		}
		catch(MyAppException exc) {
			sendMessage("Nem sikerült a segítség szövegét megszerezni!", JOptionPane.ERROR_MESSAGE);
		}
		catch(IOException exc) {
			sendMessage("Nem sikerült a segítség szövegét megszerezni!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void closeHelp(HelpView hv) {
		hv.dispose();
	}
	
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Help kontroll üzenet.", opt);
	}
}
