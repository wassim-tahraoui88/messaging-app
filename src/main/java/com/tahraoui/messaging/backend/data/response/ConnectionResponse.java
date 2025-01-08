package com.tahraoui.messaging.backend.data.response;

import java.math.BigInteger;

public record ConnectionResponse(BigInteger p, BigInteger g, boolean success) implements SerializableResponse {}
