package de.headshotharp.web.auth;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthPlayer {
	private String username, password;

	public AuthPlayer(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public AuthPlayer(ResultSet rs) throws SQLException {
		this(rs.getString("name"), rs.getString("password"));
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
