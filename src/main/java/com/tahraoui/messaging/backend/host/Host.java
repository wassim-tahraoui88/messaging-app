package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.ResponseReader;
import com.tahraoui.messaging.backend.data.UserCredentials;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import com.tahraoui.messaging.model.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.security.SecureRandom;

public class Host implements Runnable, RequestWriter, ResponseReader {

	private static final Logger LOGGER = LogManager.getLogger(Host.class);
	private static final int BIT_LENGTH = 2048;

	private final int port;
	private final int id;
	private final String username;
	private final String password;
	private final ClientRequestHandler requestHandler;
	private ResponseReader responseReader;

	private final BigInteger p, g;
	private final BigInteger privateKey, publicKey;
	private BigInteger sharedKey;

	public Host(int port, UserCredentials credentials) {
		var random = new SecureRandom();
		this.p = BigInteger.probablePrime(BIT_LENGTH, random);
		this.g = new BigInteger("2");
		this.privateKey = new BigInteger(BIT_LENGTH, random);
		this.publicKey = g.modPow(privateKey, p);

		this.port = port;
		this.id = 0;
		this.username = credentials.username();
		this.password = credentials.password();
		this.requestHandler = new ClientRequestHandler();
		this.requestHandler.setResponseReader(this);
	}

	@Override public void run() {
		try (var serverSocket = new ServerSocket(port)) {
			while (!serverSocket.isClosed()) {
				var socket = serverSocket.accept();
				try {
					LOGGER.info("Received connection from {}.", socket.getInetAddress());
					var handler = new ClientHandler(socket, password, requestHandler, p, g);
					handler.setDisconnectionListener(requestHandler::remove);
					var id = handler.getId();
					var threadName = "ClientHandler Thread - [%d]".formatted(id);
					requestHandler.add(id, handler.getWriter());
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

	public void computeSharedKey(BigInteger clientPublicKey) {
		this.sharedKey = clientPublicKey.modPow(privateKey, p);

	}

	public int getId() { return id; }
	public String getUsername() { return username; }
	public BigInteger getP() { return p; }
	public BigInteger getG() { return g; }
	public BigInteger getPrivateKey() { return privateKey; }
	public BigInteger getPublicKey() { return publicKey; }
	public BigInteger getSharedKey() { return sharedKey; }

	public void setResponseReader(ResponseReader responseReader) { this.responseReader = responseReader; }

	@Override
	public void writeRequest(SerializableRequest request) { requestHandler.writeRequest(request); }
	@Override
	public void readResponse(SerializableResponse response) { this.responseReader.readResponse(response); }
}
