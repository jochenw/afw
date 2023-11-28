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

import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;

/** Default implementation of {@link ILogFactory}. Doesn't do any logging until another implementation
 * becomes available, and logging can be deferred to the new implementation.
 * The purpose is the ability to assign loggers immediately, even at a point,
 * when the real logging facility is not yet available.
 */
public class DefaultLogFactory extends AbstractLogFactory implements ILogFactory, LogManager.Listener {
    private AbstractLogFactory lf = new SimpleLogFactory();
    private boolean initialized;
    private final List<DefaultLog> loggers = new ArrayList<DefaultLog>();

    @Override
    protected AbstractLog newLog(String pId) {
        if (initialized) {
            return (AbstractLog) lf.getLog(pId);
        } else {
            final DefaultLog df = new DefaultLog(this, pId);
            df.logFactoryChanged(lf);
            loggers.add(df);
            return df;
        }
    }

    @Override
    public synchronized void logFactoryChanged(ILogFactory pFactory) {
        lf = (AbstractLogFactory) pFactory;
        for (DefaultLog log : loggers) {
            log.logFactoryChanged(pFactory);
        }
        initialized = true;
    }

	@Override
	protected void init() {
		// Does nothing.
	}
}
