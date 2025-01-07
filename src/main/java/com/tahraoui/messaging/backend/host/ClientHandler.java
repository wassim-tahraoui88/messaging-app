package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.RequestHandler;
import com.tahraoui.messaging.backend.data.request.ConnectionRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.ConnectionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;

public class ClientHandler implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class.getName());

	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;
	private RequestHandler requestHandler;

	public ClientHandler(Socket socket, String password, BigInteger p, BigInteger g) throws IOException {
		this.socket = socket;
		this.reader = new ObjectInputStream(socket.getInputStream());
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.writer.flush();
		if (!connect(password, new ConnectionResponse(writer.hashCode(), p, g))) throw new IOException();
	}

	private boolean connect(String password, ConnectionResponse response) {
		try {
			LOGGER.debug("Waiting for connection request...");
			var request = (ConnectionRequest) this.reader.readObject();
			LOGGER.debug("Connection request received.");
			if (request == null || !request.password().equals(password)) throw new IOException("Invalid password.");
			LOGGER.debug("Connection request accepted.");
			LOGGER.debug("Sending connection response...");
			this.writer.writeObject(response);
			this.writer.flush();
			LOGGER.debug("Connection response sent.");
			return true;
		}
		catch (IOException | ClassNotFoundException _) {
			LOGGER.error("Failed to read connection request.");
		}

		closeResources();
		closeSocket();
		return false;
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
		if (requestHandler == null) return;
		try {
			var request = reader.readObject();
			if (!(request instanceof SerializableRequest _request)) return;

			requestHandler.handleRequest(_request);
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

	public ObjectOutput getWriter() { return writer; }
	public void setRequestHandler(RequestHandler requestHandler) { this.requestHandler = requestHandler; }
}
