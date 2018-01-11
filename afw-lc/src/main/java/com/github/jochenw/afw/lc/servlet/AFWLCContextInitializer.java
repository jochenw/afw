package com.github.jochenw.afw.lc.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.github.jochenw.afw.core.servlet.AFWCoreContextInitializer;
import com.github.jochenw.afw.lc.ComponentFactory;


public abstract class AFWLCContextInitializer extends AFWCoreContextInitializer<ComponentFactory> {
	public static ComponentFactory getComponentFactory(ServletContext pContext) {
		return AFWCoreContextInitializer.getData(pContext);
	}
	public static ComponentFactory getComponentFactory(HttpServletRequest pRequest) {
		return AFWCoreContextInitializer.getData(pRequest.getSession().getServletContext());
	}
}
