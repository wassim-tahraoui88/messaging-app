package com.tahraoui.messaging.model.exception;

public class WritingFailedException extends AppException {
	public WritingFailedException() { super("Failed to write data to stream."); }
}
