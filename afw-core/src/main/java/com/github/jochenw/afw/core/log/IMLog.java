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

import com.github.jochenw.afw.core.log.ILog.Level;

/** A method logger is basically a normal logger, which annotates every
 * logging message with a particular method name.
 */
public interface IMLog {
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
     * Logs a message with level {@link ILog.Level#TRACE}, if
     * that level is enabled.
     * @param pMessage The message, which is being logged.
     */
    void trace(String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#DEBUG}, if
     * that level is enabled.
     * @param pMessage The message, which is being logged.
     */
    void debug(String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#INFO}, if
     * that level is enabled.
     * @param pMessage The message, which is being logged.
     */
    void info(String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#WARN}, if
     * that level is enabled.
     * @param pMessage The message, which is being logged.
     */
    void warn(String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#ERROR}, if
     * that level is enabled.
     * @param pMessage The message, which is being logged.
     */
    void error(String pMessage);
    /**
     * Logs a message with level {@link ILog.Level#FATAL}, if
     * that level is enabled.
     * @param pMessage The message, which is being logged.
     */
    void fatal(String pMessage);
    /**
     * Logs a message with the given level, if
     * that level is enabled.
     * @param pLevel The messages log level.
     * @param pMessage The message, which is being logged.
     */
  void log(Level pLevel, String pMessage);
    
  /** Logs an exception with level {@link ILog.Level#INFO},
   * if that level is enabled.
   * @param pTh The exception, which is being logged.
   */
  void info(Throwable pTh);
  /** Logs a message, and an exception with level
   * {@link ILog.Level#INFO}, if that level is enabled.
   * @param pMessage The message, which is being logged.
   * @param pTh The exception, which is being logged.
   */
  void info(String pMessage, Throwable pTh);
  /** Logs an exception with level {@link ILog.Level#WARN},
   * if that level is enabled.
   * @param pTh The exception, which is being logged.
   */
  void warn(Throwable pTh);
  /** Logs a message, and an exception with level
   * {@link ILog.Level#WARN}, if that level is enabled.
   * @param pMessage The message, which is being logged.
   * @param pTh The exception, which is being logged.
   */
  void warn(String pMessage, Throwable pTh);
  /** Logs an exception with level {@link ILog.Level#ERROR},
   * if that level is enabled.
   * @param pTh The exception, which is being logged.
   */
  void error(Throwable pTh);
  /** Logs a message, and an exception with level
   * {@link ILog.Level#ERROR}, if that level is enabled.
   * @param pMessage The message, which is being logged.
   * @param pTh The exception, which is being logged.
   */
  void error(String pMessage, Throwable pTh);
  /** Logs an exception with level {@link ILog.Level#FATAL},
   * if that level is enabled.
   * @param pTh The exception, which is being logged.
   */
  void fatal(Throwable pTh);
  /** Logs a message, and an exception with level
   * {@link ILog.Level#FATAL}, if that level is enabled.
   * @param pMessage The message, which is being logged.
   * @param pTh The exception, which is being logged.
   */
  void fatal(String pMessage, Throwable pTh);

  /** Logs a message, and arguments, with log level
   * {@link ILog.Level#TRACE}, if that level is enabled.
   * The log message, and the arguments are being
   * separated with the string ", ". For example,
   * <pre>
   *    log.trace("myMethod", "Some Message", "Arg1", "Arg2");
   *  </pre>
   *  would log the message "Some Message, Arg1, Arg2".
   * @param pMessage The message, which is being logged.
   * @param pArgs The message arguments, which are being
   *   appended to the message string, using the separator ", ".
   */
  void trace(String pMessage, Object... pArgs);
  /** Logs a message, and arguments, with log level
   * {@link ILog.Level#DEBUG}, if that level is enabled.
   * The log message, and the arguments are being
   * separated with the string ", ". For example,
   * <pre>
   *    log.debug("myMethod", "Some Message", "Arg1", "Arg2");
   *  </pre>
   *  would log the message "Some Message, Arg1, Arg2".
   * @param pMessage The message, which is being logged.
   * @param pArgs The message arguments, which are being
   *   appended to the message string, using the separator ", ".
   */
  void debug(String pMessage, Object... pArgs);
  /** Logs a message, and arguments, with log level
   * {@link ILog.Level#INFO}, if that level is enabled.
   * The log message, and the arguments are being
   * separated with the string ", ". For example,
   * <pre>
   *    log.info("myMethod", "Some Message", "Arg1", "Arg2");
   *  </pre>
   *  would log the message "Some Message, Arg1, Arg2".
   * @param pMessage The message, which is being logged.
   * @param pArgs The message arguments, which are being
   *   appended to the message string, using the separator ", ".
   */
  void info(String pMessage, Object... pArgs);
  /** Logs a message, and arguments, with log level
   * {@link ILog.Level#WARN}, if that level is enabled.
   * The log message, and the arguments are being
   * separated with the string ", ". For example,
   * <pre>
   *    log.warn("myMethod", "Some Message", "Arg1", "Arg2");
   *  </pre>
   *  would log the message "Some Message, Arg1, Arg2".
   * @param pMessage The message, which is being logged.
   * @param pArgs The message arguments, which are being
   *   appended to the message string, using the separator ", ".
   */
  void warn(String pMessage, Object... pArgs);
  /** Logs a message, and arguments, with log level
   * {@link ILog.Level#ERROR}, if that level is enabled.
   * The log message, and the arguments are being
   * separated with the string ", ". For example,
   * <pre>
   *    log.error("myMethod", "Some Message", "Arg1", "Arg2");
   *  </pre>
   *  would log the message "Some Message, Arg1, Arg2".
   * @param pMessage The message, which is being logged.
   * @param pArgs The message arguments, which are being
   *   appended to the message string, using the separator ", ".
   */
  void error(String pMessage, Object... pArgs);
  /** Logs a message, and arguments, with log level
   * {@link ILog.Level#FATAL}, if that level is enabled.
   * The log message, and the arguments are being
   * separated with the string ", ". For example,
   * <pre>
   *    log.fatal("myMethod", "Some Message", "Arg1", "Arg2");
   *  </pre>
   *  would log the message "Some Message, Arg1, Arg2".
   * @param pMessage The message, which is being logged.
   * @param pArgs The message arguments, which are being
   *   appended to the message string, using the separator ", ".
   */
  void fatal(String pMessage, Object... pArgs);
  /** Logs a message, and arguments, with the given
   * log level, if that level is enabled.
   * The log message, and the arguments are being
   * separated with the string ", ". For example,
   * <pre>
   *    log.log("myMethod", pLevel, "Some Message", "Arg1", "Arg2");
   *  </pre>
   *  would log the message "Some Message, Arg1, Arg2".
   * @param pLevel The log level.
   * @param pMessage The message, which is being logged.
   * @param pArgs The message arguments, which are being
   *   appended to the message string, using the separator ", ".
   */
  void log(Level pLevel, String pMessage, Object... pArgs);
    
  /** Logs a formatted message, applying the given arguments,
   * with log level {@link ILog.Level#TRACE}, if that level
   * is enabled. The method {@link String#format(String, Object...)}
   * is used to format the message, so this is basically
   * equivalent to
   * <pre>
   *   log.trace(String.format(pFormat, pArgs));
   * </pre>
   * @param pFormat The message format, which is being used
   * to create the log message.
   * @param pArgs The message arguments, which are being
   *   applied to the message format string.
   */
  void tracef(String pFormat, Object... pArgs);
  /** Logs a formatted message, applying the given arguments,
   * with log level {@link ILog.Level#DEBUG}, if that level
   * is enabled. The method {@link String#format(String, Object...)}
   * is used to format the message, so this is basically
   * equivalent to
   * <pre>
   *   log.debug(String.format(pFormat, pArgs));
   * </pre>
   * @param pFormat The message format, which is being used
   * to create the log message.
   * @param pArgs The message arguments, which are being
   *   applied to the message format string.
   */
  void debugf(String pFormat, Object... pArgs);
  /** Logs a formatted message, applying the given arguments,
   * with log level {@link ILog.Level#INFO}, if that level
   * is enabled. The method {@link String#format(String, Object...)}
   * is used to format the message, so this is basically
   * equivalent to
   * <pre>
   *   log.info(String.format(pFormat, pArgs));
   * </pre>
   * @param pFormat The message format, which is being used
   * to create the log message.
   * @param pArgs The message arguments, which are being
   *   applied to the message format string.
   */
  void infof(String pFormat, Object... pArgs);
  /** Logs a formatted message, applying the given arguments,
   * with log level {@link ILog.Level#WARN}, if that level
   * is enabled. The method {@link String#format(String, Object...)}
   * is used to format the message, so this is basically
   * equivalent to
   * <pre>
   *   log.warn(String.format(pFormat, pArgs));
   * </pre>
   * @param pFormat The message format, which is being used
   * to create the log message.
   * @param pArgs The message arguments, which are being
   *   applied to the message format string.
   */
  void warnf(String pFormat, Object... pArgs);
  /** Logs a formatted message, applying the given arguments,
   * with log level {@link ILog.Level#ERROR}, if that level
   * is enabled. The method {@link String#format(String, Object...)}
   * is used to format the message, so this is basically
   * equivalent to
   * <pre>
   *   log.error(String.format(pFormat, pArgs));
   * </pre>
   * @param pFormat The message format, which is being used
   * to create the log message.
   * @param pArgs The message arguments, which are being
   *   applied to the message format string.
   */
  void errorf(String pFormat, Object... pArgs);
  /** Logs a formatted message, applying the given arguments,
   * with log level {@link ILog.Level#FATAL}, if that level
   * is enabled. The method {@link String#format(String, Object...)}
   * is used to format the message, so this is basically
   * equivalent to
   * <pre>
   *   log.fatal(String.format(pFormat, pArgs));
   * </pre>
   * @param pFormat The message format, which is being used
   * to create the log message.
   * @param pArgs The message arguments, which are being
   *   applied to the message format string.
   */
  void fatalf(String pFormat, Object... pArgs);
  /** Logs a formatted message, applying the given arguments,
   * with the given log level, if that level
   * is enabled. The method {@link String#format(String, Object...)}
   * is used to format the message, so this is basically
   * equivalent to
   * <pre>
   *   log.log(pLevel, String.format(pFormat, pArgs));
   * </pre>
   * @param pLevel The log level.
   * @param pFormat The message format, which is being used
   * to create the log message.
   * @param pArgs The message arguments, which are being
   *   applied to the message format string.
   */
  void logf(Level pLevel, String pFormat, Object... pArgs);
    
  /**
   * Logs a message, that the configured method is being entered,
   * with log level {@link ILog.Level#DEBUG}.
   */
  void entering();
  /**
   * Logs a message, that the configured method is being entered,
   * appending the given extra message,
   * with log level {@link ILog.Level#DEBUG}.
   * @param pMessage The extra message.
   */
  void entering(String pMessage);
  /**
   * Logs a message, that the configured method is being entered,
   * appending the given extra message, and the given
   * arguments, with log level {@link ILog.Level#DEBUG}.
   * @param pMessage The extra message.
   * @param pArgs The extra message arguments.
   */
  void entering(String pMessage, Object... pArgs);
  /**
   * Logs a message, that the configured method is being entered,
   * appending the given extra message, and the given
   * arguments, with log level {@link ILog.Level#DEBUG}.
   * The extra message, and the message arguments are being
   * formatted using {@link String#format(String, Object...)}.
   * @param pFormat The extra messages format string.
   * @param pArgs The extra message arguments.
   */
  void enteringf(String pFormat, Object... pArgs);
  /**
   * Logs a message, that the configured method is being left,
   * with log level {@link ILog.Level#DEBUG}.
   */
  void exiting();
  /**
   * Logs a message, that the configured method is being left,
   * and an extra message,
   * with log level {@link ILog.Level#DEBUG}.
   * @param pMessage The extra message.
   */
  void exiting(String pMessage);
  /**
   * Logs a message, that the configured method is being left,
   * returning the given result value,
   * and an extra message, with log level {@link ILog.Level#DEBUG}.
   * @param pMessage The extra message.
   * @param pResult The method calls result.
   */
  void exiting(String pMessage, int pResult);
  /**
   * Logs a message, that the configured method is being left,
   * returning the given result value,
   * and an extra message, with log level {@link ILog.Level#DEBUG}.
   * @param pMessage The extra message.
   * @param pResult The method calls result.
   */
  void exiting(String pMessage, long pResult);
  /**
   * Logs a message, that the configured method is being left,
   * returning the given result value,
   * and an extra message, with log level {@link ILog.Level#DEBUG}.
   * @param pMessage The extra message.
   * @param pResult The method calls result.
   */
  void exiting(String pMessage, boolean pResult);
  /**
   * Logs a message, that the configured method is being left,
   * returning the given result value,
   * and an extra message, with log level {@link ILog.Level#DEBUG}.
   * @param pMessage The extra message.
   * @param pResult The method calls result.
   */
  void exiting(String pMessage, short pResult);
  /**
   * Logs a message, that the configured method is being left,
   * returning the given result value,
   * and an extra message, with log level {@link ILog.Level#DEBUG}.
   * @param pMessage The extra message.
   * @param pResult The method calls result.
   */
  void exiting(String pMessage, byte pResult);
  /**
   * Logs a message, that the configured method is being left,
   * returning the given result value,
   * and an extra message, with log level {@link ILog.Level#DEBUG}.
   * @param pMessage The extra message.
   * @param pResult The method calls result.
   */
    void exiting(String pMessage, double pResult);
    /**
     * Logs a message, that the configured method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMessage, float pResult);
    /**
     * Logs a message, that the configured method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMessage, char pResult);
    /**
     * Logs a message, that the configured method is being left,
     * returning the given result value,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * @param pMessage The extra message.
     * @param pResult The method calls result.
     */
    void exiting(String pMessage, Object pResult);
    /**
     * Logs a message, that the configured method is being left,
     * returning the given result value,
     * and an extra message,
     * with log level {@link ILog.Level#DEBUG}.
     * The extra message is created using
     * {@link String#join(CharSequence, CharSequence...)},
     * using the separator string ", ", the extra message
     * string, and the given arguments.
     * @param pMessage The extra messages format string.
     * @param pArgs The extra messages arguments.
     */
    void exiting(String pMessage, Object... pArgs);
    /**
     * Logs a message, that the configured method is being left,
     * and an extra message, with log level {@link ILog.Level#DEBUG}.
     * The extra message is created using {@link String#format(String, Object...)}.
     * @param pFormat The extra messages format string.
     * @param pArgs The extra messages arguments.
     */
    void exitingf(String pFormat, Object... pArgs);
}
