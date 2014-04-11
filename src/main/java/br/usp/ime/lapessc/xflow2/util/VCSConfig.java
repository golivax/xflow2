package br.usp.ime.lapessc.xflow2.util;

public class VCSConfig {
	
	public String url;
	public String user;
	public String password;

	public VCSConfig(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
}