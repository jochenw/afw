package com.github.jochenw.afw.core.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.github.jochenw.afw.core.util.Strings;

public abstract class AFWCoreContextInitializer<Data extends Object> implements ServletContextListener {
	private static final String KEY = AFWCoreContextInitializer.class.getName() + ".KEY";

	@Override
	public void contextInitialized(ServletContextEvent pSce) {
		final ServletContext sc = pSce.getServletContext();
		final String instanceName = getInstanceName(sc.getContextPath());
		final Data data = newData(instanceName);
		sc.setAttribute(KEY, data);
	}

	@Override
	public void contextDestroyed(ServletContextEvent pSce) {
		final ServletContext sc = pSce.getServletContext();
		final Data data = getData(sc);
		sc.setAttribute(KEY, data);
		shutdown(data);
	}

	protected String getInstanceName(String pContextName) {
		if (Strings.isEmpty(pContextName)) {
			return "ROOT";
		} else if (pContextName.startsWith("/")) {
			return pContextName.substring(1);
		} else {
			return pContextName;
		}
	}

	public static <O> O getData(ServletContext pContext) {
		@SuppressWarnings("unchecked")
		final O data = (O) pContext.getAttribute(KEY);
		if (data == null) {
			throw new IllegalStateException("Data not available in context");
		}
		return data;
	}
	
	protected void shutdown(Data pData) {}
	
    protected abstract Data newData(String pInstanceName);
}
