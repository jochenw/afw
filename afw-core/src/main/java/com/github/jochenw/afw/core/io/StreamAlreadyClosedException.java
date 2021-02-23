package com.github.jochenw.afw.core.io;

import java.io.IOException;

public class StreamAlreadyClosedException extends IOException {
	private static final long serialVersionUID = 4995937849536256420L;

	public StreamAlreadyClosedException() {
		super("Stream cloed");
	}

	public StreamAlreadyClosedException(String pMessage, Throwable pCause) {
		super(pMessage, pCause);
	}

	public StreamAlreadyClosedException(String pMessage) {
		super(pMessage);
	}

	public StreamAlreadyClosedException(Throwable pCause) {
		super("Stream closed", pCause);
	}

}
