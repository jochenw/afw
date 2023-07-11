package com.github.jochenw.afw.core.scripts;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.io.IReadable;


/** A script engine is used to
 * <ol>
 *   <li>{@link #getScript(IReadable, Charset) Parse} script files, that are written in the script engines programming language,
 *     creating instances of {@link Script}.</li>
 *   <li>{@link Script#run(Map)} the created instance of {@link Script} to execute the script, that has
 *     been parsed, applying a given data model.</li>
 * </ol>
 * @author jwi
 *
 */
public interface IScriptEngine {
	/**
	 * A parsed, and possibly compiled instance of a script file, that is ready for execution.
	 */
	public interface Script {
		/**
		 * Invokes the script, that returns a result value.
		 * @param <O> The result type.
		 * @param pParameters The data model, that is being applied by the script.
		 * @return The result object, that has been returned by the script.
		 */
		public @Nullable <O> O call(@Nullable Map<String,Object> pParameters);
		/**
		 * Invokes a script, that doesn't return a result value.
		 * @param pParameters The data model, that is being applied by the script.
		 */
		public void run(@Nullable Map<String,Object> pParameters);
	}
	/**
	 * Returns, whether the given {@link IReadable} contains a script, that can be
	 * evaluated by the engine. In practice, this will not do very much. For
	 * example, in the case of the {@link GroovyScriptEngine}, this will simply
	 * check, whether the file name ends with ".groovy".
	 * @param pReadable A representation of the script file.
	 * @return True, if the engine is ready to parse the {@link IReadable} as a
	 *   script.
	 */
	public boolean isScriptable(@Nonnull IReadable pReadable);
	/**
	 * Reads, and parses the given {@link IReadable} as a script, converting it
	 * into an instance of {@link Script}.
	 * @param pReadable A representation of the script file.
	 * @param pCharset The script files character set. May be null, in which
	 *   case {@link StandardCharsets#UTF_8} is used as the default.
	 * @return An executable representation of the script file.
	 * @throws RuntimeException Reading, or parsing the script file failed.
	 *   Possible reasons include I/O errors, or syntax errors.
	 */
	public Script getScript(@Nonnull IReadable pReadable, @Nullable Charset pCharset);
}
