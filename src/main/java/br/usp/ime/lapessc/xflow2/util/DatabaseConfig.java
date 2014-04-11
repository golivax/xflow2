package br.usp.ime.lapessc.xflow2.util;

public class DatabaseConfig {
	
	public String url;
	public String port;
	public String user;
	public String password;
	public String database;
	
	public DatabaseConfig(String url, String port, String user, 
			String password, String database) {
		
		this.url = url;
		this.port = port;
		this.user = user;
		this.password = password;
		this.database = database;
	}

	public String getUrl() {
		return url;
	}

	public String getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}
	
	
}