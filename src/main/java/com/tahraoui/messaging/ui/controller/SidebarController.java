package com.tahraoui.messaging.ui.controller;

import com.tahraoui.messaging.backend.ConnectionService;
import com.tahraoui.messaging.model.Connection;
import com.tahraoui.messaging.ui.components.ConnectedProfile;
import com.tahraoui.messaging.ui.listener.ConnectionListener;
import com.tahraoui.messaging.ui.listener.NavigationListener;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class SidebarController implements NavigationListener, ConnectionListener {

	@FXML private VBox root, vbox_connectedUsers;

	@FXML private void initialize() {
		ConnectionService.getInstance().addNavigationListener(this);

	}

	@Override
	public void receiveConnection(Connection connection) {
		vbox_connectedUsers.getChildren().add(new ConnectedProfile(connection));
	}
	@Override
	public void removeConnection(int id) {
		var children = vbox_connectedUsers.getChildren();
		children.removeIf(node -> ((ConnectedProfile) node).getConnectionId() == id);
	}

	@Override
	public void switchToHome() { root.setVisible(false); }
	@Override
	public void switchToChatbox() {
		root.setVisible(true);
		var connectionInstance = ConnectionService.getInstance();
		receiveConnection(new Connection(connectionInstance.getId(), connectionInstance.getUsername()));
	}
}
