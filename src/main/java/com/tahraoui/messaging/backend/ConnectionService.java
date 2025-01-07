package com.tahraoui.messaging.backend;

import com.tahraoui.messaging.backend.client.Client;
import com.tahraoui.messaging.backend.host.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ConnectionService {
	private static final Logger LOGGER = LogManager.getLogger(ConnectionService.class.getName());

	public static void host(int port, String password) {
		var host = new Host(port, password);
		LOGGER.debug("Host started on port {}", port);

	}
	public static void join(int port, String password) {
		try {
			var client = new Client(port, password);
		}
		catch (IOException _) {

		}
	}
}
