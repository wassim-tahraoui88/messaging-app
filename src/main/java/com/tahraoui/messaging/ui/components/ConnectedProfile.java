package com.tahraoui.messaging.ui.components;

import com.tahraoui.gui.text.TLabel;
import com.tahraoui.messaging.model.Connection;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class ConnectedProfile extends HBox {

	private final int connectionId;
	private ProfileListener listener;

	public ConnectedProfile(Connection user, boolean kickable) {
		this.connectionId = user.id();

		var username = new TLabel(user.username());
		username.getStyleClass().add("username");

		var kickButton = new Button("Kick");
		kickButton.getStyleClass().add("btn");
		kickButton.setOnAction(_ -> handleKick());

		setPadding(new Insets(0,0,0,5));
		getStyleClass().add("profile");
		getChildren().add(username);
		if (kickable) getChildren().add(kickButton);
	}

	public void setKickHandler(ProfileListener listener) { this.listener = listener; }

	private void handleKick() {
		if (listener != null)
			listener.kick();
	}

	public int getConnectionId() { return connectionId; }

	public interface ProfileListener {
		void kick();
	}
}
