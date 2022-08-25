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

import java.util.Formatter;

import com.github.jochenw.afw.core.util.Strings;


/**
 * Abstract base class for implementations of {@link ILog}.
 */
public abstract class AbstractLog implements ILog {
    /** The basic separator string.
     */
    protected static final String SEP_STD = ": ";
    /** The separator string for entering a method.
     */
    protected static final String SEP_ENT = ": -> ";
    /** The separator string for exiting from a method.
     */
    protected static final String SEP_EXT = ": <- ";
    /** The log factory, that created this object.
     */
    protected final AbstractLogFactory logFactory;
    /** The loggers id.
     */
    protected final String id;

    /**
     * Creates a new logger with the given factory, and id.
     * @param pFactory The log factory, that created this object.
     * @param pId The loggers id.
     */
    protected AbstractLog(AbstractLogFactory pFactory, String pId) {
        logFactory = pFactory;
        id = pId;
    }
    
    /** Creates a log message with the given method name, separator, and message string.
     * @param pMethod The method name, that is being logged.
     * @param pSep The separator string, that is being used to separate method name and
     *   actual log message.
     * @param pMessage The actual log message.
     * @return The created log message.
     */
    protected String asMessage(String pMethod, String pSep, String pMessage) {
        final StringBuilder sb = new StringBuilder();
        if (pMethod != null) {
            sb.append(pMethod);
            sb.append(pSep);
        }
        if (pMessage != null) {
        	sb.append(pMessage);
        }
        return sb.toString();
    }

    /** Creates a log message with the given method name, separator, message string,
     *   and message parameters.
     * @param pMethod The method name, that is being logged.
     * @param pSep The separator string, that is being used to separate method name and
     *   actual log message.
     * @param pMessage The actual log message, with optional parameter references.
     * @param pArgs The message parameters, if any.
     * @return The created log message.
     */
    protected String asMessage(String pMethod, String pSep, String pMessage, Object... pArgs) {
        final StringBuilder sb = new StringBuilder();
        if (pMethod != null) {
            sb.append(pMethod);
            sb.append(pSep);
        }
        sb.append(pMessage);
        if (pArgs != null  &&  pArgs.length > 0) {
            sb.append(", ");
            Strings.append(sb, pArgs);
        }
        return sb.toString();
    }

    /** Creates a log message with method name, separator
     *   and message parameters, but without message string.
     * @param pMethod The method name, that is being logged.
     * @param pSep The separator string, that is being used to separate method name and
     *   actual log message.
     * @param pArgs The message parameters, if any.
     * @return The created log message.
     * @see AbstractLog#asMessageF(String, String, String, Object...)
     */
    protected String asMessage(String pMethod, String pSep, Object... pArgs) {
        final StringBuilder sb = new StringBuilder();
        if (pMethod != null) {
            sb.append(pMethod);
            sb.append(pSep);
        }
        if (pArgs != null  &&  pArgs.length > 0) {
            Strings.append(sb, pArgs);
        }
        return sb.toString();
    }

    /** Creates a log message with method name, separator
     *   message string, and message parameters
     * @param pMethod The method name, that is being logged.
     * @param pSep The separator string, that is being used to separate method name and
     *   actual log message.
     * @param pFormat The message string, with optional parameter references.
     * @param pArgs The message parameters, if any.
     * @return The created log message.
     * @see AbstractLog#asMessageF(String, String, String, Object...)
     */
    protected String asMessageF(String pMethod, String pSep, String pFormat, Object... pArgs) {
        final StringBuilder sb = new StringBuilder();
        if (pMethod != null) {
            sb.append(pMethod);
            sb.append(pSep);
        }
        try (Formatter fmt = new Formatter(sb)) {
            fmt.format(pFormat, pArgs);
        }
        return sb.toString();
    }

    /**
    * Logs the given message, with the given log level, if that is enabled.
    * @param pLevel The log level; nothing will be written, if the level isn't enabled.
    * @param pMessage The log message, that is being written.
    */
    protected abstract void log(Level pLevel, String pMessage);
    /**
    * Logs the given exception, with the given log level, if that is enabled.
    * @param pLevel The log level; nothing will be written, if the level isn't enabled.
    * @param pMessage The log message, that is being written.
    * @param pTh The exception, that is being logged.
    */
    protected abstract void log(Level pLevel, String pMessage, Throwable pTh);
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void trace(String pMethod, String pMessage) {
        if (isTraceEnabled()) {
            log(Level.TRACE, asMessage(pMethod, SEP_STD, pMessage));
        }
    }

    @Override
    public void debug(String pMethod, String pMessage) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_STD, pMessage));
        }
    }

    @Override
    public void info(String pMethod, String pMessage) {
        if (isInfoEnabled()) {
            log(Level.INFO, asMessage(pMethod, SEP_STD, pMessage));
        }
    }

    @Override
    public void warn(String pMethod, String pMessage) {
        if (isWarnEnabled()) {
            log(Level.WARN, asMessage(pMethod, SEP_STD, pMessage));
        }
    }

    @Override
    public void error(String pMethod, String pMessage) {
        if (isErrorEnabled()) {
            log(Level.ERROR, asMessage(pMethod, SEP_STD, pMessage));
        }
    }

    @Override
    public void fatal(String pMethod, String pMessage) {
        if (isFatalEnabled()) {
            log(Level.FATAL, asMessage(pMethod, SEP_STD, pMessage));
        }
    }

    @Override
    public void log(Level pLevel, String pMethod, String pMessage) {
        if (isEnabledFor(pLevel)) {
            log(pLevel, asMessage(pMethod, SEP_STD, pMessage));
        }
    }

    @Override
    public void info(String pMethod, Throwable pTh) {
        if (isInfoEnabled()) {
            log(Level.INFO, asMessage(pMethod, SEP_STD, (Object[]) null), pTh);
        }
    }

    @Override
    public void info(String pMethod, String pMessage, Throwable pTh) {
        if (isInfoEnabled()) {
            log(Level.INFO, asMessage(pMethod, SEP_STD, pMessage), pTh);
        }
    }

    @Override
    public void warn(String pMethod, Throwable pTh) {
        if (isWarnEnabled()) {
            log(Level.WARN, asMessage(pMethod, SEP_STD, (String) null), pTh);
        }
    }

    @Override
    public void warn(String pMethod, String pMessage, Throwable pTh) {
        if (isWarnEnabled()) {
            log(Level.WARN, asMessage(pMethod, SEP_STD, pMessage), pTh);
        }
    }

    @Override
    public void error(String pMethod, Throwable pTh) {
        if (isErrorEnabled()) {
            log(Level.ERROR, asMessage(pMethod, SEP_STD, (String) null), pTh);
        }
    }

    @Override
    public void error(String pMethod, String pMessage, Throwable pTh) {
        if (isErrorEnabled()) {
            log(Level.ERROR, asMessage(pMethod, SEP_STD, pMessage), pTh);
        }
    }

    @Override
    public void fatal(String pMethod, Throwable pTh) {
        if (isFatalEnabled()) {
            log(Level.FATAL, asMessage(pMethod, SEP_STD, (String) null), pTh);
        }
    }

    @Override
    public void fatal(String pMethod, String pMessage, Throwable pTh) {
        if (isFatalEnabled()) {
            log(Level.FATAL, asMessage(pMethod, SEP_STD, pMessage), pTh);
        }
    }

    @Override
    public void trace(String pMethod, String pMessage, Object... pArgs) {
        if (isTraceEnabled()) {
            log(Level.TRACE, asMessage(pMethod, SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void debug(String pMethod, String pMessage, Object... pArgs) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void info(String pMethod, String pMessage, Object... pArgs) {
        if (isInfoEnabled()) {
            log(Level.INFO, asMessage(pMethod, SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void warn(String pMethod, String pMessage, Object... pArgs) {
        if (isWarnEnabled()) {
            log(Level.WARN, asMessage(pMethod, SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void error(String pMethod, String pMessage, Object... pArgs) {
        if (isErrorEnabled()) {
            log(Level.ERROR, asMessage(pMethod, SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void fatal(String pMethod, String pMessage, Object... pArgs) {
        if (isFatalEnabled()) {
            log(Level.FATAL, asMessage(pMethod, SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void log(String pMethod, Level pLevel, String pMessage, Object... pArgs) {
        if (isEnabledFor(pLevel)) {
            log(pLevel, asMessage(pMethod, SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void tracef(String pMethod, String pFormat, Object... pArgs) {
        if (isTraceEnabled()) {
            log(Level.TRACE, asMessageF(pMethod, SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void debugf(String pMethod, String pFormat, Object... pArgs) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessageF(pMethod, SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void infof(String pMethod, String pFormat, Object... pArgs) {
        if (isInfoEnabled()) {
            log(Level.INFO, asMessageF(pMethod, SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void warnf(String pMethod, String pFormat, Object... pArgs) {
        if (isWarnEnabled()) {
            log(Level.WARN, asMessageF(pMethod, SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void errorf(String pMethod, String pFormat, Object... pArgs) {
        if (isErrorEnabled()) {
            log(Level.ERROR, asMessageF(pMethod, SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void fatalf(String pMethod, String pFormat, Object... pArgs) {
        if (isFatalEnabled()) {
            log(Level.FATAL, asMessageF(pMethod, SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void logf(Level pLevel, String pMethod, String pFormat, Object... pArgs) {
        if (isEnabledFor(pLevel)) {
            log(pLevel, asMessageF(pMethod, SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void entering(String pMethod) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_ENT, (String) null));
        }
    }

    @Override
    public void entering(String pMethod, String pMessage) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_ENT, pMessage));
        }
    }

    @Override
    public void entering(String pMethod, Object... pArgs) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_ENT, pArgs));
        }
    }

    @Override
    public void entering(String pMethod, String pMessage, Object... pArgs) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_ENT, pMessage, pArgs));
        }
    }

    @Override
    public void enteringf(String pMethod, String pFormat, Object... pArgs) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessageF(pMethod, SEP_ENT, pFormat, pArgs));
        }
    }

    @Override
    public void exiting(String pMethod) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, (String) null));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, int pResult) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, long pResult) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, boolean pResult) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, short pResult) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, byte pResult) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, double pResult) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, float pResult) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, char pResult) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, Object pResult) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, pResult));
        }
    }

    @Override
    public void exiting(String pMethod, Object... pArgs) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pArgs));
        }
    }

    @Override
    public void exiting(String pMethod, String pMessage, Object... pArgs) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessage(pMethod, SEP_EXT, pMessage, pArgs));
        }
    }

    @Override
    public void exitingf(String pMethod, String pFormat, Object... pArgs) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, asMessageF(pMethod, SEP_EXT, pFormat, pArgs));
        }
    }
}
