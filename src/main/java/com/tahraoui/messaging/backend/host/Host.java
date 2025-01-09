package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.ResponseReader;
import com.tahraoui.messaging.backend.data.UserCredentials;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import com.tahraoui.messaging.model.exception.AppException;
import com.tahraoui.messaging.util.EncryptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.*;

public class Host implements Runnable, RequestWriter, ResponseReader {

	private static final Logger LOGGER = LogManager.getLogger(Host.class);

	private final KeyPair rsaKeyPair;
	private final SecretKey aesKey;
	private final IvParameterSpec iv;

	private final int port;
	private final String username;
	private final String password;
	private final ClientRequestHandler requestHandler;
	private ResponseReader responseReader;

	public Host(int port, UserCredentials credentials) throws GeneralSecurityException {
		this.aesKey = EncryptionUtils.generateKey();
		this.rsaKeyPair = EncryptionUtils.generateKeyPair();
		this.iv = EncryptionUtils.generateIV();

		this.port = port;
		this.username = credentials.username();
		this.password = credentials.password();
		this.requestHandler = new ClientRequestHandler(aesKey, iv);
		this.requestHandler.setResponseReader(this);
	}

	@Override public void run() {
		try (var serverSocket = new ServerSocket(port)) {
			while (!serverSocket.isClosed()) {
				var socket = serverSocket.accept();
				try {
					LOGGER.info("Received connection from {}.", socket.getInetAddress());
					var handler = new ClientHandler(socket, password, requestHandler, rsaKeyPair, aesKey, iv);
					handler.setDisconnectionListener(requestHandler::remove);
					var id = handler.getId();
					var threadName = "ClientHandler Thread - [%d]".formatted(id);
					requestHandler.add(id, handler);
					new Thread(handler, threadName).start();
				}
				catch (AppException e) {
					LOGGER.error(e.getMessage());
				}
				catch (IOException e) {
					LOGGER.fatal("An internal error has occurred while establishing connection: {}.", e.getMessage());
				}
			}
		}
		catch (IOException _) {
			LOGGER.error("Server is shutdown.");
		}
	}

	public String getUsername() { return username; }
	public void setResponseReader(ResponseReader responseReader) { this.responseReader = responseReader; }

	@Override
	public void writeRequest(SerializableRequest request) { requestHandler.writeRequest(request); }
	@Override
	public void readResponse(SerializableResponse response) { this.responseReader.readResponse(response); }

	@Override
	public byte[] encryptMessage(String message) { return requestHandler.encryptMessage(message); }
	@Override
	public String decryptMessage(byte[] data) { return requestHandler.decryptMessage(data); }
}
