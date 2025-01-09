package com.tahraoui.messaging.backend.data.request;

import java.security.PublicKey;

public record ConnectionEstablishmentRequest(int id, String username, String password, PublicKey key) implements SerializableRequest { }
