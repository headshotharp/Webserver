package de.headshotharp.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@PropertySources({ @PropertySource("classpath:/application.properties"),
		@PropertySource("file:application.properties") })
public class Config {
	private final Path path = new Path();
	private final Bukkit bukkit = new Bukkit();
	private final Mysql mysql = new Mysql();

	public Path getPath() {
		return path;
	}

	public Bukkit getBukkit() {
		return bukkit;
	}

	public Mysql getMysql() {
		return mysql;
	}

	public static class Mysql {
		private String host;
		private int port;
		private String database;
		private String username;
		private String password;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getDatabase() {
			return database;
		}

		public void setDatabase(String database) {
			this.database = database;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	public static class Bukkit {
		private String host;
		private int port;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	}

	public static class Path {
		private String skins;
		private String external;
		private String upload;
		private String permissions;

		public String getSkins() {
			return skins;
		}

		public void setSkins(String skins) {
			this.skins = skins;
		}

		public String getExternal() {
			return external;
		}

		public void setExternal(String external) {
			this.external = external;
		}

		public String getUpload() {
			return upload;
		}

		public void setUpload(String upload) {
			this.upload = upload;
		}

		public String getPermissions() {
			return permissions;
		}

		public void setPermissions(String permissions) {
			this.permissions = permissions;
		}
	}
}
