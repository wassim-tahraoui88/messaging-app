package com.tahraoui.messaging.backend.data.request;

public record MessageRequest(int senderId, String senderName, String content) implements SerializableRequest {
}
