/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.log;


/**
 * Interface of a logger.
 */
public interface ILog {
	/**
	 * Enumeration of permitted log levels.
	 */
    public enum Level {
    	/** The trace level.
    	 */
        TRACE,
    	/** The debug level.
    	 */
        DEBUG,
    	/** The info level.
    	 */
        INFO,
    	/** The warning level.
    	 */
        WARN,
    	/** The error level.
    	 */
        ERROR,
    	/** The fatal error level.
    	 */
        FATAL
    }
    /** Returns, whether the {@link ILog.Level#TRACE trace} level is enabled.
     * @return True, if the {@link ILog.Level#TRACE trace} level is enabled.
     */
    boolean isTraceEnabled();
    /** Returns, whether the {@link ILog.Level#DEBUG debug} level is enabled.
     * @return True, if the {@link ILog.Level#DEBUG debug} level is enabled.
     */
    boolean isDebugEnabled();
    /** Returns, whether the {@link ILog.Level#INFO info} level is enabled.
     * @return True, if the {@link ILog.Level#INFO info} level is enabled.
     */
    boolean isInfoEnabled();
    /** Returns, whether the {@link ILog.Level#WARN warn} level is enabled.
     * @return True, if the {@link ILog.Level#WARN warn} level is enabled.
     */
    boolean isWarnEnabled();
    /** Returns, whether the {@link ILog.Level#ERROR error} level is enabled.
     * @return True, if the {@link ILog.Level#ERROR error} level is enabled.
     */
    boolean isErrorEnabled();
    /** Returns, whether the {@link ILog.Level#FATAL fatal} level is enabled.
     * @return True, if the {@link ILog.Level#FATAL fatal} level is enabled.
     */
    boolean isFatalEnabled();
    /** Returns, whether the given level is enabled.
     * @param pLevel The level, which is being tested.
     * @return True, if the given level is enabled.
     */
    boolean isEnabledFor(Level pLevel);
    /**
     * Returns the logger id.
     * @return The logger id.
     */
    public String getId();

    /**
     * Logs a message with level {@link ILog.Level#TRACE}, if
     * that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     */
    void trace(String pMethod, String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#DEBUG}, if
     * that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     */
    void debug(String pMethod, String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#INFO}, if
     * that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     */
    void info(String pMethod, String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#WARN}, if
     * that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     */
    void warn(String pMethod, String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#ERROR}, if
     * that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     */
    void error(String pMethod, String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#FATAL}, if
     * that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     */
    void fatal(String pMethod, String pMessage);
    /**
     * Logs a message with the given level, if
     * that level is enabled.
     * @param pLevel The messages log level.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     */
    void log(Level pLevel, String pMethod, String pMessage);

    /** Logs an exception with level {@link ILog.Level#INFO},
     * if that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pTh The exception, which is being logged.
     */
    void info(String pMethod, Throwable pTh);
    /** Logs a message, and an exception with level
     * {@link ILog.Level#INFO}, if that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pTh The exception, which is being logged.
     */
    void info(String pMethod, String pMessage, Throwable pTh);
    /** Logs an exception with level {@link ILog.Level#WARN},
     * if that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pTh The exception, which is being logged.
     */
    void warn(String pMethod, Throwable pTh);
    /** Logs a message, and an exception with level
     * {@link ILog.Level#WARN}, if that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pTh The exception, which is being logged.
     */
    void warn(String pMethod, String pMessage, Throwable pTh);
    /** Logs an exception with level {@link ILog.Level#ERROR},
     * if that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pTh The exception, which is being logged.
     */
    void error(String pMethod, Throwable pTh);
    /** Logs a message, and an exception with level
     * {@link ILog.Level#ERROR}, if that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pTh The exception, which is being logged.
     */
    void error(String pMethod, String pMessage, Throwable pTh);
    /** Logs an exception with level {@link ILog.Level#FATAL},
     * if that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pTh The exception, which is being logged.
     */
    void fatal(String pMethod, Throwable pTh);
    /** Logs a message, and an exception with level
     * {@link ILog.Level#FATAL}, if that level is enabled.
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pTh The exception, which is being logged.
     */
    void fatal(String pMethod, String pMessage, Throwable pTh);


    /** Logs a message, and arguments, with log level
     * {@link ILog.Level#TRACE}, if that level is enabled.
     * The log message, and the arguments are being
     * separated with the string ", ". For example,
     * <pre>
     *    log.trace("myMethod", "Some Message", "Arg1", "Arg2");
     *  </pre>
     *  would log the message "Some Message, Arg1, Arg2".
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pArgs The message arguments, which are being
     *   appended to the message string, using the separator ", ".
     */
    void trace(String pMethod, String pMessage, Object... pArgs);
    /** Logs a message, and arguments, with log level
     * {@link ILog.Level#DEBUG}, if that level is enabled.
     * The log message, and the arguments are being
     * separated with the string ", ". For example,
     * <pre>
     *    log.debug("myMethod", "Some Message", "Arg1", "Arg2");
     *  </pre>
     *  would log the message "Some Message, Arg1, Arg2".
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pArgs The message arguments, which are being
     *   appended to the message string, using the separator ", ".
     */
    void debug(String pMethod, String pMessage, Object... pArgs);
    /** Logs a message, and arguments, with log level
     * {@link ILog.Level#INFO}, if that level is enabled.
     * The log message, and the arguments are being
     * separated with the string ", ". For example,
     * <pre>
     *    log.info("myMethod", "Some Message", "Arg1", "Arg2");
     *  </pre>
     *  would log the message "Some Message, Arg1, Arg2".
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pArgs The message arguments, which are being
     *   appended to the message string, using the separator ", ".
     */
    void info(String pMethod, String pMessage, Object... pArgs);
    /** Logs a message, and arguments, with log level
     * {@link ILog.Level#WARN}, if that level is enabled.
     * The log message, and the arguments are being
     * separated with the string ", ". For example,
     * <pre>
     *    log.warn("myMethod", "Some Message", "Arg1", "Arg2");
     *  </pre>
     *  would log the message "Some Message, Arg1, Arg2".
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pArgs The message arguments, which are being
     *   appended to the message string, using the separator ", ".
     */
    void warn(String pMethod, String pMessage, Object... pArgs);
    /** Logs a message, and arguments, with log level
     * {@link ILog.Level#ERROR}, if that level is enabled.
     * The log message, and the arguments are being
     * separated with the string ", ". For example,
     * <pre>
     *    log.error("myMethod", "Some Message", "Arg1", "Arg2");
     *  </pre>
     *  would log the message "Some Message, Arg1, Arg2".
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pArgs The message arguments, which are being
     *   appended to the message string, using the separator ", ".
     */
    void error(String pMethod, String pMessage, Object... pArgs);
    /** Logs a message, and arguments, with log level
     * {@link ILog.Level#FATAL}, if that level is enabled.
     * The log message, and the arguments are being
     * separated with the string ", ". For example,
     * <pre>
     *    log.fatal("myMethod", "Some Message", "Arg1", "Arg2");
     *  </pre>
     *  would log the message "Some Message, Arg1, Arg2".
     * @param pMethod The method name, which is being logged.
     * @param pMessage The message, which is being logged.
     * @param pArgs The message arguments, which are being
     *   appended to the message string, using the separator ", ".
     */
    void fatal(String pMethod, String pMessage, Object... pArgs);
    /** Logs a message, and arguments, with the given
     * log level, if that level is enabled.
     * The log message, and the arguments are being
     * separated with the string ", ". For example,
     * <pre>
     *    log.log("myMethod", pLevel, "Some Message", "Arg1", "Arg2");
     *  </pre>
     *  would log the message "Some Message, Arg1, Arg2".
     * @param pMethod The method name, which is being logged.
     * @param pLevel The log level.
     * @param pMessage The message, which is being logged.
     * @param pArgs The message arguments, which are being
     *   appended to the message string, using the separator ", ".
     */
    void log(String pMethod, Level pLevel, String pMessage, Object... pArgs);

    /** Logs a formatted message, applying the given arguments,
     * with log level {@link ILog.Level#TRACE}, if that level
     * is enabled. The method {@link String#format(String, Object...)}
     * is used to format the message, so this is basically
     * equivalent to
     * <pre>
     *   log.trace(pMethod, String.format(pFormat, pArgs));
     * </pre>
     * @param pMethod The method name, which is being logged.
     * @param pFormat The message format, which is being used
     * to create the log message.
     * @param pArgs The message arguments, which are being
     *   applied to the message format string.
     */
    void tracef(String pMethod, String pFormat, Object... pArgs);
    /** Logs a formatted message, applying the given arguments,
     * with log level {@link ILog.Level#DEBUG}, if that level
     * is enabled. The method {@link String#format(String, Object...)}
     * is used to format the message, so this is basically
     * equivalent to
     * <pre>
     *   log.debug(pMethod, String.format(pFormat, pArgs));
     * </pre>
     * @param pMethod The method name, which is being logged.
     * @param pFormat The message format, which is being used
     * to create the log message.
     * @param pArgs The message arguments, which are being
     *   applied to the message format string.
     */
    void debugf(String pMethod, String pFormat, Object... pArgs);
    /** Logs a formatted message, applying the given arguments,
     * with log level {@link ILog.Level#INFO}, if that level
     * is enabled. The method {@link String#format(String, Object...)}
     * is used to format the message, so this is basically
     * equivalent to
     * <pre>
     *   log.info(pMethod, String.format(pFormat, pArgs));
     * </pre>
     * @param pMethod The method name, which is being logged.
     * @param pFormat The message format, which is being used
     * to create the log message.
     * @param pArgs The message arguments, which are being
     *   applied to the message format string.
     */
    void infof(String pMethod, String pFormat, Object... pArgs);
    /** Logs a formatted message, applying the given arguments,
     * with log level {@link ILog.Level#WARN}, if that level
     * is enabled. The method {@link String#format(String, Object...)}
     * is used to format the message, so this is basically
     * equivalent to
     * <pre>
     *   log.warn(pMethod, String.format(pFormat, pArgs));
     * </pre>
     * @param pMethod The method name, which is being logged.
     * @param pFormat The message format, which is being used
     * to create the log message.
     * @param pArgs The message arguments, which are being
     *   applied to the message format string.
     */
   void warnf(String pMethod, String pFormat, Object... pArgs);
   /** Logs a formatted message, applying the given arguments,
    * with log level {@link ILog.Level#ERROR}, if that level
    * is enabled. The method {@link String#format(String, Object...)}
    * is used to format the message, so this is basically
    * equivalent to
    * <pre>
    *   log.error(pMethod, String.format(pFormat, pArgs));
    * </pre>
    * @param pMethod The method name, which is being logged.
    * @param pFormat The message format, which is being used
    * to create the log message.
    * @param pArgs The message arguments, which are being
    *   applied to the message format string.
    */
    void errorf(String pMethod, String pFormat, Object... pArgs);
    /** Logs a formatted message, applying the given arguments,
     * with log level {@link ILog.Level#FATAL}, if that level
     * is enabled. The method {@link String#format(String, Object...)}
     * is used to format the message, so this is basically
     * equivalent to
     * <pre>
     *   log.fatal(pMethod, String.format(pFormat, pArgs));
     * </pre>
     * @param pMethod The method name, which is being logged.
     * @param pFormat The message format, which is being used
     * to create the log message.
     * @param pArgs The message arguments, which are being
     *   applied to the message format string.
     */
    void fatalf(String pMethod, String pFormat, Object... pArgs);
    /** Logs a formatted message, applying the given arguments,
     * with the given log level, if that level
     * is enabled. The method {@link String#format(String, Object...)}
     * is used to format the message, so this is basically
     * equivalent to
     * <pre>
     *   log.log(pMethod, pLevel, String.format(pFormat, pArgs));
     * </pre>
     * @param pLevel The log level.
     * @param pMethod The method name, which is being logged.
     * @param pFormat The message format, which is being used
     * to create the log message.
     * @param pArgs The message arguments, which are being
     *   applied to the message format string.
     */
    void logf(Level pLevel, String pMethod, String pFormat, Object... pArgs);

    /**
     * Logs a message, that the given method is being entered,
     * with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being entered.
     */
    void entering(String pMethod);
    /**
     * Logs a message, that the given method is being entered,
     * appending the given extra message,
     * with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being entered.
     * @param pMessage The extra message.
     */
    void entering(String pMethod, String pMessage);
    /**
     * Logs a message, that the given method is being entered,
     * appending the given extra message, and the given
     * arguments, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being entered.
     * @param pMessage The extra message.
     * @param pArgs The extra message arguments.
     */
    void entering(String pMethod, String pMessage, Object... pArgs);
    /**
     * Logs a message, that the given method is being entered,
     * appending the given extra message, and the given
     * arguments, with log level {@link ILog.Level#DEBUG}.
     * The extra message, and the message arguments are being
     * formatted using {@link String#format(String, Object...)}.
     * @param pMethod Name of the method, that's being entered.
     * @param pFormat The extra messages format string.
     * @param pArgs The extra message arguments.
     */
    void enteringf(String pMethod, String pFormat, Object... pArgs);
    /**
     * Logs a message, that the given method is being left,
     * with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     */
    void exiting(String pMethod);
    /**
     * Logs a message, that the given method is being left,
     * and an extra message,
     * with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     */
    void exiting(String pMethod, String pMessage);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMethod, String pMessage, int pResult);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMethod, String pMessage, long pResult);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMethod, String pMessage, boolean pResult);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMethod, String pMessage, short pResult);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMethod, String pMessage, byte pResult);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMethod, String pMessage, double pResult);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMethod, String pMessage, float pResult);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMethod, String pMessage, char pResult);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMethod, String pMessage, Object pResult);
    /**
     * Logs a message, that the given method is being left,
     * returning the given result value,
     * and an extra message,
     * with log level {@link ILog.Level#DEBUG}.
     * The extra message is created using
     * {@link String#join(CharSequence, CharSequence...)},
     * using the separator string ", ", the extra message
     * string, and the given arguments.
     * @param pMethod Name of the method, that's being left.
     * @param pMessage The extra messages format string.
     * @param pArgs The extra messages arguments.
     */
    void exiting(String pMethod, String pMessage, Object... pArgs);
    /**
     * Logs a message, that the given method is being left,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * The extra message is created using {@link String#format(String, Object...)}.
     * @param pMethod Name of the method, that's being left.
     * @param pFormat The extra messages format string.
     * @param pArgs The extra messages arguments.
     */
    void exitingf(String pMethod, String pFormat, Object... pArgs);
}
