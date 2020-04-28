package models;

import java.io.*;

import utilities.HelpOptions;
import utilities.MyAppException;

public class HelpModel {
	
	private String separator = System.getProperty("file.separator");
	private String workingDir = System.getProperty("user.dir");
	
	public String fetchHelpText(HelpOptions ho) throws IOException, MyAppException{
		String path = this.workingDir+separator+"files"+separator+"helpfile.txt";
		StringBuilder helpText = new StringBuilder();
		LineNumberReader in = new LineNumberReader(new InputStreamReader(new FileInputStream(path)));
		String inline;
		String helpTitle = ho.getOptionName();
		do {
			inline = in.readLine();
			if(inline == null) {
				in.close();
				throw new MyAppException("Nem volt illeszkedő rész a szövegfájlban.");
			}
			if(inline.equals(helpTitle)) break;
		}
		while(true);
		do {
			inline = in.readLine();
			if(inline == null) break;
			if(inline.length()>=2) {
				if(inline.substring(0, 2).equals("::")) break;
			}
			helpText.append(inline+"\n");
		}
		while(true);
		in.close();
		return helpText.toString();
	}
	
}
