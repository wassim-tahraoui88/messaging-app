package com.tahraoui.messaging.ui.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class Message extends HBox {

	private static final Boolean DEFAULT_RECEIVED = false;
	private StringProperty text;
	private BooleanProperty received;

	private final Label label;

	public Message(String text, boolean received) {
		this();
		setText(text);
		setReceived(received);
	}
	public Message() {
		this.label = new Label();
		this.label.setWrapText(true);
		this.label.getStyleClass().add("message");
		var styleClass = this.getStyleClass();
		styleClass.add("message-box");
		styleClass.add("sent");
		getChildren().add(this.label);
		initProperties();
	}
	private void initProperties() {
		receivedProperty().addListener(_ -> updateBackground());
		textProperty().addListener(_ -> updateText());
	}

	public final StringProperty textProperty() {
		if (this.text == null) this.text = new SimpleStringProperty("");
		return this.text;
	}
	public final BooleanProperty receivedProperty() {
		if (this.received == null) this.received = new SimpleBooleanProperty(this, "received", DEFAULT_RECEIVED);
		return this.received;
	}

	public final String getText() { return this.textProperty().getValue(); }
	public final void setText(String text) { this.textProperty().setValue(text); }
	public final Boolean getReceived() { return this.receivedProperty().getValue(); }
	public final void setReceived(Boolean state) { this.receivedProperty().setValue(state); }

	private void updateText() {
		this.label.setText(getText());

	}
	private void updateBackground() {
		var styleClass = this.getStyleClass();
		var received = getReceived();
		styleClass.add(received ? "received" : "sent");
		styleClass.remove(received ? "sent" : "received");
	}
}
