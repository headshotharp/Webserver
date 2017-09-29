package de.headshotharp.web.util;

import java.net.InetSocketAddress;
import java.net.Socket;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.headshotharp.web.Config;

@Component
public class ServerOnlineChecker {
	@Autowired
	private Config config;

	private boolean onlineStatus = false;

	@PostConstruct
	public void startChecker() {
		final String host = config.getBukkit().getHost();
		final int port = config.getBukkit().getPort();
		new Thread(new Runnable() {
			@Override
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

	public boolean isOnline() {
		return onlineStatus;
	}

	private boolean checkRemoteHost(String host, int port) {
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
