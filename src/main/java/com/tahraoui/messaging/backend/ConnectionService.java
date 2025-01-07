package com.tahraoui.messaging.backend;

import com.tahraoui.messaging.backend.client.Client;
import com.tahraoui.messaging.backend.host.Host;

import java.io.IOException;

public class ConnectionService {

	public static void host(int port, String password) {
		var host = new Host(port, password);

	}
	public static void join(int port, String password) {
		try {
			var client = new Client(port, password);
		}
		catch (IOException _) {

		}
	}
}
