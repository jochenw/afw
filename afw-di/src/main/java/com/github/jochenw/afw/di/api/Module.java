package com.github.jochenw.afw.di.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	void configure(@Nonnull Binder pBinder);

	/**
	 * Creates a module, that extends the current module by calling
	 * the given module.
	 */
	public default Module extend(@Nullable Module pModule) {
		return (b) -> {
			configure(b);
			if (pModule != null) {
				pModule.configure(b);
			}
		};
	}

	/**
	 * Creates a module, that extends the current module by calling
	 * the given modules.
	 */
	public default Module extend(@Nullable Module... pModules) {
		return (b) -> {
			configure(b);
			if (pModules != null) {
				for (Module m : pModules) {
					m.configure(b);
				}
			}
		};
	}

	/**
	 * Creates a module, that extends the current module by calling
	 * the given modules.
	 */
	public default Module extend(@Nullable Iterable<Module> pModules) {
		return (b) -> {
			configure(b);
			if (pModules != null) {
				for (Module m : pModules) {
					m.configure(b);
				}
			}
		};
	}
}