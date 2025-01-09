package com.tahraoui.messaging.backend.data.response;

public record MessageResponse(int senderId, String senderName, String content) implements SerializableResponse {
}
