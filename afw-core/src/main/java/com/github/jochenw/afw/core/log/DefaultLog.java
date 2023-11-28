/*
0 * Copyright 2018 Jochen Wiedmann
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
 * Default implementation of {@link ILog}.  Doesn't do any logging until a
 * real implementation of {@link ILogFactory} becomes available, and
 * logging can be deferred to the new implementation.
 */
public class DefaultLog extends AbstractLog implements LogManager.Listener {
    private AbstractLog log;

    /**
     * Creates a new instance with the given factory, and the given id.
     * @param pFactory The factory, that creates this logger.
     * @param pId The logger id.
     */
    public DefaultLog(DefaultLogFactory pFactory, String pId) {
        super(pFactory, pId);
        
    }

    @Override
    public synchronized void logFactoryChanged(ILogFactory pFactory) {
        log = (AbstractLog) pFactory.getLog(getId());
    }
    
    DefaultLogFactory getFactory() {
        return (DefaultLogFactory) logFactory;
    }
    
    @Override
    public synchronized boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public synchronized boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public synchronized boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public synchronized boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public synchronized boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public synchronized boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    @Override
    public synchronized boolean isEnabledFor(Level pLevel) {
        return log.isEnabledFor(pLevel);
    }

    @Override
    protected synchronized void log(Level pLevel, String pMessage) {
        log.log(pLevel, pMessage);
    }

    @Override
    protected synchronized void log(Level pLevel, String pMessage, Throwable pTh) {
        log.log(pLevel, pMessage, pTh);
    }
}
