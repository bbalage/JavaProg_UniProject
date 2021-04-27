package utilities;

public enum HelpOptions {
	
	ORACLE_LOGIN("::Oracle_login::"), DATABASE_HELP("::Database_help::"), FILE_HELP("::File_help::");
	
	private String optionName;
	
	private HelpOptions(String optionName) {
		this.optionName = optionName;
	}

	public String getOptionName() {
		return optionName;
	}
}
