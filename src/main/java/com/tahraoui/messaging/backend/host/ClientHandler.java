package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.request.ConnectionEstablishmentRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.request.SystemMessageRequest;
import com.tahraoui.messaging.backend.data.response.ConnectionEstablishmentResponse;
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
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

public class ClientHandler implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

	private final SecretKey aesKey;
	private final KeyPair rsaKeyPair;
	private final IvParameterSpec iv;

	private final int id;
	private boolean isKicked;
	private final String username;
	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;
	private final RequestWriter requestWriter;
	private DisconnectionListener disconnectionListener;

	public ClientHandler(Socket socket, String password, RequestWriter requestWriter, KeyPair rsaKeyPair, SecretKey aesKey, IvParameterSpec iv) throws AppException, IOException {
		this.aesKey = aesKey;
		this.rsaKeyPair = rsaKeyPair;
		this.iv = iv;

		this.socket = socket;
		this.reader = new ObjectInputStream(socket.getInputStream());
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.writer.flush();

		this.requestWriter = requestWriter;

		var request = connect(password);
		this.id = request.id();
		this.username = request.username();
		this.requestWriter.writeRequest(new SystemMessageRequest("%s [%d] has joined the chat.".formatted(username, id)));
	}

	private ConnectionEstablishmentRequest connect(String password) throws AppException {
		var success = false;
		try {
			var request = (ConnectionEstablishmentRequest) this.reader.readObject();

			if (request == null || !request.password().equals(password)) {
				writer.writeObject(new ConnectionEstablishmentResponse(null,null, null,false));
				writer.flush();
				throw new WrongPasswordException();
			}

			var encryptedKey = EncryptionUtils.encryptRSA(aesKey.getEncoded(), request.key());

			var response = new ConnectionEstablishmentResponse(rsaKeyPair.getPublic(), encryptedKey, iv.getIV(), true);
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
		catch (Exception e) {
			throw new RuntimeException(e);
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
			if (!isKicked) throw new ConnectionFailedException();
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

	public void kick() { isKicked = true; }
	public int getId() { return id; }
	public ObjectOutput getWriter() { return writer; }
	public void setDisconnectionListener(DisconnectionListener disconnectionListener) { this.disconnectionListener = disconnectionListener; }
}
