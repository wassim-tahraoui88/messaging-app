package com.tahraoui.messaging.ui.controller;

import com.tahraoui.gui.component.DragComponent;
import com.tahraoui.gui.component.MaximizeComponent;
import com.tahraoui.gui.component.MinimizeComponent;
import com.tahraoui.gui.icons.old.GlyphIcon;
import com.tahraoui.gui.init.Bootstrapper;
import com.tahraoui.gui.text.TLabel;
import com.tahraoui.gui.util.Config;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class HeaderController {

	@FXML private BorderPane header;
	@FXML private TLabel label;
	@FXML private GlyphIcon<?> minimize, maxIcon, restoreIcon;

	@FXML public void initialize() {
		new DragComponent(header, Bootstrapper.getStage()).attach();
		new MinimizeComponent(minimize, Bootstrapper.getStage()).attach();
		new MaximizeComponent(maxIcon, Bootstrapper.getStage(), maxIcon, restoreIcon).attach();
		new MaximizeComponent(restoreIcon, Bootstrapper.getStage(), maxIcon, restoreIcon).attach();
		label.setText(Config.getInstance().getAppName());
	}

	public void onSettingsClicked() {}
	public void onExitClicked() { Bootstrapper.exit(); }
}
