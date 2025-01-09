package com.tahraoui.messaging.backend.data.request;

public record KickRequest(int userId, String username) implements SerializableRequest { }
