package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class AccessCredentials {

	public AccessCredentials(){
		
	}
	
	public AccessCredentials(String username, String password){
		this.username = username;
		this.password = password;
	}
	
	@Column(name = "PRJ_USER", nullable = false)
	private String username;
	
	@Column(name = "PRJ_PASSWD", nullable = false)
	private String password;

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	
}
