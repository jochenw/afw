package com.github.jochenw.afw.core.log;

import com.github.jochenw.afw.core.ResourceLocator;

public interface ILogFactory {
	void setResourceLocator(ResourceLocator pLocator);
	ResourceLocator getResourceLocator();
	void start();
	void shutdown();
    ILog getLog(Class<?> pClass);
    ILog getLog(String pId);
    IMLog getLog(Class<?> pClass, String pMethod);
    IMLog getLog(String pId, String pMethod);
}
