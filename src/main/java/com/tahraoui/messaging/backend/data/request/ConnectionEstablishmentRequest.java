package com.tahraoui.messaging.backend.data.request;

public record ConnectionEstablishmentRequest(int id, String username, String password) implements SerializableRequest { }
