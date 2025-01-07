package com.tahraoui.messaging.backend.client;

import com.tahraoui.messaging.backend.data.request.ConnectionRequest;
import com.tahraoui.messaging.backend.data.response.ConnectionResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientListener implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(ClientListener.class.getName());

	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;

	public ClientListener(Socket socket) throws IOException {
		this.socket = socket;
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.reader = new ObjectInputStream(socket.getInputStream());
		this.writer.flush();
	}

	public ConnectionResponse connect(String password) {
		try {
			LOGGER.debug("Sending connection request...");
			this.writer.writeObject(new ConnectionRequest(password));
			this.writer.flush();
			LOGGER.debug("Connection request sent.");

			LOGGER.debug("Waiting for connection response...");
			return (ConnectionResponse) this.reader.readObject();
		}
		catch (IOException | ClassNotFoundException _) {
			LOGGER.error("Failed to read connection request...");
			closeResources();
		}
		return null;
	}

	@Override public void run() {
		try {
			while (socket.isConnected() && !socket.isClosed()) handleRequest();
		}
		finally {
			closeResources();
		}
	}

	private void handleRequest() {
		try {
			var request = reader.readObject();
			if (!(request instanceof SerializableResponse)) return;

		}
		catch (IOException | ClassNotFoundException e) {
			closeSocket();
		}
	}

	private void closeSocket() {
		try {
			socket.close();
		}
		catch (IOException e) {
			LOGGER.fatal("Failed to close socket", e);
		}
	}
	private void closeResources() {
		try {
			reader.close();
			writer.close();
		}
		catch (IOException e) {
			LOGGER.fatal("Failed to close resources", e);
		}
	}
}
