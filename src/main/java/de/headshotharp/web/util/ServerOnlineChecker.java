package de.headshotharp.web.util;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

import de.headshotharp.web.data.DataProvider;

public class ServerOnlineChecker {
	public static boolean onlineStatus = false;

	private static String host = "";
	private static int port = -1;

	public static void startChecker() {
		Properties prop = DataProvider.getProperties();
		host = prop.getProperty("bukkit.host");
		port = Integer.parseInt(prop.getProperty("bukkit.port"));
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					onlineStatus = checkRemoteHost(host, port);
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public static boolean isOnline() {
		return onlineStatus;
	}

	static boolean checkRemoteHost(String host, int port) {
		Socket socket = null;
		boolean check = false;
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), 2000);
			check = true;
		} catch (Exception e) {
			check = false;
		}
		try {
			socket.close();
		} catch (Exception e) {

		}
		return check;
	}
}
