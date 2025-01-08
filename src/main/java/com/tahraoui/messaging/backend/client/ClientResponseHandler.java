package com.tahraoui.messaging.backend.client;

import com.tahraoui.messaging.backend.data.ResponseHandler;
import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientResponseHandler implements ResponseHandler {

	private static final Logger LOGGER = LogManager.getLogger(ClientResponseHandler.class);

	private ResponseReceiver receiver;
	public ClientResponseHandler() {}


	@Override
	public void handleResponse(SerializableResponse response) {

		if (response instanceof MessageResponse _response) receiver.receiveMessage(_response);
	}

	@Override
	public void setListener(ResponseReceiver listener) {
		this.receiver = listener;
	}
}
