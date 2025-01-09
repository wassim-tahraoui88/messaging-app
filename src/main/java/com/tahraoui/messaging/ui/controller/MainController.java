package com.tahraoui.messaging.ui.controller;

import com.tahraoui.messaging.backend.ConnectionService;
import com.tahraoui.messaging.ui.listener.NavigationListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainController implements NavigationListener {

	private enum Page { HOME, CHATBOX }

	@FXML private BorderPane root;

	@FXML private void initialize() {
		switchToHome();
		ConnectionService.getInstance().addNavigationListener(this);
	}

	@FXML public void switchToHome() { switchPage(Page.HOME); }
	@FXML public void switchToChatbox() { switchPage(Page.CHATBOX); }

	private void switchPage(Page page) {
		try {
			var path = "/views/content/pages/%s.fxml".formatted(page.name().toLowerCase());
			FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
			Pane newPage = loader.load();
			root.setCenter(newPage);
		}
		catch (IOException e) {
			System.out.println("Test");
		}
	}
}
