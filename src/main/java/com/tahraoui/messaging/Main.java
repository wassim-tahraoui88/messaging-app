package com.tahraoui.messaging;

import com.tahraoui.gui.init.Bootstrapper;
import com.tahraoui.messaging.util.Utils;
import com.tahraoui.messaging.util.WebSocketClient;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws IOException {
		var socket = new WebSocketClient(Utils.SERVER_URL, (message) -> {
			System.out.printf("Received: %s", message);
		});
		Bootstrapper.getInstance().init(stage);
	}

	public static void main(String[] args) {
		launch(args);
//		test();
	}

	public static void test() { }
}