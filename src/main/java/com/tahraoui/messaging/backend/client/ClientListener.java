package com.tahraoui.messaging.backend.client;

import com.tahraoui.messaging.backend.data.ResponseHandler;
import com.tahraoui.messaging.backend.data.request.ConnectionRequest;
import com.tahraoui.messaging.backend.data.response.ConnectionResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import com.tahraoui.messaging.model.exception.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

public class ClientListener implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(ClientListener.class);

	private final int id;
	private final String username;
	private final BigInteger privateKey, publicKey;
	private BigInteger sharedKey;
	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;
	private ResponseHandler responseHandler;

	public ClientListener(Socket socket, UserCredentials credentials) throws AppException, IOException {
		this.socket = socket;
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.reader = new ObjectInputStream(socket.getInputStream());
		this.writer.flush();
		this.id = writer.hashCode();

		var response = connect(credentials);

		this.username = credentials.username();

		var random = new SecureRandom();
		this.privateKey = new BigInteger(response.p().bitLength(), random);
		this.publicKey = response.g().modPow(privateKey, response.p());
	}

	public ConnectionResponse connect(UserCredentials credentials) throws AppException {
		var success = false;
		try {
			LOGGER.info("Requesting connection...");
			this.writer.writeObject(new ConnectionRequest(this.id, credentials.username(), credentials.password()));
			this.writer.flush();

			var response = (ConnectionResponse) this.reader.readObject();

			if (response == null) throw new ConnectionFailedException();
			else if (!response.success()) throw new WrongPasswordException();

			LOGGER.info("Connection established.");
			return response;
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
			while (socket.isConnected() && !socket.isClosed()) handleResponse();
			LOGGER.warn("Socket is closed.");
		}
		finally {
			closeResources();
		}
	}

	private void handleResponse() {
		LOGGER.debug("Handling response reading...");
		try {
			LOGGER.debug("Reading response object...");
			var response = reader.readObject();
			if (response instanceof SerializableResponse _response)
				responseHandler.handleResponse(_response);
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

	public void computeSharedKey(BigInteger hostPublicKey, BigInteger p) {
		this.sharedKey = hostPublicKey.modPow(privateKey, p);

	}
	public void setResponseHandler(ResponseHandler responseHandler) { this.responseHandler = responseHandler; }

	public int getId() { return id; }
	public BigInteger getPrivateKey() { return privateKey; }
	public BigInteger getPublicKey() { return publicKey; }
	public BigInteger getSharedKey() { return sharedKey; }

	public void setListener(ResponseReceiver listener) { this.responseHandler.setListener(listener); }

	public String getUsername() { return username; }
}
