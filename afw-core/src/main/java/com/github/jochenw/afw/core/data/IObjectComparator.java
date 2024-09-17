package com.github.jochenw.afw.core.data;


/**
 * A comparator for complex objects, or object tree structures.
 */
public interface IObjectComparator {
	/** A listener, which is being notified, if the comparator finds
	 * any difference. The complex objects are considered equal, if
	 * the listener isn't notified at all.
	 */
	public interface Listener {
		/**
		 * This method is being invoked in case of differences, that
		 * have been detected.
		 * @param pContext A description of the differences location.
		 * @param pDescription A textual description of the difference.
		 */
		public void difference(String pContext, String pDescription);
	}

	/**
	 * Default implementation of {@link Listener}, throwing an exception
	 * in case of any difference.
	 */
	public static class DefaultListener implements Listener {
		/** Creates a new instance.
		 */
		public DefaultListener() {}

		@Override
		public void difference(String pContext, String pDescription) {
			throw new IllegalStateException("At " + pContext + ": " + pDescription);
		}
	}

	/**
	 * Called to compare the expected object with the actual object, invoking
	 * the given {@link Listener} in case of differences.
	 * @param pListener The listener, which is being called in case of
	 *   differences.
	 * @param pExpectedObject The expected object.
	 * @param pActualObject The actual object.
	 * @see #compare(Object, Object)
	 */
	public void compare(Listener pListener, Object pExpectedObject, Object pActualObject);
	/**
	 * Called to compare the expected object with the actual object, invoking
	 * the {@link DefaultListener default listener} in case of differences.
	 * (In other words: Throws an exception in case of differences.)
	 * @param pExpectedObject The expected object.
	 * @param pActualObject The actual object.
	 * @see #compare(Listener, Object, Object)
	 */
	public default void compare(Object pExpectedObject, Object pActualObject) {
		compare(new DefaultListener(), pExpectedObject, pActualObject);
	}
}
