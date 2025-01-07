package com.tahraoui.messaging.backend;

import com.tahraoui.messaging.backend.client.Client;
import com.tahraoui.messaging.backend.host.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.security.InvalidKeyException;

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
		HOST_STATE = true;
		LOGGER.debug("Host started on port {}.", port);
	}
	public static void join(int port, String password) {
		if (CLIENT_STATE) {
			LOGGER.warn("Client already connected.");
			return;
		}
		try {
			var client = new Client(port, password);
			CLIENT_STATE = true;
			LOGGER.debug("Connected to server on port {} with id {}.", port, client.getId());
		}
		catch (ConnectException _) {
			LOGGER.error("Connection failed.");
		}
		catch (InvalidKeyException e) {
			LOGGER.error("Wrong password.");
		}
		catch (IOException _) {
			LOGGER.fatal("An error has occurred while connecting.");
		}

	}
}
