package com.tahraoui.messaging.ui.controller;

import com.tahraoui.messaging.backend.ConnectionService;
import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.ui.components.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public class ChatBoxController implements ChatBoxListener {

	@FXML private VBox vbox_messages;
	@FXML private TextField textField_message;
	@FXML private Button btn_send;

	@FXML private void initialize() {
		btn_send.setOnAction(_ -> sendMessage());
		textField_message.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) sendMessage();
		});
		ConnectionService.getInstance().setChatBoxControllerListener(this);
	}
	private void sendMessage() {
		var message = textField_message.getText();
		if (message.isEmpty()) return;
		vbox_messages.getChildren().add(new Message(ConnectionService.getInstance().getUsername(), message,false));
		textField_message.clear();
	}

	@Override
	public void receiveMessage(MessageResponse message) {
		vbox_messages.getChildren().add(new Message("",message.content(),false));

	}
}
