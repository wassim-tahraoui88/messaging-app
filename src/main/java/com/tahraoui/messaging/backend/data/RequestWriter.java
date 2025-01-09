package com.tahraoui.messaging.backend.data;

import com.tahraoui.messaging.backend.data.request.SerializableRequest;

public interface RequestWriter {
	void writeRequest(SerializableRequest request);
	byte[] encryptMessage(String message);
	String decryptMessage(byte[] data);
}
