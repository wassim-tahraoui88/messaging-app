package com.tahraoui.messaging.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class NetworkUtils {

	public static final String SERVER_NAME = "localhost";

	public static boolean isValidIPAddress(String ipAddress) {
		try {
			var _ = InetAddress.getByName(ipAddress);
			return true;
		}
		catch (Exception _) {
			return false;
		}
	}

	public static int nextAvailablePort() {
		int port = 1;
		while (port <= 65535) {
			try  (var serverSocket = new ServerSocket(port)) {
				serverSocket.setReuseAddress(true);
				return port;
			}
			catch (IOException _) {
				System.out.printf("Port: %d is in use.\n", port);
				port++;
			}
		}
		return -1;
	}
	public static boolean isPortAvailable(int port) {
		if (port < 1 || port > 65535) return false;
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			serverSocket.setReuseAddress(true);
			return true;
		}
		catch (IOException _) {
			return false;
		}
	}
}
