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
package com.github.jochenw.afw.core.log.simple;

import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.ILog;


/** Very simple implementation of {@link ILog}, which is not based on any
 * external logging framework.
 */
public class SimpleLog extends AbstractLog {
    SimpleLog(SimpleLogFactory pFactory, String pId) {
        super(pFactory, pId);
    }

    /** Returns the factory, that created this logger.
     * @return The factory, that created this logger
     */
    protected SimpleLogFactory getFactory() {
        return (SimpleLogFactory) super.logFactory;
    }

    @Override
    public boolean isTraceEnabled() {
        return getFactory().getLevel().ordinal() <= Level.TRACE.ordinal();
    }

    @Override
    public boolean isDebugEnabled() {
        return getFactory().getLevel().ordinal() <= Level.DEBUG.ordinal();
    }

    @Override
    public boolean isInfoEnabled() {
        return getFactory().getLevel().ordinal() <= Level.INFO.ordinal();
    }

    @Override
    public boolean isWarnEnabled() {
        return getFactory().getLevel().ordinal() <= Level.WARN.ordinal();
    }

    @Override
    public boolean isErrorEnabled() {
        return getFactory().getLevel().ordinal() <= Level.ERROR.ordinal();
    }

    @Override
    public boolean isFatalEnabled() {
        return getFactory().getLevel().ordinal() <= Level.FATAL.ordinal();
    }

    @Override
    public boolean isEnabledFor(Level pLevel) {
        return getFactory().getLevel().ordinal() <= pLevel.ordinal();
    }

    @Override
    protected void log(Level pLevel, String pMessage) {
        getFactory().write(getId(), pLevel, pMessage);
    }

    @Override
    protected void log(Level pLevel, String pMessage, Throwable pTh) {
        getFactory().write(getId(), pLevel, pMessage, pTh);
    }

}
