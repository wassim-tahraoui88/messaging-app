package com.tahraoui.messaging.backend.data.response;

import java.io.Serializable;
import java.math.BigInteger;

public record ConnectionResponse(BigInteger p, BigInteger g) implements Serializable {
}
