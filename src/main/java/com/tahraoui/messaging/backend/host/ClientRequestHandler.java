package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.RequestHandler;
import com.tahraoui.messaging.backend.data.request.MessageRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

public class ClientRequestHandler implements RequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(ClientRequestHandler.class);
	private final Map<Integer, ObjectOutput> writers;

	public ClientRequestHandler() {
		this.writers = new HashMap<>(10);

	}

	public void add(Integer id, ObjectOutput handler) { writers.put(id, handler); }

	@Override
	public void handleRequest(SerializableRequest request) {
		if (request instanceof MessageRequest _request) broadcastResponse(new MessageResponse(_request.senderName(), _request.content()));
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
		for (var writer : writers.values()) unicastResponse(response, writer);
	}
}
