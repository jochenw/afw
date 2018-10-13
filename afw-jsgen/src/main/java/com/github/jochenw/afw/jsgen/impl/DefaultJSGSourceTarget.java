package com.github.jochenw.afw.jsgen.impl;

import java.io.Closeable;
import java.io.IOException;


public class DefaultJSGSourceTarget implements JSGSourceTarget {
	private final Appendable appendable;
	private String lineTerminator = "\n";

	public DefaultJSGSourceTarget(Appendable pAppendable) {
		appendable = pAppendable;
	}

	public Appendable getAppendable() {
		return appendable;
	}


	@Override
	public void write(Object pObject) throws IOException {
		appendable.append(pObject.toString());
	}

	@Override
	public void newLine() throws IOException {
		appendable.append(lineTerminator);
	}

	@Override
	public void close() throws IOException {
		if (appendable instanceof Closeable) {
			((Closeable) appendable).close();
		}
	}
}
