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
    
    void info(String pMessage, Throwable pTh);
    void warn(String pMessage, Throwable pTh);
    void error(String pMessage, Throwable pTh);
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
    
    void entering(String pMessage);
    void entering(String pMessage, Object... pArgs);
    void enteringf(String pFormat, Object... pArgs);
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
