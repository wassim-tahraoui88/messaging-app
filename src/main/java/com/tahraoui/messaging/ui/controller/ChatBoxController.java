package com.tahraoui.messaging.ui.controller;

import com.tahraoui.messaging.backend.ConnectionService;
import com.tahraoui.messaging.backend.data.request.KickRequest;
import com.tahraoui.messaging.backend.data.request.MessageRequest;
import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.backend.data.response.SystemMessageResponse;
import com.tahraoui.messaging.ui.components.Message;
import com.tahraoui.messaging.ui.components.SystemMessage;
import com.tahraoui.messaging.ui.listener.ChatBoxListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public class ChatBoxController implements ChatBoxListener {

	@FXML private VBox vbox_messages;
	@FXML private TextField textField_message;
	@FXML private Button btn_send;
	@FXML private Label label_username;

	@FXML private void initialize() {
		btn_send.setOnAction(_ -> sendMessage());
		textField_message.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) sendMessage();
		});
		var connectionInstance = ConnectionService.getInstance();
		label_username.setText(connectionInstance.getUsername());
		connectionInstance.setChatBoxControllerListener(this);
	}
	private void sendMessage() {
		var message = textField_message.getText();
		if (message.isEmpty()) return;
		var connectionInstance = ConnectionService.getInstance();
		connectionInstance.writeRequest(new MessageRequest(connectionInstance.getId(), connectionInstance.getUsername(), message));
		textField_message.clear();
	}

	@Override
	public void receiveMessage(MessageResponse message) {
		var connectionInstance = ConnectionService.getInstance();
		var messageItem = new Message(message.senderId(), message.senderName(), message.content(),
				message.senderId() != connectionInstance.getId(),
				connectionInstance.isHost);
		if (connectionInstance.isHost) messageItem.setMessageListener(id -> connectionInstance.writeRequest(new KickRequest(id, message.senderName())));
		vbox_messages.getChildren().add(messageItem);
	}

	@Override
	public void receiveSystemMessage(SystemMessageResponse message) {
		vbox_messages.getChildren().add(new SystemMessage(message.content()));
	}
}
