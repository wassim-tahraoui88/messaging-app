package com.tahraoui.messaging.ui.listener;

import com.tahraoui.messaging.model.Connection;

public interface ConnectionListener {
	void receiveConnection(Connection connection);
	void removeConnection(int id);
}
