package com.github.jochenw.afw.core.util;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A component for executing commands as external processes.
 */
public class Executor {
	/** Interface of a listener, which is being notified about the external processes
	 * status.
	 */
	public interface Listener {
		/** Returns a consumer, which reads the external processes standard output.
		 * @return A consumer, which reads the external processes standard output.
		 *   May be null, in which case the processes standard output is being silently
		 *   discarded.
		 */
		@Nullable Consumer<InputStream> getStdOutputConsumer();
		/** Returns a consumer, which reads the external processes error output.
		 * @return A consumer, which reads the external processes error output.
		 *   May be null, in which case the processes standard output is being silently
		 *   discarded.
		 */
		@Nullable Consumer<InputStream> getErrorOutputConsumer();
		/**
		 * Called when the external process has finished.
		 * @param pStatus The external processes exit code.
		 */
		void accept(int pStatus);
	}
	/** Called to execute the command {@code pCmd}, using the environment {@code pEnv},
	 * in the directory {@code pDir}.
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
	public int run(@Nullable Path pDir, @Nonnull String[] pCmd, @Nullable String[] pEnv,
			       @Nonnull Listener pListener) {
		final @Nonnull Listener listener = Objects.requireNonNull(pListener, "Listener");
		return run(pDir, pCmd, pEnv, pListener.getStdOutputConsumer(),
				   pListener.getErrorOutputConsumer(),
				   (i) -> { listener.accept(i);});
	}

	/** Called to execute the command {@code pCmd}, using the environment {@code pEnv},
	 * in the directory {@code pDir}.
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
	public int run(@Nullable Path pDir, @Nonnull String[] pCmd, @Nullable String[] pEnv,
			        @Nullable Consumer<InputStream> pStdOutputConsumer,
			        @Nullable Consumer<InputStream> pErrOutputConsumer,
			        @Nullable IntConsumer pExitCodeHandler) {
		try {
			final File dir = pDir == null ? null : pDir.toFile();
			final Process pr = Runtime.getRuntime().exec(pCmd, pEnv, dir);
			startConsumer(pr.getInputStream(), pStdOutputConsumer);
			startConsumer(pr.getErrorStream(), pErrOutputConsumer);
			final int status = pr.waitFor();
			if (pExitCodeHandler != null) {
				pExitCodeHandler.accept(status);
			}
			return status;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected void startConsumer(@Nonnull InputStream pIn, Consumer<InputStream> pConsumer) {
		final Runnable runnable = () -> {
			if (pConsumer != null) {
				pConsumer.accept(pIn);
			}
		};
		final Thread t = new Thread(runnable, getThreadName());
		t.setDaemon(isUsingDaemonThreads());
		t.start();
	}

	private boolean isUsingDaemonThreads = true;

	/** Returns, whether the threads, which are created by this object, are daemon
	 * threads (true, default), or not.
	 * @return True, if the, which are created by this object, are daemon
	 * threads (default), otherwise false.
	 */
	public boolean isUsingDaemonThreads() {
		return isUsingDaemonThreads;
	}

	/** Sets, whether the threads, which are created by this object, are daemon
	 * threads (true, default), or not (false).
	 * @param pUsingDaemonThreads True, if the, which are created by this object, are daemon
	 * threads (default), otherwise false.
	 */
	public void setUsingDaemonThreads(boolean pUsingDaemonThreads) {
		isUsingDaemonThreads = pUsingDaemonThreads;
	}

	private final AtomicLong consumerId = new AtomicLong();
	/** Returns a new id, which is unique for this instance. This can be used to
	 * construct a new thread name in {@link #getThreadName()}.
	 * @return A new id, which is unique for this instance.
	 */
	protected long getConsumerId() {
		return consumerId.getAndIncrement();
	}
	/** Returns a new threads name. It is suggested to ensure, that the returned name
	 * is unique, at least for this instance by using {@link #getConsumerId()}, when
	 * onstructing the result.
	 * @return A new threads name. It is suggested to ensure, that the returned name
	 * is unique, at least for this instance by using {@link #getConsumerId()}, when
	 * onstructing the result.
	 */
	protected String getThreadName() {
		return "ExecOutputPuller" + getConsumerId();
	}
}
