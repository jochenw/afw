package com.github.jochenw.afw.core.scripts;

import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.Functions;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.MutableBoolean;

public class DefaultScriptEngineRegisty implements IScriptEngineRegistry {
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
