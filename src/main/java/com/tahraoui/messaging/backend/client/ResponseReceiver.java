package com.tahraoui.messaging.backend.client;

import com.tahraoui.messaging.backend.data.response.MessageResponse;

public interface ResponseReceiver {
	void receiveMessage(MessageResponse response);
}
