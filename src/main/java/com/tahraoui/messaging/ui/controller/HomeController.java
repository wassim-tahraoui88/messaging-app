package com.tahraoui.messaging.ui.controller;

import com.tahraoui.gui.popup.ModalFactory;
import com.tahraoui.messaging.backend.ConnectionService;
import com.tahraoui.messaging.backend.data.pojo.UserCredentials;
import com.tahraoui.messaging.ui.components.NumberField;
import com.tahraoui.messaging.util.NetworkUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class HomeController {

	@FXML private NumberField numberField_hostPort, numberField_joinPort;
	@FXML private TextField textField_hostUsername, textField_joinUsername;
	@FXML private PasswordField passwordField_hostPassword, passwordField_joinPassword;
	@FXML private Button button_host, button_join, button_hostPortVerify;
	private final Tooltip portStatus = new Tooltip();

	@FXML private void initialize() {
		portStatus.setContentDisplay(ContentDisplay.TOP);
		portStatus.setShowDuration(Duration.seconds(2));
		portStatus.setAutoHide(true);
		button_hostPortVerify.setOnAction(_ -> handleVerifyPort());

		button_host.setOnAction(_ -> handleHost());
		button_join.setOnAction(_ -> handleJoin());
	}

	private boolean verifyPort() {
		var port = numberField_hostPort.getText();
		try {
			return !port.isBlank() && NetworkUtils.isPortAvailable(Integer.parseInt(port));
		}
		catch (NumberFormatException _) {
			return false;
		}
	}
	private void handleVerifyPort() {
		portStatus.hide();
		portStatus.setText( verifyPort() ? "Port number is valid." :"Invalid port number...");
		var screenPosition = button_hostPortVerify.localToScreen(button_hostPortVerify.getBoundsInLocal());
		portStatus.show(button_hostPortVerify, screenPosition.getMaxX(), screenPosition.getMinY());
	}
	private void handleHost() {
		var port = numberField_hostPort.getText();
		var username = textField_hostUsername.getText();
		var password = passwordField_hostPassword.getText();
		if (port.isBlank() || username.isBlank() || password.isBlank()) {
			ModalFactory.showError("Connection Error","Please fill in all fields...");
			return;
		}

		if (verifyPort()) ConnectionService.getInstance().host(Integer.parseInt(port), new UserCredentials(username, password));
		else ModalFactory.showWarning("Connection Warning","Port number is invalid...");
	}
	private void handleJoin() {
		var port = numberField_joinPort.getText();
		var username = textField_joinUsername.getText();
		var password = passwordField_joinPassword.getText();
		if (port.isBlank() || username.isBlank() || password.isBlank()) {
			ModalFactory.showError("Connection Error","Please fill in all fields...");
			return;
		}
		try {
			ConnectionService.getInstance().join(Integer.parseInt(port), new UserCredentials(username, password));
		}
		catch (NumberFormatException _) {
			portStatus.setText("Invalid port number...");
			var screenPosition = button_join.localToScreen(button_join.getBoundsInLocal());
			portStatus.show(button_join, screenPosition.getMaxX(), screenPosition.getMinY());
		}
	}
}
