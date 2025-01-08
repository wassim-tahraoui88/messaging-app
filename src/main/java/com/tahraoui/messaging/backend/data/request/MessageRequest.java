package com.tahraoui.messaging.backend.data.request;

public record MessageRequest(String senderName, String content) implements SerializableRequest {
}
