package com.tahraoui.messaging.backend;

import com.tahraoui.messaging.ui.listener.NavigationListener;
import javafx.application.Platform;

import java.util.LinkedList;
import java.util.List;

public class Navigator {

	private final List<NavigationListener> navigationListeners;

	public Navigator() {
		this.navigationListeners = new LinkedList<>();

	}

	public void add(NavigationListener listener) {
		navigationListeners.add(listener);

	}

	public void switchToHome() {
		System.out.println("Request to switch to home");
		Platform.runLater(() -> {
			for (var listener : navigationListeners) listener.switchToHome();
		});
	}
	public void switchToChatbox() {
		if (!ConnectionService.getInstance().isHost) return;
		System.out.println("Request to switch to chatbox");
		Platform.runLater(() -> {
			for (var listener : navigationListeners) listener.switchToChatbox();
		});
	}
}
