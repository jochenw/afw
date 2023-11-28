package com.github.jochenw.afw.core.scripts;

import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.MutableBoolean;


/** Default implementation of {@link IScriptEngineRegistry}. As of this
 * writing, only the {@link GroovyScriptEngine} is registered automatically,
 * but others can be registered manually.
 */
public class DefaultScriptEngineRegisty implements IScriptEngineRegistry {
	/**
	 * Returns the default list of {@link IScriptEngine script engines}.
	 * As of this writing, the only engine is the {@link GroovyScriptEngine}.
	 * @return The default list of {@link IScriptEngine script engines}.
	 */
	public static List<IScriptEngine> getDefaultScriptEngines() {
		final List<IScriptEngine> engines = new ArrayList<IScriptEngine>();
		IScriptEngine groovyEngine;
		try {
			groovyEngine = new GroovyScriptEngine();
		} catch (Throwable t) {
			groovyEngine = null;
		}
		if (groovyEngine != null) {
			engines.add(groovyEngine);
		}
		return engines;
	}
	
	private List<IScriptEngine> engines;

	/**
	 * Returns the list of {@link IScriptEngine script engines}.
	 * Without manual intervention, these are just the
	 * {@link #getDefaultScriptEngines() default script engines}.
	 * @return The list of {@link IScriptEngine script engines}.
	 * @see #add(IScriptEngine)
	 * @see #clear()
	 */
	public synchronized List<IScriptEngine> getScriptEngines() {
		if (engines == null) {
			engines = getDefaultScriptEngines();
		}
		return engines;
	}

	@Override
	public void add(IScriptEngine pEngine) {
		if (engines == null) {
			engines = getDefaultScriptEngines();
		}
		engines.add(pEngine);
	}

	/** Clears the list of script engines.
	 */
	public void clear() {
		if (engines == null) {
			engines = getDefaultScriptEngines();
		}
		engines.clear();
	}
	
	@Override
	public void forEach(FailableConsumer<IScriptEngine, ?> pConsumer) {
		getDefaultScriptEngines().forEach((se) -> Functions.accept(pConsumer, se));
	}

	@Override
	public boolean isScriptFile(IReadable pReadable) {
		final MutableBoolean mb = new MutableBoolean();
		getScriptEngines().forEach((se) -> {
			if (se.isScriptable(pReadable)) {
				mb.set();
			}
		});
		return mb.isSet();
	}
}
