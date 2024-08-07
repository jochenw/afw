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

import com.github.jochenw.afw.core.ResourceLocator;


/** Abstract base class for implementations of {@link ILogFactory}.
 */
public abstract class AbstractLogFactory implements ILogFactory {
	private boolean initialized;
	private ResourceLocator resourceLocator;

	/** Creates a new instance. Protected, to avoid accidental instantiation.
	 * This constructor might be removed, it is mainly present to avoid a Javadoc
	 * warning with JDK 21.
	 */
	protected AbstractLogFactory() {}

	@Override
	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	@Override
	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	@Override
	public void start() {
		if (!initialized) {
			initialized = true;
			init();
		}
	}
	/**
	 * Called to initialize the log factory.
	 */
	protected abstract void init();
	@Override
	public void shutdown() {
		// Does nothing.
	}
	
	@Override
    public ILog getLog(Class<?> pClass) {
        return newLog(pClass.getName());
    }

    @Override
    public ILog getLog(String pId) {
        return newLog(pId);
    }

    /** Creates a new logger with the given id.
     * @param pId The loggers id.
     * @return The created  logger.
     */
    protected abstract AbstractLog newLog(String pId);
    
    @Override
    public IMLog getLog(Class<?> pClass, String pMethod) {
        return new DefaultMLog((AbstractLog) getLog(pClass), pMethod);
    }

    @Override
    public IMLog getLog(String pId, String pMethod) {
        return new DefaultMLog((AbstractLog) getLog(pId), pMethod);
    }
}
