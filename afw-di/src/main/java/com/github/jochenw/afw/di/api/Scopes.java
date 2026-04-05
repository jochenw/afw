package com.github.jochenw.afw.di.api;


/** This class provides the possible scpoes, that a binding can take.
 */
public class Scopes {
	/** Private constructor, because this class contains only static fields.
	 */
	private Scopes() {}

	/** Implementation of a scope.
	 */
	public static class Scope {
		private final String name;
		/** Creates a new instance with the given name.
		 * @param pName The scopes name.
		 */
		protected Scope(String pName) {
			name = pName;
		}
		/** Returns the scopes name.
		 * @return The sopes name.
		 */
		public String name() { return name; }
	}

	/** The singleton scope: A binding with that scope will
	 * always provide one, and the same instance. That
	 * instance will be created upon the first request.
	 * @see #EAGER_SINGLETON
	 * @see #NO_SCOPE
	 */
	public static final Scope SINGLETON = new Scope("SINGLETON");
	/** The singleton scope: A binding with that scope will
	 * always provide one, and the same instance. Compared
	 * to the {@link #SINGLETON} scope however, that instance
	 * will be created immediately, and not upon the first
	 * request.
	 * @see #SINGLETON
	 * @see #NO_SCOPE
	 */
	public static final Scope EAGER_SINGLETON = new Scope("EAGER_SINGLETON");
	/** The "no scope": A binding with this scope will create a
	 * new instance upon every request.
	 * @see #SINGLETON
	 * @see #EAGER_SINGLETON
	 */
	public static final Scope NO_SCOPE = new Scope("NO_SCOPE");
}
