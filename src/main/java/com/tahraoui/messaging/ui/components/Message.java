package com.tahraoui.messaging.ui.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class Message extends VBox {

	private static final Boolean DEFAULT_RECEIVED = false;
	private StringProperty senderName;
	private StringProperty messageContent;
	private BooleanProperty received;

	private final Label senderLabel, contentLabel;
	private MessageListener messageListener;

	public Message(int senderId, String senderName, String text, boolean received, boolean isAdmin) {
		this();

		if (senderName == null || senderName.isBlank()) getChildren().remove(senderLabel);
		else setSenderName(senderName);

		if (isAdmin) this.senderLabel.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
				if (messageListener != null)
					messageListener.kickUser(senderId);
			}
		});

		setMessageContent(text);
		setReceived(received);
	}

	public Message() {
		this.senderLabel = new Label();
		this.senderLabel.getStyleClass().add("sender");
		this.senderLabel.setTooltip(new Tooltip("Double click to kick from the chat."));


		this.contentLabel = new Label();
		this.contentLabel.setWrapText(true);
		this.contentLabel.getStyleClass().add("message");

		var styleClass = this.getStyleClass();
		styleClass.add("message-container");
		styleClass.add("sent");

		var hBox = new HBox(contentLabel);
		hBox.getStyleClass().add("message-box");
		VBox.setVgrow(hBox, Priority.ALWAYS);

		getChildren().add(senderLabel);
		getChildren().add(hBox);
		initProperties();
	}
	private void initProperties() {
		receivedProperty().addListener(_ -> updateBackground());
		messageContentProperty().addListener(_ -> updateMessageContent());
		senderNameProperty().addListener(_ -> updateSenderName());
	}

	public final StringProperty senderNameProperty() {
		if (this.senderName == null) this.senderName = new SimpleStringProperty("");
		return this.senderName;
	}
	public final StringProperty messageContentProperty() {
		if (this.messageContent == null) this.messageContent = new SimpleStringProperty("");
		return this.messageContent;
	}
	public final BooleanProperty receivedProperty() {
		if (this.received == null) this.received = new SimpleBooleanProperty(this, "received", DEFAULT_RECEIVED);
		return this.received;
	}

	public final String getSenderName() { return this.senderNameProperty().getValue(); }
	public final void setSenderName(String value) { this.senderNameProperty().setValue(value); }

	public final String getMessageContent() { return this.messageContentProperty().getValue(); }
	public final void setMessageContent(String value) { this.messageContentProperty().setValue(value); }

	public final Boolean getReceived() { return this.receivedProperty().getValue(); }
	public final void setReceived(Boolean state) { this.receivedProperty().setValue(state); }

	private void updateSenderName() {
		this.senderLabel.setText(getSenderName());

	}
	private void updateMessageContent() {
		this.contentLabel.setText(getMessageContent());

	}
	private void updateBackground() {
		var styleClass = this.getStyleClass();
		var received = getReceived();
		styleClass.add(received ? "received" : "sent");
		styleClass.remove(received ? "sent" : "received");
	}

	public void setMessageListener(MessageListener messageListener) { this.messageListener = messageListener; }

	public interface MessageListener {
		void kickUser(int userId);
	}
}
