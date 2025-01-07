package com.tahraoui.messaging.model.exception;

public class WrongPasswordException extends AppException {
	public WrongPasswordException() { super("The password you entered is incorrect."); }
}
