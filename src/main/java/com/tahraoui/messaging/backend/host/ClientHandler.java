package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.request.ConnectionRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.request.SystemMessageRequest;
import com.tahraoui.messaging.backend.data.response.ConnectionResponse;
import com.tahraoui.messaging.model.exception.AppException;
import com.tahraoui.messaging.model.exception.ConnectionFailedException;
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
	private final String username;
	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;
	private final RequestWriter requestWriter;
	private DisconnectionListener disconnectionListener;

	public ClientHandler(Socket socket, String password, RequestWriter requestWriter, BigInteger p, BigInteger g) throws AppException, IOException {
		this.socket = socket;
		this.reader = new ObjectInputStream(socket.getInputStream());
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.writer.flush();

		this.requestWriter = requestWriter;

		var request = connect(password, new ConnectionResponse(p, g,true));
		this.id = request.id();
		this.username = request.username();
		this.requestWriter.writeRequest(new SystemMessageRequest("%s [%d] has joined the chat.".formatted(username, id)));
	}

	private ConnectionRequest connect(String password, ConnectionResponse response) throws AppException {
		var success = false;
		try {
			var request = (ConnectionRequest) this.reader.readObject();
			if (request == null || !request.password().equals(password)) {
				writer.writeObject(new ConnectionResponse(null,null,false));
				writer.flush();
				throw new WrongPasswordException();
			}
			this.writer.writeObject(response);
			this.writer.flush();
			success = true;
			return request;
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
		catch (ConnectionFailedException e) {
			LOGGER.debug("{} [{}] has disconnected.", username, id);
			disconnectionListener.onDisconnect(id);
			requestWriter.writeRequest(new SystemMessageRequest("%s [%d] has disconnected.".formatted(username, id)));
			Thread.currentThread().interrupt();
		}
		finally {
			closeResources();
		}
	}

	private void handleRequest() throws ConnectionFailedException {
		if (requestWriter == null) return;
		try {
			var request = reader.readObject();
			if (request == null) throw new ConnectionFailedException();

			if (request instanceof SerializableRequest _request)
				requestWriter.writeRequest(_request);
		}
		catch (ClassNotFoundException e) {
			LOGGER.error("Failed to read request: {}.", e.getMessage());
		}
		catch (IOException e) {
			throw new ConnectionFailedException();
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
		catch (IOException _) { }
	}

	public int getId() { return id; }
	public ObjectOutput getWriter() { return writer; }
	public void setDisconnectionListener(DisconnectionListener disconnectionListener) { this.disconnectionListener = disconnectionListener; }
}
