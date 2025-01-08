package com.tahraoui.messaging.backend.data;

import com.tahraoui.messaging.backend.client.ResponseReceiver;
import com.tahraoui.messaging.backend.data.response.SerializableResponse;

public interface ResponseHandler {

	void handleResponse(SerializableResponse response);

	void setListener(ResponseReceiver listener);
}
