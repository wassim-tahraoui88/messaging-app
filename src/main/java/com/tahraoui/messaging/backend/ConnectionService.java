package com.tahraoui.messaging.backend;

import com.tahraoui.messaging.backend.client.Client;
import com.tahraoui.messaging.backend.client.UserCredentials;
import com.tahraoui.messaging.backend.client.ResponseReceiver;
import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.backend.host.Host;
import com.tahraoui.messaging.model.exception.AppException;
import com.tahraoui.messaging.ui.controller.ChatBoxListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

// Singleton pattern
public class ConnectionService implements ResponseReceiver {
	private static final Logger LOGGER = LogManager.getLogger(ConnectionService.class);

	private static ConnectionService instance;
	public static ConnectionService getInstance() {
		if (instance == null) instance = new ConnectionService();
		return instance;
	}

	private boolean hostState, clientState;
	private ChatBoxListener chatBoxListener;
	private String username;

	private ConnectionService() {}

	public String getUsername() { return username; }

	public void host(int port, UserCredentials credentials) {
		if (hostState) {
			LOGGER.warn("Host already started.");
			return;
		}

		var host = new Host(port, credentials);
//		host.setListener(this);
		new Thread(host,"Thread - Host").start();
		hostState = true;
		LOGGER.debug("Host started on port {}.", port);
	}
	public void join(int port, UserCredentials credentials) {
		if (clientState) {
			LOGGER.warn("Client already connected.");
			return;
		}
		try {
			var client = new Client(port, credentials);
			client.setListener(this);
			this.username = client.getUsername();
			clientState = true;
			LOGGER.debug("Connected to server on port {} with id {}.", port, client.getId());
		}
		catch (AppException e) {
			LOGGER.error(e.getMessage());
		}
		catch (IOException _) {
			LOGGER.fatal("An error has occurred while connecting.");
		}
	}

	@Override
	public void receiveMessage(MessageResponse response) {
		if (chatBoxListener != null) chatBoxListener.receiveMessage(response);
	}

	//region Setters
	public void setChatBoxControllerListener(ChatBoxListener listener) { chatBoxListener = listener; }


	//endregion
}
