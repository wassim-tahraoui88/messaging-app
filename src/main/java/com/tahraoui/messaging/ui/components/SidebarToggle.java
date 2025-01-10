package com.tahraoui.messaging.ui.components;

import javafx.geometry.Orientation;
import javafx.scene.control.Separator;


public class SidebarToggle extends Separator {

	public SidebarToggle() {
		setOrientation(Orientation.VERTICAL);
		getStyleClass().add("sidebar-toggle");
	}

}
