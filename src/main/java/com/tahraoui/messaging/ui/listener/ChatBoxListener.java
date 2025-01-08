package com.tahraoui.messaging.ui.listener;

import com.tahraoui.messaging.backend.data.response.MessageResponse;

public interface ChatBoxListener {
	void receiveMessage(MessageResponse message);
}
