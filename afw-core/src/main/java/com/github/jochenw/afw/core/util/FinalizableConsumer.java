package com.github.jochenw.afw.core.util;

import java.util.function.Consumer;

public class FinalizableConsumer<T> implements Consumer<T> {
	/** Called for success.
	 */
	public void finished() {
	}
	/**  Called for error.
	 */
	public void error(Throwable pThrowable) {
		throw Exceptions.show(pThrowable);
	}
	@Override
	public void accept(T pValue) {
	}
}
