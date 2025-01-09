package com.tahraoui.messaging.ui.controller;

import com.tahraoui.gui.component.ResizeComponent;
import com.tahraoui.gui.init.Bootstrapper;
import com.tahraoui.gui.util.Config;
import com.tahraoui.messaging.backend.ConnectionService;
import com.tahraoui.messaging.ui.components.SidebarToggle;
import com.tahraoui.messaging.ui.listener.NavigationListener;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class RootController implements NavigationListener {

	private static RootController instance;
	public static RootController getInstance() { return instance; }

	@FXML
	private BorderPane root;
	@FXML
	private Pane sidebar;

	private SidebarToggle toggle;

	@FXML public void initialize() {
		instance = this;
		ConnectionService.getInstance().addNavigationListener(this);

		toggle = new SidebarToggle();
		toggle.setOnMouseClicked(_ -> toggleSidebar());
		toggle.setOnMouseClicked(_ -> toggleSidebar());
		toggle.setVisible(false);
		root.setLeft(toggle);

		new ResizeComponent(root, Bootstrapper.getStage(), Config.getInstance().getResizeMargin()).attach();
//		new AFKComponent(root, Application.getStage(), (_) -> System.out.println("I am AFK..."), (_) -> System.out.println("I am back!")).attach();
		Bootstrapper.getStage().maximizedProperty().addListener((_, _, isMaximized) -> {
			var _class = root.getStyleClass();
			if (isMaximized) _class.remove("border-radius-20");
			else _class.add("border-radius-20");
		});
	}

	public boolean toggleSidebar() {
		root.setLeft(root.getLeft() == sidebar ? toggle : sidebar);
		return root.getLeft() == sidebar;
	}

	@Override public void switchToHome() {
		root.setLeft(toggle);
		toggle.setVisible(false);
	}

	@Override public void switchToChatbox() {
		if (!ConnectionService.getInstance().isHost) return;
		toggle.setVisible(true);
		root.setLeft(sidebar);
	}
}