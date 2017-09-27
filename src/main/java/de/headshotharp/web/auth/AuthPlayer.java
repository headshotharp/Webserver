package de.headshotharp.web.auth;

public class AuthPlayer {
	private String username, password;

	public AuthPlayer(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
