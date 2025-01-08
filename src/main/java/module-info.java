module com.tahraoui.messaging {
	requires javafx.controls;
	requires javafx.fxml;
	requires com.tahraoui.gui;

	requires org.controlsfx.controls;
	requires com.dlsc.formsfx;
	requires net.synedra.validatorfx;

	requires batik.util;
	requires batik.dom;
	requires commons.logging;
	requires java.net.http;
	requires org.apache.logging.log4j.core;

	opens com.tahraoui.messaging to javafx.fxml;
	opens com.tahraoui.messaging.ui.controller to javafx.fxml;
	opens com.tahraoui.messaging.ui.components to javafx.fxml;

	opens config;
	opens fonts;
	opens styles;
	opens views;

	exports com.tahraoui.messaging;
	exports com.tahraoui.messaging.util;
	opens com.tahraoui.messaging.ui.listener to javafx.fxml;
}