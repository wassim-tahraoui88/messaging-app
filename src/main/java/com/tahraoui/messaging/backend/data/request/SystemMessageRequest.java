package com.tahraoui.messaging.backend.data.request;

public record SystemMessageRequest(String content) implements SerializableRequest { }
