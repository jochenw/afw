package com.github.jochenw.afw.di.api;


/** Interface of a {@link BindingBuilder} without scope. In other words,
 * a scope may be applied.
 */
public interface ScopableBindingBuilder {
	/** Sets the bindings scope to the given.
	 * @param pScope The bindings scope.
	 */
	void in(Scope pScope);
	/** Sets the bindings scope to {@link Scopes#EAGER_SINGLETON}.
	 */
	void asEagerSingleton();
}
