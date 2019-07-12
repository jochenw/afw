package com.github.jochenw.afw.core.data;

public interface IObjectComparator {
	public interface Listener {
		public void difference(String pContext, String pDescription);
	}

	public static class DefaultListener implements Listener {
		@Override
		public void difference(String pContext, String pDescription) {
			throw new IllegalStateException("At " + pContext + ": " + pDescription);
		}
	}

	public void compare(Listener pListener, Object pExpectedObject, Object pActualObject);
	public default void compare(Object pExpectedObject, Object pActualObject) {
		compare(new DefaultListener(), pExpectedObject, pActualObject);
	}
}
