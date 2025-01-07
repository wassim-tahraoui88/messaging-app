package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.model.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.security.SecureRandom;

public class Host implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(Host.class);
	private static final int BIT_LENGTH = 2048;

	private final int port;
	private final String password;
	private final ClientRequestHandler requestHandler;

	private final BigInteger p, g;
	private final BigInteger privateKey, publicKey;
	private BigInteger sharedKey;

	public Host(int port, String password) {
		var random = new SecureRandom();
		this.p = BigInteger.probablePrime(BIT_LENGTH, random);
		this.g = new BigInteger("2");
		this.privateKey = new BigInteger(BIT_LENGTH, random);
		this.publicKey = g.modPow(privateKey, p);

		this.port = port;
		this.password = password;
		this.requestHandler = new ClientRequestHandler();
	}
	public void computeSharedKey(BigInteger clientPublicKey) {
		this.sharedKey = clientPublicKey.modPow(privateKey, p);

	}

	@Override public void run() {
		try (var serverSocket = new ServerSocket(port)) {
			while (!serverSocket.isClosed()) {
				var socket = serverSocket.accept();
				try {
					LOGGER.debug("Server listening on port {}.", socket.getInetAddress());
					var clientHandler = new ClientHandler(socket, password, p, g);
					clientHandler.setRequestHandler(requestHandler);
					var writer = clientHandler.getWriter();
					var threadName = "ClientHandler Thread - [%d]".formatted(writer.hashCode());
					requestHandler.add(writer.hashCode(), writer);
					new Thread(clientHandler, threadName).start();
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

	public BigInteger getP() { return p; }
	public BigInteger getG() { return g; }
	public BigInteger getPrivateKey() { return privateKey; }
	public BigInteger getPublicKey() { return publicKey; }
	public BigInteger getSharedKey() { return sharedKey; }
}
