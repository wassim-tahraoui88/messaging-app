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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

public class ClientRequestHandler implements RequestWriter {

	private record Handler(ObjectOutput writer) {}

	private static final Logger LOGGER = LogManager.getLogger(ClientRequestHandler.class);
	private final Map<Integer, ClientHandler> handlers;
	private ResponseReader responseReader;

	public ClientRequestHandler() {
		this.handlers = new HashMap<>(10);

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

		var response = new SystemMessageResponse("%s [%d] has been kicked from the chat.".formatted(request.username(), request.userId()));
		unicastResponse(new KickResponse(), handlers.get(request.userId()).getWriter());
		remove(request.userId());
		broadcastResponse(response);
	}
	private void handleMessageRequest(MessageRequest request) {
		var response = new MessageResponse(request.senderId(), request.senderName(), request.content());
		broadcastResponse(response);
	}

}
