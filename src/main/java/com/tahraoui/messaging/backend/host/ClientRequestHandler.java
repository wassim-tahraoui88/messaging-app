package com.tahraoui.messaging.backend.host;

import com.tahraoui.messaging.backend.data.RequestWriter;
import com.tahraoui.messaging.backend.data.ResponseReader;
import com.tahraoui.messaging.backend.data.request.MessageRequest;
import com.tahraoui.messaging.backend.data.request.SerializableRequest;
import com.tahraoui.messaging.backend.data.request.SystemMessageRequest;
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

	private static final Logger LOGGER = LogManager.getLogger(ClientRequestHandler.class);
	private final Map<Integer, ObjectOutput> writers;
	private ResponseReader responseReader;

	public ClientRequestHandler() {
		this.writers = new HashMap<>(10);

	}

	public void add(int id, ObjectOutput handler) { writers.put(id, handler); }
	public void remove(int id) {
		var writer = writers.remove(id);
		if (writer != null) {
			try {
				writer.close();
			}
			catch (IOException _) { }
		}
	}

	private void handleMessageRequest(MessageRequest request) {
		var response = new MessageResponse(request.senderName(), request.content());
		broadcastResponse(response);
	}
	private void handleSystemMessageRequest(SystemMessageRequest request) {
		var response = new SystemMessageResponse(request.content());
		broadcastResponse(response);
	}

	public void setResponseReader(ResponseReader responseReader) { this.responseReader = responseReader; }

	@Override
	public void writeRequest(SerializableRequest request) {
		if (request instanceof MessageRequest _request) handleMessageRequest(_request);
		else if (request instanceof SystemMessageRequest _request) handleSystemMessageRequest(_request);
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
		responseReader.readResponse(response);
	}

}
