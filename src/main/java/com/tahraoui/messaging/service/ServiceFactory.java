package com.tahraoui.messaging.service;

public class ServiceFactory {

	private static final AuthService AUTH_SERVICE = new AuthService();
	private static final ProfileService PROFILE_SERVICE = new ProfileService();
	private static final ChatService CHAT_SERVICE = new ChatService();

	public static AuthService getAuthService() { return AUTH_SERVICE; }
	public static ProfileService getProfileService() { return PROFILE_SERVICE; }
	public static ChatService getChatService() { return CHAT_SERVICE; }
}
