package com.tahraoui.messaging.ui.components;

import com.tahraoui.messaging.model.Connection;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ConnectedProfile extends HBox {

	private final int connectionId;
	private final Label username;

	public ConnectedProfile(Connection user) {
		this.connectionId = user.id();

		this.username = new Label(user.username());
		this.username.getStyleClass().add("username");

		this.getStyleClass().add("profile");
		getChildren().add(username);
	}

	public int getConnectionId() { return connectionId; }
}
