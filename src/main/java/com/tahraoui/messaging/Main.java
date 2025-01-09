package com.tahraoui.messaging;

import com.tahraoui.gui.init.Bootstrapper;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws IOException {
		Bootstrapper.getInstance().init(stage);
	}

	public static void main(String[] args) {
		launch(args);
//		test();
	}

}