package com.tahraoui.messaging.backend.data;

import com.tahraoui.messaging.backend.data.response.SerializableResponse;

public interface ResponseReader {
	void readResponse(SerializableResponse response);
}
