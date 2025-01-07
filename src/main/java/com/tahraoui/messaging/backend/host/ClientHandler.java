package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.RequestHandler;
import com.tahraoui.messaging.backend.data.request.ConnectionRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.ConnectionResponse;
import com.tahraoui.messaging.model.exception.AppException;
import com.tahraoui.messaging.model.exception.ReadingFailedException;
import com.tahraoui.messaging.model.exception.WritingFailedException;
import com.tahraoui.messaging.model.exception.WrongPasswordException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.net.Socket;

public class ClientHandler implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class.getName());

	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;
	private RequestHandler requestHandler;

	public ClientHandler(Socket socket, String password, BigInteger p, BigInteger g) throws AppException, IOException {
		this.socket = socket;
		this.reader = new ObjectInputStream(socket.getInputStream());
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.writer.flush();
		connect(password, new ConnectionResponse(writer.hashCode(), p, g));
	}

	private void connect(String password, ConnectionResponse response) throws AppException {
		var success = false;
		try {
			LOGGER.debug("Waiting for connection request...");
			var request = (ConnectionRequest) this.reader.readObject();
			LOGGER.debug("Connection request received.");
			if (request == null || !request.password().equals(password)) {
				writer.writeObject(new ConnectionResponse(-1, null, null));
				writer.flush();
				throw new WrongPasswordException();
			}
			LOGGER.debug("Connection request accepted.");
			LOGGER.debug("Sending connection response...");
			this.writer.writeObject(response);
			this.writer.flush();
			success = true;
			LOGGER.debug("Connection response sent.");
		}
		catch (ClassNotFoundException | StreamCorruptedException | OptionalDataException _) {
			throw new ReadingFailedException();
		}
		catch (IOException _) {
			throw new WritingFailedException();
		}
		finally {
			if (!success) {
				closeResources();
				closeSocket();
			}
		}
	}

	@Override public void run() {
		try {
			while (socket.isConnected() && !socket.isClosed()) handleRequest();
			LOGGER.warn("Client with id {} has disconnected.", writer.hashCode());
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
