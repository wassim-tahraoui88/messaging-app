package com.tahraoui.messaging.ui.controller;

import com.tahraoui.messaging.ui.components.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public class ChatBoxController {

	@FXML private VBox vbox_messages;
	@FXML private TextField textField_message;
	@FXML private Button btn_send;

	public void initialize() {
		btn_send.setOnAction(_ -> sendMessage());
		textField_message.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) sendMessage();
		});
	}

	private void sendMessage() {
		var message = textField_message.getText();
		if (message.isEmpty()) return;
		vbox_messages.getChildren().add(new Message(message,false));
		textField_message.clear();
	}
}
