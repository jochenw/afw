package com.github.jochenw.afw.core.log;

import com.github.jochenw.afw.core.ResourceLocator;

public abstract class AbstractLogFactory implements ILogFactory {
	private boolean initialized;
	private ResourceLocator resourceLocator;
	
	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	public void start() {
		if (!initialized) {
			initialized = true;
			init();
		}
	}
	protected abstract void init();
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
