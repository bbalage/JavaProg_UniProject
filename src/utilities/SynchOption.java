package utilities;

public enum SynchOption {
	
	NONE("",-1), ORACLE("",-1), SQLITE("sqlite db",-1), XML("xml",1), JSON("json",2), CSV("csv",0), DAT("dat", 3);

	private String filename;
	private int filePolicyOpt;
	
	private SynchOption(String filename, int fpopt) {
		this.filename = filename;
		this.filePolicyOpt = fpopt;
	}

	public String getFilename() {
		return filename;
	}
	
	public int getFilePolicyOpt() {
		return filePolicyOpt;
	}
}
