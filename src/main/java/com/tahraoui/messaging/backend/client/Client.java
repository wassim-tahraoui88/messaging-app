package com.tahraoui.messaging.backend.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;

import static com.tahraoui.messaging.util.NetworkUtils.SERVER_NAME;

public class Client {

	private final BigInteger privateKey, publicKey;
	private BigInteger sharedKey;

	public Client( int port, String password) throws IOException {
		var socket = new Socket(InetAddress.getByName(SERVER_NAME), port);
		var listener = new ClientListener(socket);
		var connection = listener.connect(password);
		if (connection == null) throw new RuntimeException("");

		var random = new SecureRandom();
		this.privateKey = new BigInteger(connection.p().bitLength(), random);
		this.publicKey = connection.g().modPow(privateKey, connection.p());

		new Thread(listener).start();
	}

	public void computeSharedKey(BigInteger hostPublicKey, BigInteger p) {
		this.sharedKey = hostPublicKey.modPow(privateKey, p);

	}

	public BigInteger getPrivateKey() { return privateKey; }
	public BigInteger getPublicKey() { return publicKey; }
	public BigInteger getSharedKey() { return sharedKey; }
}
