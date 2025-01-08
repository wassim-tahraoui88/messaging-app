package com.tahraoui.messaging.backend.data.request;

public record ConnectionRequest(int id, String username, String password) implements SerializableRequest { }
