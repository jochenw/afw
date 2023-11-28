package com.github.jochenw.afw.core.util;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.scripts.DefaultScriptEngineRegisty;
import com.github.jochenw.afw.core.scripts.IScriptEngineRegistry;
import com.github.jochenw.afw.core.scripts.IScriptEngine.Script;

/**
 * Utility class for working with templates.
 */
public class Scripts {
	private static @Nonnull IScriptEngineRegistry scriptEngineRegistry = new DefaultScriptEngineRegisty();


	/**
	 * Returns the default {@link IScriptEngineRegistry script engine registry}.
	 * @return The default {@link IScriptEngineRegistry script engine registry}.
	 */
	public synchronized static @Nonnull IScriptEngineRegistry getScriptEngineRegistry() {
		return scriptEngineRegistry;
	}

	/**
	 * Sets the default {@link IScriptEngineRegistry script engine registry}.
	 * @param pScriptEngineRegistry The new default {@link IScriptEngineRegistry script engine registry},
	 *   replacing the current.
	 */
	public static synchronized void setScriptEngineRegistry(@Nonnull IScriptEngineRegistry pScriptEngineRegistry) {
		Scripts.scriptEngineRegistry = Objects.requireNonNull(pScriptEngineRegistry);
	}

	/** Called to read a script from the given URL, and compile it.
	 * @param pUrl The URL, from which the script is being read.
	 * @param pCharset The scripts character set. May be null, in which case
	 *   {@link StandardCharsets#UTF_8} is being used as the default.
	 * @return The compiled script.
	 */
	public static Script compile(URL pUrl, Charset pCharset) {
		return compile(IReadable.of(pUrl), pCharset);
	}

	/** Called to read a script from the given {@link IReadable}, and compile it.
	 * @param pReadable The readable, from which the script is being read.
	 * @param pCharset The scripts character set. May be null, in which case
	 *   {@link StandardCharsets#UTF_8} is being used as the default.
	 * @return The compiled script.
	 */
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

	/**
	 * Returns, whether the given file is considered to contain a script, based on the
	 * file's extension.
	 * @param pPath The file, which is checked for being a script.
	 * @return True, if the file can be compiled into a script.
	 */
	public static boolean isScriptFile(Path pPath) {
		return getScriptEngineRegistry().isScriptFile(IReadable.of(pPath));
	}

}
