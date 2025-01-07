package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.request.ConnectionRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.ConnectionResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class.getName());
	public static final List<ClientHandler> HANDLERS = new ArrayList<>(10);

	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;

	public ClientHandler(Socket socket, String password, ConnectionResponse connectionResponse) throws IOException {
		this.socket = socket;
		this.reader = new ObjectInputStream(socket.getInputStream());
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.writer.flush();

		if (!connect(password, connectionResponse)) {
			socket.close();
			return;
		}

		HANDLERS.add(this);
	}

	private boolean connect(String password, ConnectionResponse response) {
		try {
			var request = (ConnectionRequest) this.reader.readObject();
			if (request == null || request.password().equals(password)) throw new IOException();
			this.writer.writeObject(response);
			this.writer.flush();
			return true;
		}
		catch (IOException | ClassNotFoundException _) {
			LOGGER.error("Failed to read connection request.");
		}

		closeResources();
		closeSocket();
		return false;
	}

	private void broadcastResponse(SerializableResponse response) {
		for (var handler : HANDLERS) {
			try {
				handler.writer.writeObject(response);
				handler.writer.flush();
			}
			catch (IOException _) {
				LOGGER.error("Failed to send response to client {}.", handler);
			}
		}
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
			if (!(request instanceof SerializableRequest)) return;

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
