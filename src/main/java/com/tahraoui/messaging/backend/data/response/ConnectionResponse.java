package com.tahraoui.messaging.backend.data.response;

import java.math.BigInteger;

public record ConnectionResponse(int id, BigInteger p, BigInteger g) implements SerializableResponse {}
