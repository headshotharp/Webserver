package de.headshotharp.web.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MysqlConnector {
	private static boolean init = false;
	private static String ip = "";
	private static String dbname = "";
	private static String username = "";
	private static String password = "";
	private static String port = "";

	public static Connection getConnection() {
		if (!init) {
			Properties prop = DataProvider.getProperties();
			ip = prop.getProperty("mysql.host");
			port = prop.getProperty("mysql.port");
			dbname = prop.getProperty("mysql.database");
			username = prop.getProperty("mysql.user");
			password = prop.getProperty("mysql.password");
			init = true;
		}
		try {
			return DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbname
					+ "?autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8",
					username, password);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
