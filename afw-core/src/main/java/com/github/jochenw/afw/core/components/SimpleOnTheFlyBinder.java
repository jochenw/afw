package com.github.jochenw.afw.core.components;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.components.SimpleComponentFactory.OnTheFlyBinder;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.props.IProperty;
import com.github.jochenw.afw.core.props.IPropertyFactory;

public class SimpleOnTheFlyBinder implements OnTheFlyBinder {
	@Override
	public Object bind(@Nonnull SimpleComponentFactory pFactory,
			           @Nonnull Class<Object> pType, @Nullable String pName, @Nonnull String pEntityId,
			           @Nonnull Class<Object> pDeclaringClass) {
		if (pType.equals(ILog.class)) {
			final String id;
			if (pName == null  ||  pName.length() == 0) {
				id = pDeclaringClass.getName();
			} else {
				id = pName;
			}
			final ILogFactory logFactory = pFactory.requireInstance(ILogFactory.class);
			return logFactory.getLog(id);
		} else if (pType.equals(String.class)) {
			final String name;
			if (pName == null  ||  pName.length() == 0) {
				name = pDeclaringClass.getName() + "." + pEntityId;
			} else {
				name = pName;
			}
			final IPropertyFactory propFactory = pFactory.requireInstance(IPropertyFactory.class);
			return propFactory.getPropertyValue(name);
		} else if (pType.equals(IProperty.class)) {
			final String name;
			if (pName == null  ||  pName.length() == 0) {
				name = pDeclaringClass.getName() + "." + pEntityId;
			} else {
				name = pName;
			}
			final IPropertyFactory propFactory = pFactory.requireInstance(IPropertyFactory.class);
			return propFactory.getProperty(name);
		} else {
			return null;
		}
	}

}
