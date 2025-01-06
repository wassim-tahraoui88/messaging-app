package com.tahraoui.messaging.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class WebSocketClient {

	private static final Logger LOGGER = LogManager.getLogger(WebSocketClient.class);

	private final WebSocket socket;

	public WebSocketClient(String serverUri, Consumer<String> onMessageReceived) {
		socket = HttpClient.newHttpClient().newWebSocketBuilder()
				.buildAsync(URI.create(serverUri), new WebSocketListener(onMessageReceived))
				.join();
	}

	public void sendMessage(String message) {
		socket.sendText(message, true);

	}
	public void close() {
		socket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing connection").join();

	}

	private record WebSocketListener(Consumer<String> onMessageReceived) implements WebSocket.Listener {

		@Override
		public void onOpen(WebSocket socket) {
			LOGGER.info("WebSocket connection established.");
			socket.request(1);
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			onMessageReceived.accept(data.toString());
			webSocket.request(1);
			return null;
		}

		@Override
		public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
			LOGGER.info("WebSocket connection closed: {}", reason);
			return null;
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			LOGGER.error("WebSocket error: {}", error.getMessage());
		}
	}
}
