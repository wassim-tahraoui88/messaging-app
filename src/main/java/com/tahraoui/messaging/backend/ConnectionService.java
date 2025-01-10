package com.tahraoui.messaging.backend;

import com.tahraoui.messaging.backend.client.Client;
import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.ResponseReader;
import com.tahraoui.messaging.model.UserCredentials;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.KickResponse;
import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import com.tahraoui.messaging.backend.data.response.SystemMessageResponse;
import com.tahraoui.messaging.backend.host.Host;
import com.tahraoui.messaging.model.exception.AppException;
import com.tahraoui.messaging.ui.controller.SidebarController;
import com.tahraoui.messaging.ui.listener.ChatBoxListener;
import com.tahraoui.messaging.ui.listener.ConnectionListener;
import com.tahraoui.messaging.ui.listener.NavigationListener;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ConnectionService implements RequestWriter, ResponseReader {

	private static final Logger LOGGER = LogManager.getLogger(ConnectionService.class);

	private static ConnectionService instance;
	public static ConnectionService getInstance() {
		if (instance == null) instance = new ConnectionService();
		return instance;
	}

	private final Navigator navigator;

	private int id;
	private String username;
	public boolean isHost;
	private boolean isClient;
	private RequestWriter requestWriter;
	private ChatBoxListener chatBoxListener;
	private ConnectionListener connectionListener;

	private ConnectionService() {
		this.navigator = new Navigator();

	}

	//region Host/Client
	public void host(int port, UserCredentials credentials) {
		if (isConnected()) {
			LOGGER.warn("Host already started.");
			return;
		}

		try {
			var host = new Host(port, credentials);
			host.setResponseReader(this);
			this.id = 0;
			this.username = host.getUsername();
			this.requestWriter = host;
			host.setConnectionListener(connectionListener);
			new Thread(host,"Thread - Host").start();
		}
		catch (GeneralSecurityException e) {
			disconnect();
			LOGGER.error(e.getMessage());
			return;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			disconnect();
			LOGGER.fatal("An error has occurred while starting the host.");
			return;
		}

		this.isHost = true;
		this.navigator.switchToChatbox();
		LOGGER.info("Host started on port {}.", port);
	}
	public void join(int port, UserCredentials credentials) {
		if (isConnected()) {
			LOGGER.warn("Client already connected.");
			return;
		}

		try {
			var client = new Client(port, credentials);
			client.setResponseReader(this);
			this.id = client.getId();
			this.username = client.getUsername();
			this.requestWriter = client;
		}
		catch (AppException | GeneralSecurityException e) {
			disconnect();
			LOGGER.error(e.getMessage());
			return;
		}
		catch (IOException _) {
			disconnect();
			LOGGER.fatal("An error has occurred while connecting.");
			return;
		}

		this.isClient = true;
		this.navigator.switchToChatbox();
		LOGGER.debug("Connected to server on port {} with id {}.", port, id);
	}
	public void disconnect() {
		this.isHost = false;
		this.isClient = false;

		this.requestWriter = null;

		this.username = null;
		navigator.switchToHome();
	}
	//endregion

	//region Overrides
	@Override public void writeRequest(SerializableRequest request) {
		requestWriter.writeRequest(request);

	}
	@Override public void readResponse(SerializableResponse response) {
		if (response instanceof MessageResponse _response) receiveMessage(_response);
		else if (response instanceof KickResponse) disconnect();
		else if (response instanceof SystemMessageResponse _response) receiveSystemMessage(_response);
	}

	@Override public byte[] encryptMessage(String message) { return requestWriter.encryptMessage(message); }
	@Override public String decryptMessage(byte[] data) { return requestWriter.decryptMessage(data); }
	//endregion

	//region Aux Methods
	private void receiveMessage(MessageResponse message) {
		if (chatBoxListener != null) Platform.runLater(() -> chatBoxListener.receiveMessage(message));
	}
	private void receiveSystemMessage(SystemMessageResponse message) {
		if (chatBoxListener != null) Platform.runLater(() -> chatBoxListener.receiveSystemMessage(message));
	}
	//endregion

	//region Getters
	public int getId() { return id; }
	public String getUsername() { return username; }
	private boolean isConnected() { return isHost || isClient; }
	//endregion

	//region Setters
	public void addNavigationListener(NavigationListener listener) { this.navigator.add(listener); }
	public void setChatBoxListener(ChatBoxListener listener) { chatBoxListener = listener; }
	public void setConnectionListener(ConnectionListener listener) { this.connectionListener = listener; }
	//endregion
}
