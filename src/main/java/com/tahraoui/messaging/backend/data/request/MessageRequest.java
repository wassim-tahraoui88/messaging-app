package com.tahraoui.messaging.backend.data.request;

public record MessageRequest(int senderId, String senderName, byte[] data) implements SerializableRequest { }