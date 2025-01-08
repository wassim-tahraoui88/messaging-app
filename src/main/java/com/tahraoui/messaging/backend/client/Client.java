package com.tahraoui.messaging.backend.client;

import com.tahraoui.messaging.model.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import static com.tahraoui.messaging.util.NetworkUtils.SERVER_NAME;

public class Client {

	private static final Logger LOGGER = LogManager.getLogger(Client.class);

	private final ClientListener handler;


	public Client(int port, UserCredentials credentials) throws AppException, IOException {
		var socket = new Socket(InetAddress.getByName(SERVER_NAME), port);
		var responseHandler = new ClientResponseHandler();
		this.handler = new ClientListener(socket, credentials);
		this.handler.setResponseHandler(responseHandler);
		var threadName = "Client Thread - [%d]".formatted(this.handler.getId());
		new Thread(handler, threadName).start();
		LOGGER.info("Thread created.");
	}

	public void setListener(ResponseReceiver listener) { handler.setListener(listener); }
	public int getId() { return handler.getId(); }

	public String getUsername() { return handler.getUsername(); }
}
