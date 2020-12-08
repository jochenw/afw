package com.github.jochenw.afw.core.scripts;

import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;

public interface IScriptEngineRegistry {
	boolean isScriptFile(IReadable of);
	void forEach(FailableConsumer<IScriptEngine,?> pEngine);
	void add(IScriptEngine pEngine);

}
