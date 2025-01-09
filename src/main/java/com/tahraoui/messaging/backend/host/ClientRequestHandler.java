package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.ResponseReader;
import com.tahraoui.messaging.backend.data.request.KickRequest;
import com.tahraoui.messaging.backend.data.request.MessageRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.request.SystemMessageRequest;
import com.tahraoui.messaging.backend.data.response.KickResponse;
import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import com.tahraoui.messaging.backend.data.response.SystemMessageResponse;
import com.tahraoui.messaging.util.EncryptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class ClientRequestHandler implements RequestWriter {

	private static final Logger LOGGER = LogManager.getLogger(ClientRequestHandler.class);
	private final Map<Integer, ClientHandler> handlers;
	private ResponseReader responseReader;

	private final SecretKey aesKey;
	private final IvParameterSpec iv;
	public ClientRequestHandler(SecretKey aesKey, IvParameterSpec iv) {
		this.handlers = new HashMap<>(10);
		this.aesKey = aesKey;
		this.iv = iv;
	}

	public void add(int id, ClientHandler handler) { handlers.put(id, handler); }
	public void remove(int id) { handlers.remove(id); }

	public void setResponseReader(ResponseReader responseReader) { this.responseReader = responseReader; }

	@Override
	public void writeRequest(SerializableRequest request) {
		if (request instanceof SystemMessageRequest _request) handleSystemMessageRequest(_request);
		else if (request instanceof KickRequest _request) handleKickRequest(_request);
		else if (request instanceof MessageRequest _request) handleMessageRequest(_request);
	}

	@Override public byte[] encryptMessage(String message) {
		try {
			return EncryptionUtils.encrypt(message, aesKey, iv);
		}
		catch (GeneralSecurityException e) {
			return null;
		}
	}
	@Override public String decryptMessage(byte[] data) {
		try {
			return EncryptionUtils.decrypt(data, aesKey, iv);
		}
		catch (GeneralSecurityException e) {
			return null;
		}
	}

	private void unicastResponse(SerializableResponse response, ObjectOutput writer) {
		try {
			writer.writeObject(response);
			writer.flush();
		}
		catch (IOException _) {
			LOGGER.error("Failed to send response to client {}.", writer.hashCode());
		}
	}
	public void broadcastResponse(SerializableResponse response) {
		for (var handler : handlers.values()) unicastResponse(response, handler.getWriter());
		responseReader.readResponse(response);
	}

	private void handleSystemMessageRequest(SystemMessageRequest request) {
		var response = new SystemMessageResponse(request.content());
		broadcastResponse(response);
	}
	private void handleKickRequest(KickRequest request) {
		var handler = handlers.get(request.userId());
		handler.kick();
		remove(handler.getId());
		unicastResponse(new KickResponse(), handler.getWriter());

		broadcastResponse(new SystemMessageResponse("%s [%d] has been kicked from the chat.".formatted(request.username(), request.userId())));
	}
	private void handleMessageRequest(MessageRequest request) {
		var response = new MessageResponse(request.senderId(), request.senderName(), decryptMessage(request.data()));
		broadcastResponse(response);
	}

}
