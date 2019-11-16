package com.github.jochenw.afw.core.util;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Executor.Listener;

/** Utility class for executing external commands, aka starting
 * external processes.
 */
public class Processes {
	private static final Executor executor = new Executor();

	/** <p>Called to execute the command {@code pCmd}, using the environment {@code pEnv},
	 * in the directory {@code pDir}.</p>
	 * <p>Internally, this simply delegates to
	 * {@link Executor#run(Path, String[], String[], com.github.jochenw.afw.core.util.Executor.Listener)}.</p>
	 * @param pDir The directory, where to execute the command. May be null, in which
	 *   case the command is being executed in the current directory.
	 * @param pCmd The command to execute. For example, if you wish to execute the
	 *   command "cmd /c echo "Hello, world!", then you would pass the four strings
	 *   "cmd", "/c", "echo", and "\"Hello, world!\"".
	 * @param pEnv The environment variables to use for executing the external command.
	 *   May be null, in which case the current processes environment is inherited.
	 * @param pListener A listener object, which provides the arguments, which would
	 *   be required for calling {@link #run(Path, String[], String[], Consumer, Consumer, IntConsumer)}.
	 * @return The external processes exit code.
	 */
	public static int run(Path pDir, String[] pCmd, String[] pEnv, Listener pListener) {
		return executor.run(pDir, pCmd, pEnv, pListener);
	}
	
	/** <p>Called to execute the command {@code pCmd}, using the environment {@code pEnv},
	 * in the directory {@code pDir}.</p>
	 * <p>Internally, this simply delegates to
	 * {@link Executor#run(Path, String[], String[], Consumer, Consumer, IntConsumer)}.</p>
	 * @param pDir The directory, where to execute the command. May be null, in which
	 *   case the command is being executed in the current directory.
	 * @param pCmd The command to execute. For example, if you wish to execute the
	 *   command "cmd /c echo "Hello, world!", then you would pass the four strings
	 *   "cmd", "/c", "echo", and "\"Hello, world!\"".
	 * @param pEnv The environment variables to use for executing the external command.
	 *   May be null, in which case the current processes environment is inherited.
	 * @param pStdOutputConsumer A consumer for the external processes standard
	 *   output. May be null, in which case this output is being silently discarded.
	 * @param pErrOutputConsumer A consumer for the external processes error
	 *   output. May be null, in which case this output is being silently discarded.
	 * @param pExitCodeHandler A handler, which will be invoked, when the external
	 *   process has terminated. The handler will receive the external processes 
	 *   exit code as argument.
	 * @return The external processes exit code.
	 */
	public static int run(@Nullable Path pDir, @Nonnull String[] pCmd, @Nullable String[] pEnv,
	        @Nullable Consumer<InputStream> pStdOutputConsumer,
	        @Nullable Consumer<InputStream> pErrOutputConsumer,
	        @Nullable IntConsumer pExitCodeHandler) {
		return executor.run(pDir, pCmd, pEnv, pStdOutputConsumer, pErrOutputConsumer, pExitCodeHandler);
	}
}
