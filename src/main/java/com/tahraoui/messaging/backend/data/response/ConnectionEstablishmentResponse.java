package com.tahraoui.messaging.backend.data.response;

import java.security.PublicKey;

public record ConnectionEstablishmentResponse(PublicKey key, byte[] secret, byte[] iv, boolean success) implements SerializableResponse {}
