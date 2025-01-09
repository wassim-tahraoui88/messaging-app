package com.tahraoui.messaging.backend.data.response;

import java.math.BigInteger;

public record ConnectionEstablishmentResponse(BigInteger p, BigInteger g, boolean success) implements SerializableResponse {}
