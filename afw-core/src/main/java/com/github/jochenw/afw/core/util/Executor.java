package com.github.jochenw.afw.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Functions.FailableConsumer;

/** A component for executing commands as external processes.
 */
public class Executor {
	private static final @Nonnull ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);

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

	/** A consumer, which copies it's input to {@link System#out}, suitable as a
	 * default value for {@link #run(Path, String[], String[], Consumer, Consumer, IntConsumer)}.
	 */
	public static final Consumer<InputStream> CONSUMER_COPY_TO_SYSTEM_OUT = (in) -> {
		Streams.copy(in, System.out);
	};
	/** A consumer, which copies it's input to {@link System#out}, suitable as a
	 * default value for {@link #run(Path, String[], String[], Consumer, Consumer, IntConsumer)}.
	 */
	public static final Consumer<InputStream> CONSUMER_COPY_TO_SYSTEM_ERR = (in) -> {
		Streams.copy(in, System.err);
	};

	private ExecutorService executorService;

	/** Sets the {@link ExecutorService}, which is being used to launch threads. May be null,
	 * in which case a {@link Executor#DEFAULT_EXECUTOR_SERVICE default executor service}
	 * will be used.
	 * @param pExecutorService The {@link ExecutorService}, which is being used to launch threads. May be null,
	 * in which case a {@link Executor#DEFAULT_EXECUTOR_SERVICE default executor service}
	 * will be used. 
	 */
	public void setExecutorService(@Nullable ExecutorService pExecutorService) {
		executorService = pExecutorService;
	}
	/** Returns the {@link ExecutorService}, which is being used to launch threads. By default,
	 * a {@link Executor#DEFAULT_EXECUTOR_SERVICE default executor service} will be used.
	 * @return The {@link ExecutorService}, which is being used to launch threads. Never null,
	 * because a {@link Executor#DEFAULT_EXECUTOR_SERVICE default executor service}
	 * is available. 
	 */
	public @Nonnull ExecutorService getExecutorService() {
		if (executorService == null) {
			return DEFAULT_EXECUTOR_SERVICE;
		} else {
			return executorService;
		}
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
			       @Nullable Listener pListener) {
		final @Nonnull Listener listener = Objects.notNull(pListener, this::getDefaultListener);
		return run(pDir, pCmd, pEnv, pListener.getStdOutputConsumer(),
				   pListener.getErrorOutputConsumer(),
				   (i) -> { listener.accept(i);});
	}

	protected @Nonnull Listener getDefaultListener() {
		return new Listener() {
			@Override
			public Consumer<InputStream> getStdOutputConsumer() {
				return null;
			}

			@Override
			public Consumer<InputStream> getErrorOutputConsumer() {
				return null;
			}

			@Override
			public void accept(int pStatus) {
				// Do nothing.
			}
		};
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
			final List<Callable<Object>> callables = new ArrayList<>();
			final MutableInteger status = new MutableInteger();
			callables.add(startConsumer(pr.getInputStream(), pStdOutputConsumer));
			callables.add(startConsumer(pr.getErrorStream(), pErrOutputConsumer));
			if (input != null) {
				callables.add(() -> {
					Functions.accept(input, pr.getOutputStream());
					return null;
				});
			}
			callables.add(() -> {
				final int stat = pr.waitFor();
				status.setValue(stat);
				if (pExitCodeHandler != null) {
					pExitCodeHandler.accept(stat);
				}
				return null;
			});
			final List<Future<Object>> futures = getExecutorService().invokeAll(callables);
			for (Future<Object> future : futures) {
				future.get();
			}
			return status.getValue();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected Callable<Object> startConsumer(@Nonnull InputStream pIn, Consumer<InputStream> pConsumer) {
		return () -> {
			if (pConsumer != null) {
				pConsumer.accept(pIn);
			} else {
				// No consumer: Discard the output.
				final byte[] buffer = new byte[1024];
				for (;;) {
					final int res;
					try {
						res = pIn.read(buffer);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
					if (res == -1) {
						break;
					}
				}
			}
			return null;
		};
	}

	private boolean isUsingDaemonThreads = true;
	private FailableConsumer<OutputStream,?> input = null;

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

	/**
	 * Sets the input handler for the external processes standard output.
	 * @param pInput The input handler for the external processes standard output.
	 */
	public void setInput(FailableConsumer<OutputStream,?> pInput) {
		input = pInput;
	}

	/**
	 * Returns the input handler for the external processes standard output.
	 * @return The input handler for the external processes standard output.
	 */
	public FailableConsumer<OutputStream,?> getInput() {
		return input;
	}
}
