/**
 * 
 */
package com.github.jochenw.afw.core.log.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.core.util.Strings;

/** Interface of an application logger. In contrast to the {@link ILog standard logger},
 * the application logger is supposed to exist only once per application. Additionally,
 * it's output is supposed to become visible to the application user immediately.
 */
public interface IAppLog {
	/** The application loggers log level. Default is {@link #INFO}.
	 */
    public enum Level {
    	/** Logging level, which is being used for trace messages. These are usually hidden.
    	 */
    	TRACE,
    	/** Logging level, which is being used for debugging messages. These are usually hidden.
    	 */
    	DEBUG,
    	/** Logging level, which is being used for informations.
    	 */
    	INFO,
    	/** Logging level, which is being used for warnings.
    	 */
    	WARN,
    	/** Logging level, which is being used for errors.
    	 */
    	ERROR,
    	/** Logging level, which is being used for fatal errors.
    	 */
    	FATAL
    }

    /** Returns the application loggers logging level. Default is {@link Level#INFO}.
     * @return The application loggers current logging level. Default is {@link Level#INFO}.
     * @see #setLevel(Level)
     * @see #isEnabled(Level)
     */
    public @NonNull Level getLevel();

    /** Sets the application loggers log level. Default is {@link Level#INFO}.
     * @param pLevel The application loggers new logging level.
     * @see #getLevel()
     * @see #isEnabled(Level)
     */
    public void setLevel(@NonNull Level pLevel);

    /**
     * Returns, whether logging is currently enabled for the given level.
     * @param pLevel The logging level to query for being enabled.
     * @return True, if the given level is enabled. Otherwise false.
     * @see #setLevel(Level)
     * @see #getLevel()
     */
    public boolean isEnabled(@NonNull Level pLevel);

    /** Logs the given message.
     * @param pLevel The messages logging level.
     * @param pMsg The message being logged.
     */
    public void log(@NonNull Level pLevel, String pMsg);

    /** Logs the given message with the given arguments.
     * @param pLevel The messages logging level.
     * @param pMsg The message being logged.
     * @param pArgs The message arguments.
     */
    public default void log(@NonNull Level pLevel, @NonNull String pMsg, @Nullable Object... pArgs) {
    	if (isEnabled(pLevel)) {
    		final String msg = Strings.formatCb(pMsg, pArgs);
    		log(pLevel, msg);
    	}
    }

    /** Logs the given message, and the given exception with the given log level.
     * @param pLevel The messages logging level.
     * @param pMsg The message being logged.
     * @param pTh The exception being logged.
     */
    public default void log(@NonNull Level pLevel, @NonNull String pMsg, @NonNull Throwable pTh) {
    	if (isEnabled(pLevel)) {
    		final FailableConsumer<OutputStream,IOException> consumer = (out) -> {
    			final PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, Charset.defaultCharset()));
    			pTh.printStackTrace(pw);
    			pw.flush();
    		};
    		log(pLevel, pMsg, consumer);
    	}
    }

    /** Logs the given exception with the given log level.
     * @param pLevel The messages logging level.
     * @param pTh The exception being logged.
     */
    public default void log(@NonNull Level pLevel, @NonNull Throwable pTh) {
    	final String msg = pTh.getClass().getName() + ": " + pTh.getMessage();
    	log(pLevel, msg, pTh);
    }

    /** Logs the given message.
     * @param pLevel The messages logging level.
     * @param pMsgSupplier A supplier for a message, which is being logged.
     *   This supplier is only invoked, if the message is actually logged.
     */
    public default void log(@NonNull Level pLevel, Supplier<@NonNull String> pMsgSupplier) {
    	if (isEnabled(pLevel)) {
    		log(pLevel, pMsgSupplier.get());
    	}
    }

    /** Logs the given {@link InputStream}.
     * @param pLevel The messages logging level.
     * @param pMsg A description of the stream, which is being logged.
     * @param pStreamSupplier The {@link InputStream input stream} being logged.
     *   This supplier is only invoked, if the message is actually logged.
     *   The {@link InputStream} is <em>not</em> being closed.
     */
    public default void log(@NonNull Level pLevel, String pMsg, FailableSupplier<InputStream,IOException> pStreamSupplier) {
    	if (isEnabled(pLevel)) {
    		final FailableConsumer<OutputStream,IOException> consumer = (out) -> {
    			Streams.copy(pStreamSupplier.get(), out);
    		};
    		log(pLevel, pMsg, consumer);
    	}
    }


    /** Logs a byte stream by invoking the given {@link FailableConsumer}.
     * @param pLevel The messages logging level.
     * @param pMsg A description of the stream, which is being logged.
     * @param pStreamConsumer A consumer, which is being invoked, if logging
     *   this message is enabled. The consumer will only be invoked, if that is the
     *   case.
     */
    public void log(@NonNull Level pLevel, String pMsg, FailableConsumer<OutputStream,IOException> pStreamConsumer);

    /**
     * Logs a message with level {@link Level#TRACE}.
     * @param pMsgSupplier The supplier of the log message.
     *   Will only be invoked, if the message is actually logged (if {@code isEnabled(Level.TRACE)}
     *   returns true.
     */
    public default void trace(Supplier<@NonNull String> pMsgSupplier) {
    	log(Level.TRACE, pMsgSupplier);
    }

    /**
     * Logs a message with level {@link Level#DEBUG}.
     * @param pMsgSupplier The supplier of the log message.
     *   Will only be invoked, if the message is actually logged (if {@code isEnabled(Level.DEBUG)}
     *   returns true.
     */
    public default void debug(Supplier<@NonNull String> pMsgSupplier) {
    	log(Level.DEBUG, pMsgSupplier);
    }

    /**
     * Logs a message with level {@link Level#INFO}.
     * @param pMsgSupplier The supplier of the log message.
     *   Will only be invoked, if the message is actually logged (if {@code isEnabled(Level.INFO)}
     *   returns true.
     */
    public default void info(Supplier<@NonNull String> pMsgSupplier) {
    	log(Level.INFO, pMsgSupplier);
    }

    /**
     * Logs a message with level {@link Level#WARN}.
     * @param pMsgSupplier The supplier of the log message.
     *   Will only be invoked, if the message is actually logged (if {@code isEnabled(Level.WARN)}
     *   returns true.
     */
    public default void warn(Supplier<@NonNull String> pMsgSupplier) {
    	log(Level.WARN, pMsgSupplier);
    }

    /**
     * Logs a message with level {@link Level#ERROR}.
     * @param pMsgSupplier The supplier of the log message.
     *   Will only be invoked, if the message is actually logged (if {@code isEnabled(Level.ERROR)}
     *   returns true.
     */
    public default void error(Supplier<@NonNull String> pMsgSupplier) {
    	log(Level.ERROR, pMsgSupplier);
    }

    /**
     * Logs a message with level {@link Level#FATAL}.
     * @param pMsgSupplier The supplier of the log message.
     *   Will only be invoked, if the message is actually logged (if {@code isEnabled(Level.FATAL)}
     *   returns true.
     */
    public default void fatal(Supplier<@NonNull String> pMsgSupplier) {
    	log(Level.FATAL, pMsgSupplier);
    }

    /**
     * Logs a message with level {@link Level#TRACE}.
     * @param pMsg The message to log.
     */
    public default void trace(String pMsg) {
    	log(Level.TRACE, pMsg);
    }

    /**
     * Logs a message with level {@link Level#DEBUG}.
     * @param pMsg The message to log.
     */
    public default void debug(String pMsg) {
    	log(Level.DEBUG, pMsg);
    }

    /**
     * Logs a message with level {@link Level#INFO}.
     * @param pMsg The message to log.
     */
    public default void info(String pMsg) {
    	log(Level.INFO, pMsg);
    }

    /**
     * Logs a message with level {@link Level#WARN}.
     * @param pMsg The message to log.
     */
    public default void warn(String pMsg) {
    	log(Level.WARN, pMsg);
    }

    /**
     * Logs a message with level {@link Level#ERROR}.
     * @param pMsg The message to log.
     */
    public default void error(String pMsg) {
    	log(Level.ERROR, pMsg);
    }

    /**
     * Logs a message with level {@link Level#FATAL}.
     * @param pMsg The message to log.
     */
    public default void fatal(String pMsg) {
    	log(Level.FATAL, pMsg);
    }


    /**
     * Logs a message with level {@link Level#TRACE}.
     * @param pMsg The message to log.
     * @param pArgs The message arguments.
     */
    public default void trace(@NonNull String pMsg, @Nullable Object... pArgs) {
    	log(Level.TRACE, pMsg, pArgs);
    }

    /**
     * Logs a message with level {@link Level#DEBUG}.
     * @param pMsg The message to log.
     * @param pArgs The message arguments.
     */
    public default void debug(@NonNull String pMsg, @Nullable Object... pArgs) {
    	log(Level.DEBUG, pMsg, pArgs);
    }

    /**
     * Logs a message with level {@link Level#INFO}.
     * @param pMsg The message to log.
     * @param pArgs The message arguments.
     */
    public default void info(@NonNull String pMsg, @Nullable Object... pArgs) {
    	log(Level.INFO, pMsg, pArgs);
    }

    /**
     * Logs a message with level {@link Level#WARN}.
     * @param pMsg The message to log.
     * @param pArgs The message arguments.
     */
    public default void warn(@NonNull String pMsg, @Nullable Object... pArgs) {
    	log(Level.WARN, pMsg, pArgs);
    }

    /**
     * Logs a message with level {@link Level#ERROR}.
     * @param pMsg The message to log.
     * @param pArgs The message arguments.
     */
    public default void error(@NonNull String pMsg, @Nullable Object... pArgs) {
    	log(Level.ERROR, pMsg, pArgs);
    }

    /**
     * Logs a message with level {@link Level#FATAL}.
     * @param pMsg The message to log.
     * @param pArgs The message arguments.
     */
    public default void fatal(@NonNull String pMsg, @Nullable Object... pArgs) {
    	log(Level.FATAL, pMsg, pArgs);
    }

    /** Logs the given exception with level {@link Level#WARN}.
     * @param pTh The exception to log.
     */
    public default void warn(@NonNull Throwable pTh) {
    	log(Level.WARN, pTh);
    }

    /** Logs the given exception with level {@link Level#ERROR}.
     * @param pTh The exception to log.
     */
    public default void error(@NonNull Throwable pTh) {
    	log(Level.ERROR, pTh);
    }

    /** Logs the given exception with level {@link Level#FATAL}.
     * @param pTh The exception to log.
     */
    public default void fatal(@NonNull Throwable pTh) {
    	log(Level.FATAL, pTh);
    }

    /** Logs the given exception, and the given message with level {@link Level#WARN}.
     * @param pMsg The message to log
     * @param pTh The exception to log.
     */
    public default void warn(@NonNull String pMsg, @NonNull Throwable pTh) {
    	log(Level.WARN, pMsg, pTh);
    }

    /** Logs the given exception with level {@link Level#ERROR}.
     * @param pMsg The message to log
     * @param pTh The exception to log.
     */
    public default void error(@NonNull String pMsg, @NonNull Throwable pTh) {
    	log(Level.ERROR, pMsg, pTh);
    }

    /** Logs the given exception with level {@link Level#FATAL}.
     * @param pMsg The message to log
     * @param pTh The exception to log.
     */
    public default void fatal(@NonNull String pMsg, @NonNull Throwable pTh) {
    	log(Level.FATAL, pMsg, pTh);
    }

    /** Creates a new IAppLog with the given log level, and output file.
     * @param pLevel The log level. May be null, in which case the default value {@link Level#INFO} is being used.
     * @param pFile The log file. May be null, in which case the default value {@link System#out} is being used.
     * @return The created {@link IAppLog}.
     */
    public static IAppLog of(Level pLevel, Path pFile) {
    	final Level lvl;
    	if (pLevel == null) {
    		lvl = Level.INFO;
    	} else {
    		lvl = pLevel;
    	}
    	if (pFile == null  ||  "-".equals(pFile.toString())) {
    		final SystemOutAppLog soal = new SystemOutAppLog();
    		soal.setLevel(lvl);
    		return soal;
    	} else {
    		try {
    			@SuppressWarnings("null")
				final @NonNull OutputStream os = Files.newOutputStream(pFile);
				final DefaultAppLog dal = new DefaultAppLog(os);
    			dal.setLevel(lvl);
    			return dal;
    		} catch (IOException e) {
    			throw new UncheckedIOException(e);
    		}
    	}
    }

    /** Creates a new IAppLog with the given log level, and output file.
     * @param pLevel The log level. May be null, in which case the default value {@link Level#INFO} is being used.
     * @param pFile The log file. May be null, in which case the default value {@link System#out} is being used.
    * @return The created {@link IAppLog}.
     */
    public static IAppLog of(Level pLevel, String pFile) {
    	if (pFile == null  ||  "-".equals(pFile)) {
    		return of (pLevel, (Path) null);
    	} else {
    		return of(pLevel, Paths.get(pFile));
    	}
    }
}
