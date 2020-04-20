package main;

import javax.swing.JFrame;

import utilities.*;

public class SynchController {
	
	private SynchOption sOpt = SynchOption.NONE;
	private SynchPoll synchPoll = null;
	
	public void synchWithType(SynchOption sOpt) {
		if(sOpt == SynchOption.ORACLE) {
			synchWithOracle();
		}
	}
	
	public void startSynchSession(JFrame owner) {
		this.synchPoll = new SynchPoll(owner, SynchController.this);
		this.synchPoll.setVisible(true);
		
	}
	
	public void synchWithOracle() {
		
	}
	
}
