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

public class DefaultMLog implements IMLog {
    private final AbstractLog log;
    private final String method;

    public DefaultMLog(AbstractLog pLog, String pMethod) {
        log = pLog;
        method = pMethod;
    }
    
    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    @Override
    public boolean isEnabledFor(Level pLevel) {
        return log.isEnabledFor(pLevel);
    }

    @Override
    public void trace(String pMessage) {
        if (log.isTraceEnabled()) {
            log.log(Level.TRACE, log.asMessage(method, AbstractLog.SEP_STD, pMessage));
        }
    }

    @Override
    public void debug(String pMessage) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_STD, pMessage));
        }
    }

    @Override
    public void info(String pMessage) {
        if (log.isInfoEnabled()) {
            log.log(Level.INFO, log.asMessage(method, AbstractLog.SEP_STD, pMessage));
        }
    }

    @Override
    public void warn(String pMessage) {
        if (log.isWarnEnabled()) {
            log.log(Level.WARN, log.asMessage(method, AbstractLog.SEP_STD, pMessage));
        }
    }

    @Override
    public void error(String pMessage) {
        if (log.isErrorEnabled()) {
            log.log(Level.ERROR, log.asMessage(method, AbstractLog.SEP_STD, pMessage));
        }
    }

    @Override
    public void fatal(String pMessage) {
        if (log.isFatalEnabled()) {
            log.log(Level.FATAL, log.asMessage(method, AbstractLog.SEP_STD, pMessage));
        }
    }

    @Override
    public void log(Level pLevel, String pMessage) {
        if (log.isEnabledFor(pLevel)) {
            log.log(pLevel, log.asMessage(method, AbstractLog.SEP_STD, pMessage));
        }
    }

    @Override
    public void info(Throwable pTh) {
        if (log.isInfoEnabled()) {
            log.log(Level.INFO, log.asMessage(method, AbstractLog.SEP_STD, null), pTh);
        }
    }

    @Override
    public void info(String pMessage, Throwable pTh) {
        if (log.isInfoEnabled()) {
            log.log(Level.INFO, log.asMessage(method, AbstractLog.SEP_STD, pMessage), pTh);
        }
    }

    @Override
    public void warn(Throwable pTh) {
        if (log.isWarnEnabled()) {
            log.log(Level.WARN, log.asMessage(method, AbstractLog.SEP_STD, null), pTh);
        }
    }

    @Override
    public void warn(String pMessage, Throwable pTh) {
        if (log.isWarnEnabled()) {
            log.log(Level.WARN, log.asMessage(method, AbstractLog.SEP_STD, pMessage), pTh);
        }
    }

    @Override
    public void error(Throwable pTh) {
        if (log.isErrorEnabled()) {
            log.log(Level.ERROR, log.asMessage(method, AbstractLog.SEP_STD, null), pTh);
        }
    }

    @Override
    public void error(String pMessage, Throwable pTh) {
        if (log.isErrorEnabled()) {
            log.log(Level.ERROR, log.asMessage(method, AbstractLog.SEP_STD, pMessage), pTh);
        }
    }

    @Override
    public void fatal(Throwable pTh) {
        if (log.isErrorEnabled()) {
            log.log(Level.FATAL, log.asMessage(method, AbstractLog.SEP_STD, null), pTh);
        }
    }

    @Override
    public void fatal(String pMessage, Throwable pTh) {
        if (log.isFatalEnabled()) {
            log.log(Level.FATAL, log.asMessage(method, AbstractLog.SEP_STD, pMessage), pTh);
        }
    }

    @Override
    public void trace(String pMessage, Object... pArgs) {
        if (log.isTraceEnabled()) {
            log.log(Level.TRACE, log.asMessage(method, AbstractLog.SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void debug(String pMessage, Object... pArgs) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void info(String pMessage, Object... pArgs) {
        if (log.isInfoEnabled()) {
            log.log(Level.INFO, log.asMessage(method, AbstractLog.SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void warn(String pMessage, Object... pArgs) {
        if (log.isWarnEnabled()) {
            log.log(Level.WARN, log.asMessage(method, AbstractLog.SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void error(String pMessage, Object... pArgs) {
        if (log.isErrorEnabled()) {
            log.log(Level.ERROR, log.asMessage(method, AbstractLog.SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void fatal(String pMessage, Object... pArgs) {
        if (log.isFatalEnabled()) {
            log.log(Level.FATAL, log.asMessage(method, AbstractLog.SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void log(Level pLevel, String pMessage, Object... pArgs) {
        if (log.isEnabledFor(pLevel)) {
            log.log(pLevel, log.asMessage(method, AbstractLog.SEP_STD, pMessage, pArgs));
        }
    }

    @Override
    public void tracef(String pFormat, Object... pArgs) {
        if (log.isTraceEnabled()) {
            log.log(Level.TRACE, log.asMessageF(method, AbstractLog.SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void debugf(String pFormat, Object... pArgs) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessageF(method, AbstractLog.SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void infof(String pFormat, Object... pArgs) {
        if (log.isInfoEnabled()) {
            log.log(Level.INFO, log.asMessageF(method, AbstractLog.SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void warnf(String pFormat, Object... pArgs) {
        if (log.isWarnEnabled()) {
            log.log(Level.WARN, log.asMessageF(method, AbstractLog.SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void errorf(String pFormat, Object... pArgs) {
        if (log.isErrorEnabled()) {
            log.log(Level.ERROR, log.asMessageF(method, AbstractLog.SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void fatalf(String pFormat, Object... pArgs) {
        if (log.isFatalEnabled()) {
            log.log(Level.FATAL, log.asMessageF(method, AbstractLog.SEP_STD, pFormat, pArgs));
        }
    }

    @Override
    public void logf(Level pLevel, String pFormat, Object... pArgs) {
        if (log.isEnabledFor(pLevel)) {
            log.log(pLevel, log.asMessageF(method, AbstractLog.SEP_ENT, pFormat, pArgs));
        }
    }

    @Override
    public void entering() {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_ENT, null));
        }
    }

    @Override
    public void entering(String pMessage) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_ENT, pMessage));
        }
    }

    @Override
    public void entering(String pMessage, Object... pArgs) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_ENT, pMessage, pArgs));
        }
    }

    @Override
    public void enteringf(String pFormat, Object... pArgs) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_ENT, pFormat, pArgs));
        }
    }

    @Override
    public void exiting() {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, null));
        }
    }

    @Override
    public void exiting(String pMessage) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage));
        }
    }

    @Override
    public void exiting(String pMessage, int pResult) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMessage, long pResult) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMessage, boolean pResult) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMessage, short pResult) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMessage, byte pResult) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMessage, double pResult) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMessage, float pResult) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMessage, char pResult) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, String.valueOf(pResult)));
        }
    }

    @Override
    public void exiting(String pMessage, Object pResult) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, pResult));
        }
    }

    @Override
    public void exiting(String pMessage, Object... pArgs) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessage(method, AbstractLog.SEP_EXT, pMessage, pArgs));
        }
    }

    @Override
    public void exitingf(String pFormat, Object... pArgs) {
        if (log.isDebugEnabled()) {
            log.log(Level.DEBUG, log.asMessageF(method, AbstractLog.SEP_EXT, pFormat, pArgs));
        }
    }

}
