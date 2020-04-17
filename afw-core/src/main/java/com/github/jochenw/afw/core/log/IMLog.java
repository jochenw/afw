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

public interface IMLog {
    boolean isTraceEnabled();
    boolean isDebugEnabled();
    boolean isInfoEnabled();
    boolean isWarnEnabled();
    boolean isErrorEnabled();
    boolean isFatalEnabled();
    boolean isEnabledFor(Level pLevel);
    
    void trace(String pMessage);
    void debug(String pMessage);
    void info(String pMessage);
    void warn(String pMessage);
    void error(String pMessage);
    void fatal(String pMessage);
    void log(Level pLevel, String pMessage);
    
    void info(Throwable pTh);
    void info(String pMessage, Throwable pTh);
    void warn(Throwable pTh);
    void warn(String pMessage, Throwable pTh);
    void error(Throwable pTh);
    void error(String pMessage, Throwable pTh);
    void fatal(Throwable pTh);
    void fatal(String pMessage, Throwable pTh);
    
    void trace(String pMessage, Object... pArgs);
    void debug(String pMessage, Object... pArgs);
    void info(String pMessage, Object... pArgs);
    void warn(String pMessage, Object... pArgs);
    void error(String pMessage, Object... pArgs);
    void fatal(String pMessage, Object... pArgs);
    void log(Level pLevel, String pMessage, Object... pArgs);
    
    void tracef(String pFormat, Object... pArgs);
    void debugf(String pFormat, Object... pArgs);
    void infof(String pFormat, Object... pArgs);
    void warnf(String pFormat, Object... pArgs);
    void errorf(String pFormat, Object... pArgs);
    void fatalf(String pFormat, Object... pArgs);
    void logf(Level pLevel, String pFormat, Object... pArgs);
    
    void entering();
    void entering(String pMessage);
    void entering(String pMessage, Object... pArgs);
    void enteringf(String pFormat, Object... pArgs);
    void exiting();
    void exiting(String pMessage);
    void exiting(String pMessage, int pResult);
    void exiting(String pMessage, long pResult);
    void exiting(String pMessage, boolean pResult);
    void exiting(String pMessage, short pResult);
    void exiting(String pMessage, byte pResult);
    void exiting(String pMessage, double pResult);
    void exiting(String pMessage, float pResult);
    void exiting(String pMessage, char pResult);
    void exiting(String pMessage, Object pResult);
    void exiting(String pMessage, Object... pArgs);
    void exitingf(String pFormat, Object... pArgs);
}
