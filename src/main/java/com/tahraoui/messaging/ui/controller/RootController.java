package com.tahraoui.messaging.ui.controller;

import com.tahraoui.gui.component.ResizeComponent;
import com.tahraoui.gui.init.Bootstrapper;
import com.tahraoui.gui.util.Config;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class RootController {

	@FXML
	private BorderPane root;

	@FXML public void initialize() {
		new ResizeComponent(root, Bootstrapper.getStage(), Config.getInstance().getResizeMargin()).attach();
//		new AFKComponent(root, Application.getStage(), (_) -> System.out.println("I am AFK..."), (_) -> System.out.println("I am back!")).attach();
		Bootstrapper.getStage().maximizedProperty().addListener((_, _, isMaximized) -> {
			var _class = root.getStyleClass();
			if (isMaximized) _class.remove("border-radius-20");
			else _class.add("border-radius-20");
		});
	}

}