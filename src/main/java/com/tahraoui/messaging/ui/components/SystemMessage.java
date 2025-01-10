package com.tahraoui.messaging.ui.components;

import com.tahraoui.gui.text.TLabel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.HBox;

public class SystemMessage extends HBox {

	private StringProperty text;

	private final TLabel label;

	public SystemMessage(String text) {
		this();
		setText(text);
	}
	public SystemMessage() {
		this.label = new TLabel();
		this.label.setWrapText(true);
		this.label.getStyleClass().add("message");

		this.getStyleClass().add("system-message");

		getChildren().add(label);
		initProperties();
	}

	private void initProperties() {
		textProperty().addListener(_ -> updateText());

	}

	public final StringProperty textProperty() {
		if (this.text == null) this.text = new SimpleStringProperty("");
		return this.text;
	}
	public final String getText() { return this.textProperty().getValue(); }
	public final void setText(String value) { this.textProperty().setValue(value); }

	private void updateText() {
		this.label.setText(getText());

	}
}
