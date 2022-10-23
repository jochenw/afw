package com.github.jochenw.afw.core.inject;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.props.StringProperty;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.impl.DefaultOnTheFlyBinder;

public class AfwCoreOnTheFlyBinder extends DefaultOnTheFlyBinder {

	@Override
	protected Object getLogger(IComponentFactory pFactory, Class<?> pType, String pId) {
		if (pType == ILog.class) {
			return pFactory.requireInstance(ILogFactory.class).getLog(pId);
		} else {
			return null;
		}
	}

	@Override
	protected Object getProperty(IComponentFactory pFactory, Class<?> pType, String pId) {
		if (pType == String.class) {
			return pFactory.requireInstance(IPropertyFactory.class).getPropertyValue(pId);
		} else if (pType == StringProperty.class) {
			return pFactory.requireInstance(IPropertyFactory.class).getProperty(pId);
		} else {
			return null;
		}
	}
	
}
