package com.tahraoui.messaging.backend.client;

import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.ResponseReader;
import com.tahraoui.messaging.backend.data.pojo.UserCredentials;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.KickResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import com.tahraoui.messaging.model.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;

import static com.tahraoui.messaging.util.NetworkUtils.SERVER_NAME;

public class Client implements RequestWriter, ResponseReader {

	private static final Logger LOGGER = LogManager.getLogger(Client.class);

	private final ClientListener handler;
	private ResponseReader responseReader;

	public Client(int port, UserCredentials credentials) throws AppException, IOException, GeneralSecurityException {
		var socket = new Socket(InetAddress.getByName(SERVER_NAME), port);
		this.handler = new ClientListener(socket, credentials);
		this.handler.setResponseReader(this);
		var threadName = "Client Thread - [%d]".formatted(this.handler.getId());
		new Thread(handler, threadName).start();
	}

	public void setResponseReader(ResponseReader responseReader) { this.responseReader = responseReader; }

	public int getId() { return handler.getId(); }
	public String getUsername() { return handler.getUsername(); }

	@Override
	public void writeRequest(SerializableRequest request) { handler.writeRequest(request); }
	@Override
	public void readResponse(SerializableResponse response) {
		if (response instanceof KickResponse) handler.closeSocket();
		else responseReader.readResponse(response);
	}

	@Override
	public byte[] encryptMessage(String message) { return handler.encryptMessage(message); }
	@Override
	public String decryptMessage(byte[] data) { return handler.decryptMessage(data); }
}
