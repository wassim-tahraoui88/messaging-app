package com.tahraoui.messaging.ui.controller;

import com.tahraoui.messaging.backend.ConnectionService;
import com.tahraoui.messaging.backend.data.request.KickRequest;
import com.tahraoui.messaging.model.Connection;
import com.tahraoui.messaging.ui.components.ConnectedProfile;
import com.tahraoui.messaging.ui.components.SidebarToggle;
import com.tahraoui.messaging.ui.listener.ConnectionListener;
import com.tahraoui.messaging.ui.listener.NavigationListener;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SidebarController implements NavigationListener, ConnectionListener {

	@FXML private BorderPane root;
	@FXML private VBox vbox_connectedUsers;
	private SidebarToggle toggle;

	@FXML private void initialize() {
		ConnectionService.getInstance().addNavigationListener(this);
		toggle = new SidebarToggle();
		toggle.setOnMouseClicked(_ -> toggleSidebar());
	}

	private void toggleSidebar() {
		var state = RootController.getInstance().toggleSidebar();
		root.setRight(state ? null : toggle);
	}

	@Override
	public void receiveConnection(Connection connection) {
		var connectedProfile = new ConnectedProfile(connection, connection.id() != 0);
		if (connection.id() != 0) connectedProfile.setKickHandler(() -> ConnectionService.getInstance().writeRequest(new KickRequest(connection.id(), connection.username())));

		vbox_connectedUsers.getChildren().add(connectedProfile);
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
		if (!ConnectionService.getInstance().isHost) return;

		root.setRight(toggle);

		var connectionInstance = ConnectionService.getInstance();
		receiveConnection(new Connection(connectionInstance.getId(), connectionInstance.getUsername()));
	}
}
