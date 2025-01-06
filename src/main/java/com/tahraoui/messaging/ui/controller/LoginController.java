package com.tahraoui.messaging.ui.controller;

import com.tahraoui.gui.popup.ModalFactory;
import com.tahraoui.messaging.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

	@FXML private TextField textField_username;
	@FXML private PasswordField textField_password;
	@FXML private Button button_submit;

	@FXML
	private void initialize() {
		button_submit.setOnAction(_ -> handleLogin());
	}
	private void handleLogin() {
		System.out.println("Logging In");
		var username = textField_username.getText();
		var password = textField_password.getText();
		if (username.isBlank() || password.isBlank()) {
			var modal = ModalFactory.showError("Login Error","Please fill in all fields...");
			if (modal == ModalFactory.CONFIRM_OPTION) return;
		}
		var response = ServiceFactory.getAuthService().attemptLogin(username, password);

	}
}
