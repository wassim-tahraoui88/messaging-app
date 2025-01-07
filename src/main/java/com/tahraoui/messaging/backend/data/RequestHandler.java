package com.tahraoui.messaging.backend.data;

import com.tahraoui.messaging.backend.data.request.SerializableRequest;

public interface RequestHandler {

	void handleRequest(SerializableRequest request);
}
