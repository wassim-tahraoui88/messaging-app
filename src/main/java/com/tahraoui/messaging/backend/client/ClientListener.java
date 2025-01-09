package com.tahraoui.messaging.backend.client;

import com.tahraoui.messaging.backend.ConnectionService;
import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.ResponseReader;
import com.tahraoui.messaging.backend.data.UserCredentials;
import com.tahraoui.messaging.backend.data.request.ConnectionEstablishmentRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.ConnectionEstablishmentResponse;
import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import com.tahraoui.messaging.model.exception.AppException;
import com.tahraoui.messaging.model.exception.ConnectionFailedException;
import com.tahraoui.messaging.model.exception.ReadingFailedException;
import com.tahraoui.messaging.model.exception.WritingFailedException;
import com.tahraoui.messaging.model.exception.WrongPasswordException;
import com.tahraoui.messaging.util.EncryptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

public class ClientListener implements Runnable, RequestWriter, ResponseReader {

	private static final Logger LOGGER = LogManager.getLogger(ClientListener.class);

	private final int id;
	private final String username;

	private final KeyPair rsaKeyPair;
	private final SecretKey aesKey;
	private final IvParameterSpec iv;

	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;
	private ResponseReader responseReader;

	public ClientListener(Socket socket, UserCredentials credentials) throws AppException, IOException, GeneralSecurityException {
		this.rsaKeyPair = EncryptionUtils.generateKeyPair();

		this.socket = socket;
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.reader = new ObjectInputStream(socket.getInputStream());
		this.writer.flush();

		this.id = writer.hashCode();
		var response = connect(credentials);
		this.username = credentials.username();
		this.aesKey = EncryptionUtils.decryptRSA(response.secret(), rsaKeyPair.getPrivate());
		this.iv = new IvParameterSpec(response.iv());
	}

	public ConnectionEstablishmentResponse connect(UserCredentials credentials) throws AppException {
		var success = false;
		try {
			this.writer.writeObject(new ConnectionEstablishmentRequest(this.id, credentials.username(), credentials.password(), rsaKeyPair.getPublic()));
			this.writer.flush();

			var response = (ConnectionEstablishmentResponse) this.reader.readObject();

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
	void closeSocket() {
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

	public void setResponseReader(ResponseReader responseReader) { this.responseReader = responseReader; }

	public int getId() { return id; }

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
	public void readResponse(SerializableResponse response) { responseReader.readResponse(response); }

	@Override
	public byte[] encryptMessage(String message) {
		try {
			return EncryptionUtils.encrypt(message, aesKey, iv);
		}
		catch (GeneralSecurityException e) {
			return null;
		}
	}
	@Override
	public String decryptMessage(byte[] data) {
		try {
			return EncryptionUtils.decrypt(data, aesKey, iv);
		}
		catch (GeneralSecurityException e) {
			return null;
		}
	}
}
