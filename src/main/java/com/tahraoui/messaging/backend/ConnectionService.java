package com.tahraoui.messaging.backend;

import com.tahraoui.messaging.backend.client.Client;
import com.tahraoui.messaging.backend.host.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ConnectionService {
	private static final Logger LOGGER = LogManager.getLogger(ConnectionService.class.getName());

	private static boolean HOST_STATE, CLIENT_STATE;

	public static void host(int port, String password) {
		if (HOST_STATE) {
			LOGGER.warn("Host already started.");
			return;
		}

		var host = new Host(port, password);
		new Thread(host).start();
		LOGGER.debug("Host started on port {}", port);
		HOST_STATE = true;
	}
	public static void join(int port, String password) {
		try {
			var client = new Client(port, password);
		}
		catch (IOException _) {

		}
	}
}
