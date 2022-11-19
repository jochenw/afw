package com.github.jochenw.afw.core.scripts;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.Scripts;


/** A {@link IScriptEngineRegistry} is a registry, that manages
 * instance of {@link IScriptEngine}. The standard use case is
 * the {@link Scripts#getScriptEngineRegistry() default script
 * engine registry}.
 */
public interface IScriptEngineRegistry {
	/**
	 * Returns, whether the registry contains a {@link IScriptEngine},
	 * that would accept the given {@link IReadable file} as a script
	 * file.
	 * @param pReadable True, if the registry contains a
	 *   {@link IScriptEngine}, that accepts the given
	 *   {@link IReadable file} as a script file. 
	 * @return True, if the registry contains a {@link IScriptEngine},
	 * that would accept the given {@link IReadable file} as a script
	 * file. Otherwise false.
	 */
	boolean isScriptFile(IReadable pReadable);
	/** Invokes the given {@link FailableConsumer} for every registered
	 * script engine.
	 * @param pConsumer The consumer, that is beig invoked for every
	 *   registered engine.
	 */
	void forEach(FailableConsumer<IScriptEngine,?> pConsumer);
	/** Adds an engine to the registries list of engines.
	 * @param pEngine The script engine, which is being added to the
	 *   registry.
	 */
	void add(IScriptEngine pEngine);
}
