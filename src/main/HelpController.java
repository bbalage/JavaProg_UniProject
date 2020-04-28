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

	private ArrayList<HelpView> activeHelps = new ArrayList<HelpView>();
	private ArrayList<HelpOptions> activeOptions = new ArrayList<HelpOptions>();
	private HelpModel hm = new HelpModel();
	
	public void getHelpWindow(Window caller, HelpOptions ho) {
		if(hasActive(ho)) {
			sendMessage("Már van ilyen segítségkérés megnyitva!", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		HelpView hv;
		if(caller instanceof JFrame) hv = new HelpView((JFrame)caller, HelpController.this, ho);
		else hv = new HelpView((JDialog)caller, HelpController.this, ho);
		try {
			String helpText = hm.fetchHelpText(ho);
			hv.getTextManual().setText(helpText);
			hv.setVisible(true);
			activeHelps.add(hv);
		}
		catch(MyAppException exc) {
			sendMessage("Nem sikerült a segítség szövegét megszerezni!", JOptionPane.ERROR_MESSAGE);
		}
		catch(IOException exc) {
			sendMessage("Nem sikerült a segítség szövegét megszerezni!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void closeHelp(HelpView hv) {
		HelpOptions ho = hv.getHo();
		System.out.println("Closing help.");
		for(int i = 0; i < this.activeOptions.size(); i++) {
			System.out.println("Iterating in active options.");
			if(this.activeOptions.get(i) == ho) {
				System.out.println("Match in active options.");
				this.activeOptions.remove(i);
				break;
			}
		}
		for(int i = 0; i < this.activeHelps.size(); i++) {
			System.out.println("Iterating in active helps.");
			if(this.activeHelps.get(i) == hv) {
				System.out.println("Match in active helps.");
				this.activeHelps.remove(i);
				hv.dispose();
				break;
			}
		}
	}
	
	private boolean hasActive(HelpOptions ho) {
		for(HelpOptions hos : this.activeOptions) {
			if(hos == ho) return true;
		}
		return false;
	}
	
	public void sendMessage(String msg, int opt) {
		JOptionPane.showMessageDialog(null, msg, "Help kontroll üzenet.", opt);
	}
}
