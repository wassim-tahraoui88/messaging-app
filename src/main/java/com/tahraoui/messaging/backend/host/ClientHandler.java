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

	private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

	private final int id;
	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;
	private RequestHandler requestHandler;

	public ClientHandler(Socket socket, String password, BigInteger p, BigInteger g) throws AppException, IOException {
		this.socket = socket;
		this.reader = new ObjectInputStream(socket.getInputStream());
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.writer.flush();
		this.id = connect(password, new ConnectionResponse(p, g,true));
	}

	private int connect(String password, ConnectionResponse response) throws AppException {
		var success = false;
		try {
			LOGGER.info("Waiting for connection request...");
			var request = (ConnectionRequest) this.reader.readObject();
			if (request == null || !request.password().equals(password)) {
				writer.writeObject(new ConnectionResponse(null,null,false));
				writer.flush();
				throw new WrongPasswordException();
			}
			this.writer.writeObject(response);
			this.writer.flush();
			success = true;
			LOGGER.debug("Connection established.");
			return request.id();
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
			LOGGER.warn("Socket of Client id {} is closed.", writer.hashCode());
		}
		finally {
			closeResources();
		}
	}

	private void handleRequest() {
		if (requestHandler == null) return;
		try {
			var request = reader.readObject();
			if (request instanceof SerializableRequest _request)
				requestHandler.handleRequest(_request);
		}
		catch (IOException | ClassNotFoundException e) {
			LOGGER.error("Failed to read request: {}.", e.getMessage());
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

	public int getId() { return id; }
	public ObjectOutput getWriter() { return writer; }
	public void setRequestHandler(RequestHandler requestHandler) { this.requestHandler = requestHandler; }

}
