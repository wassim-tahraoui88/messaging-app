package com.tahraoui.messaging.model.exception;

public class ReadingFailedException extends AppException {
	public ReadingFailedException() { super("Failed to read data from stream."); }
}
