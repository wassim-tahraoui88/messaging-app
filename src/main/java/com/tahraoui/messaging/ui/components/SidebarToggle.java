package com.tahraoui.messaging.ui.components;

import javafx.geometry.Orientation;
import javafx.scene.control.Separator;


public class SidebarToggle extends Separator {

	public SidebarToggle() {
		setOrientation(Orientation.VERTICAL);
		setStyle("-fx-background-color: #000000;" +
				"-fx-padding: 0 0 0 0;" +
				"-fx-margin: 0 0 0 0;" +
				"-fx-min-width: 5;");

	}

}
