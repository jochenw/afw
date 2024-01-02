package com.github.jochenw.afw.di.api;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
	void configure(@NonNull Binder pBinder);

	/**
	 * Creates a module, that extends the current module by calling
	 * the given module.
	 * @param pModule The module, that is extending the current
	 *   module. May be null, in which case the current module
	 *   itself is being returned.
	 * @return A new module, which works by internally calling the
	 *   current module, and then the given (in that order).
	 */
	public default Module extend(@Nullable Module pModule) {
		if (pModule == null) {
			return this;
		}
		return (b) -> {
			configure(b);
			pModule.configure(b);
		};
	}

	/**
	 * Creates a module, that extends the current module by calling
	 * the given modules.
	 * @param pModules The modules, that are extending the current
	 *   module. May be null, in which case the current module
	 *   itself is being returned.
	 * @return A new module, which works by internally calling the
	 *   current module, and then the given (in that order).
	 */
	public default Module extend(@Nullable Module... pModules) {
		if (pModules == null) {
			return this;
		}
		return (b) -> {
			configure(b);
			for (Module m : pModules) {
				m.configure(b);
			}
		};
	}

	/**
	 * Creates a module, that extends the current module by calling
	 * the given modules.
	 * @param pModules The modules, that are extending the current
	 *   module. May be null, in which case the current module
	 *   itself is being returned.
	 * @return A new module, which works by internally calling the
	 *   current module, and then the given (in that order).
	 */
	public default Module extend(@Nullable Iterable<Module> pModules) {
		if (pModules == null) {
			return this;
		}
		return (b) -> {
			configure(b);
			for (Module m : pModules) {
				m.configure(b);
			}
		};
	}
}