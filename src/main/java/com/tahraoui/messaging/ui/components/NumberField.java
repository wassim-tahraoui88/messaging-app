package com.tahraoui.messaging.ui.components;


import javafx.scene.control.TextField;

public class NumberField extends TextField {

	public NumberField() {
		textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*"))
				setText(newValue.replaceAll("[^\\d]", ""));
		});
	}
}
