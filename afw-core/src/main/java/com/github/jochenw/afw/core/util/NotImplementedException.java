package com.github.jochenw.afw.core.util;

public class NotImplementedException extends IllegalStateException {
	private static final long serialVersionUID = -7402852484207080555L;

	public NotImplementedException(String pMsg) {
		super(pMsg);
	}
	public NotImplementedException() {
		this("Not implemented.");
	}
}
