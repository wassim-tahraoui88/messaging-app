package com.tahraoui.messaging.backend.data.response;

public record MessageResponse(String senderName, String content) implements SerializableResponse {
}
