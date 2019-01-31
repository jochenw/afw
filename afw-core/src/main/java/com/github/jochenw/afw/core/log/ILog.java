/**
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

public interface ILog {
    public enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }
    boolean isTraceEnabled();
    boolean isDebugEnabled();
    boolean isInfoEnabled();
    boolean isWarnEnabled();
    boolean isErrorEnabled();
    boolean isFatalEnabled();
    boolean isEnabledFor(Level pLevel);
    public String getId();

    void trace(String pMethod, String pMessage);
    void debug(String pMethod, String pMessage);
    void info(String pMethod, String pMessage);
    void warn(String pMethod, String pMessage);
    void error(String pMethod, String pMessage);
    void fatal(String pMethod, String pMessage);
    void log(Level pLevel, String pMethod, String pMessage);

    void info(String pMethod, String pMessage, Throwable pTh);
    void warn(String pMethod, String pMessage, Throwable pTh);
    void error(String pMethod, String pMessage, Throwable pTh);
    void fatal(String pMethod, String pMessage, Throwable pTh);

    void trace(String pMethod, String pMessage, Object... pArgs);
    void debug(String pMethod, String pMessage, Object... pArgs);
    void info(String pMethod, String pMessage, Object... pArgs);
    void warn(String pMethod, String pMessage, Object... pArgs);
    void error(String pMethod, String pMessage, Object... pArgs);
    void fatal(String pMethod, String pMessage, Object... pArgs);
    void log(String pMethod, Level pLevel, String pMessage, Object... pArgs);

    void tracef(String pMethod, String pFormat, Object... pArgs);
    void debugf(String pMethod, String pFormat, Object... pArgs);
    void infof(String pMethod, String pFormat, Object... pArgs);
    void warnf(String pMethod, String pFormat, Object... pArgs);
    void errorf(String pMethod, String pFormat, Object... pArgs);
    void fatalf(String pMethod, String pFormat, Object... pArgs);
    void logf(Level pLevel, String pMethod, String pFormat, Object... pArgs);

    void entering(String pMethod, String pMessage);
    void entering(String pMethod, String pMessage, Object... pArgs);
    void enteringf(String pMethod, String pFormat, Object... pArgs);
    void exiting(String pMethod, String pMessage);
    void exiting(String pMethod, String pMessage, int pResult);
    void exiting(String pMethod, String pMessage, long pResult);
    void exiting(String pMethod, String pMessage, boolean pResult);
    void exiting(String pMethod, String pMessage, short pResult);
    void exiting(String pMethod, String pMessage, byte pResult);
    void exiting(String pMethod, String pMessage, double pResult);
    void exiting(String pMethod, String pMessage, float pResult);
    void exiting(String pMethod, String pMessage, char pResult);
    void exiting(String pMethod, String pMessage, Object pResult);
    void exiting(String pMethod, String pMessage, Object... pArgs);
    void exitingf(String pMethod, String pFormat, Object... pArgs);
}
