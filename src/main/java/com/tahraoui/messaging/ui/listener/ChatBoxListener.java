package com.tahraoui.messaging.ui.listener;

import com.tahraoui.messaging.backend.data.response.MessageResponse;
import com.tahraoui.messaging.backend.data.response.SystemMessageResponse;

public interface ChatBoxListener {
	void receiveMessage(MessageResponse message);
	void receiveSystemMessage(SystemMessageResponse message);
}
