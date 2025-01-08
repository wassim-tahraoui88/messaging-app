package com.tahraoui.messaging.ui.controller;

import com.tahraoui.messaging.backend.data.response.MessageResponse;

public interface ChatBoxListener {
	void receiveMessage(MessageResponse message);
}
