package com.tahraoui.messaging.backend.client;

import com.tahraoui.messaging.backend.ConnectionService;
import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.ResponseReader;
import com.tahraoui.messaging.backend.data.UserCredentials;
import com.tahraoui.messaging.backend.data.request.ConnectionRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.ConnectionResponse;
import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import com.tahraoui.messaging.model.exception.AppException;
import com.tahraoui.messaging.model.exception.ConnectionFailedException;
import com.tahraoui.messaging.model.exception.ReadingFailedException;
import com.tahraoui.messaging.model.exception.WritingFailedException;
import com.tahraoui.messaging.model.exception.WrongPasswordException;
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

public class ClientListener implements Runnable, RequestWriter, ResponseReader {

	private static final Logger LOGGER = LogManager.getLogger(ClientListener.class);

	private final int id;
	private final String username;
	private final BigInteger privateKey, publicKey;
	private BigInteger sharedKey;
	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;
	private ResponseReader responseReader;

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
			this.writer.writeObject(new ConnectionRequest(this.id, credentials.username(), credentials.password()));
			this.writer.flush();

			var response = (ConnectionResponse) this.reader.readObject();

			if (response == null) throw new ConnectionFailedException();
			else if (!response.success()) throw new WrongPasswordException();

			success = true;

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
				throw new ConnectionFailedException();
			}
		}
	}

	@Override public void run() {
		try {
			while (socket.isConnected() && !socket.isClosed()) handleResponse();
			ConnectionService.getInstance().disconnect();
		}
		finally {
			closeResources();
		}
	}

	private void handleResponse() {
		try {
			var response = reader.readObject();
			if (response instanceof SerializableResponse _response)
				responseReader.readResponse(_response);
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
	public void setResponseReader(ResponseReader responseReader) { this.responseReader = responseReader; }

	public int getId() { return id; }
	public BigInteger getPrivateKey() { return privateKey; }
	public BigInteger getPublicKey() { return publicKey; }
	public BigInteger getSharedKey() { return sharedKey; }

	public String getUsername() { return username; }

	@Override
	public void writeRequest(SerializableRequest request) {
		try {
			writer.writeObject(request);
			writer.flush();
		}
		catch (IOException e) {
			LOGGER.error("Failed write request: {}", e.getMessage());
		}
	}
	@Override
	public void readResponse(SerializableResponse response) {
		if (response instanceof MessageResponse _response) responseReader.readResponse(_response);
	}
}
