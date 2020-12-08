package com.github.jochenw.afw.core.util;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.scripts.DefaultScriptEngineRegisty;
import com.github.jochenw.afw.core.scripts.IScriptEngineRegistry;
import com.github.jochenw.afw.core.scripts.IScriptEngine.Script;

public class Scripts {
	private static @Nonnull IScriptEngineRegistry scriptEngineRegistry = new DefaultScriptEngineRegisty();

	public synchronized static @Nonnull IScriptEngineRegistry getScriptEngineRegistry() {
		return scriptEngineRegistry;
	}

	public static synchronized void setScriptEngineRegistry(@Nonnull IScriptEngineRegistry pScriptEngineRegistry) {
		Scripts.scriptEngineRegistry = Objects.requireNonNull(pScriptEngineRegistry);
	}

	public static Script compile(URL pUrl, Charset pCharset) {
		return compile(IReadable.of(pUrl), pCharset);
	}

	public static Script compile(IReadable pReadable, Charset pCharset) {
		final MutableBoolean found = new MutableBoolean();
		final Holder<Script> holder = new Holder<Script>();
		getScriptEngineRegistry().forEach((se) -> {
			found.set();
			if (holder.get() == null) {
				if (se.isScriptable(pReadable)) {
					holder.set(se.getScript(pReadable, pCharset));
				}
			}
		});
		final Script script = holder.get();
		if (script == null) {
			if (!found.isSet()) {
				throw new NoSuchElementException("No script engine is available.");
			}
			throw new IllegalStateException("No suitable script engine found for " + pReadable.getName());
		}
		return script;
	}

	public static boolean isScriptFile(Path pPath) {
		return getScriptEngineRegistry().isScriptFile(IReadable.of(pPath));
	}

}
