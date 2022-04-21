package com.github.jochenw.afw.di.api;

/**
 * Interface of a module, that participates in the creation of bindings by
 * consuming, and using, the given {@link Binder binder}.
 */
public interface Module {
	/**
	 * Called to participate in the creation of bindings by
	 * consuming, and using, the given {@link Binder binder}.
	 * @param pBinder The binder, which may be called to create
	 * new binding builders.
	 */
	void configure(Binder pBinder);
}