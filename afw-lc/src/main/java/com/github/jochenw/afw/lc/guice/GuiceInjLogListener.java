package com.github.jochenw.afw.lc.guice;

import java.lang.reflect.Field;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.props.IBooleanProperty;
import com.github.jochenw.afw.core.props.IIntProperty;
import com.github.jochenw.afw.core.props.ILongProperty;
import com.github.jochenw.afw.core.props.IProperty;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.lc.InjLog;
import com.github.jochenw.afw.lc.InjProperty;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class GuiceInjLogListener implements TypeListener {
	final ILogFactory logFactory;
	final IPropertyFactory propertyFactory;

	public GuiceInjLogListener(ILogFactory pLogFactory, IPropertyFactory pPropertyFactory) {
		logFactory = pLogFactory;
		propertyFactory = pPropertyFactory;
	}

	@Override
	public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
		Class<?> clazz = typeLiteral.getRawType();
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getType() == ILog.class) {
					final InjLog injLog = field.getAnnotation(InjLog.class);
					if (injLog != null) {
						String id = injLog.id();
						if (id == null  ||  id.length() == 0) {
							id = field.getDeclaringClass().getName();
						}
						typeEncounter.register(newMembersInjector(field, id));
					}
					final InjProperty injProperty = field.getAnnotation(InjProperty.class);
					if (injProperty != null) {
						String id = injProperty.id();
						if (id == null ||  id.length() == 0) {
							id = field.getDeclaringClass().getName() + "." + field.getName();
						}
						typeEncounter.register(newPropertyMembersInjector(field, id, injProperty.defaultValue()));
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	private <T> MembersInjector<T> newPropertyMembersInjector(final Field pField, String pId, String pDefaultValue) {
		pField.setAccessible(true);
		final IProperty<?> property;
		if (pField.getType() == IBooleanProperty.class) {
			if (pDefaultValue == null  ||  pDefaultValue.length() == 0) {
				throw new IllegalStateException("No default value specified for field "
						+ pField.getName() + " in class " + pField.getDeclaringClass().getName());
			}
			property = propertyFactory.getBooleanProperty(pId, Boolean.valueOf(pDefaultValue));
		} else if (pField.getType() == IIntProperty.class) {
			if (pDefaultValue == null  ||  pDefaultValue.length() == 0) {
				throw new IllegalStateException("No default value specified for field "
						+ pField.getName() + " in class " + pField.getDeclaringClass().getName());
			}
			final int defaultVal;
			try {
				defaultVal = Integer.valueOf(pDefaultValue);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("Invalid default value specified for field "
						+ pField.getName() + " in class " + pField.getDeclaringClass().getName()
						+ ". Expected integer, got " + pDefaultValue);
			}
			property = propertyFactory.getIntProperty(pId, defaultVal);
		} else if (pField.getType() == ILongProperty.class) {
			if (pDefaultValue == null  ||  pDefaultValue.length() == 0) {
				throw new IllegalStateException("No default value specified for field "
						+ pField.getName() + " in class " + pField.getDeclaringClass().getName());
			}
			final long defaultVal;
			try {
				defaultVal = Long.valueOf(pDefaultValue);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("Invalid default value specified for field "
						+ pField.getName() + " in class " + pField.getDeclaringClass().getName()
						+ ". Expected long, got " + pDefaultValue);
			}
			property = propertyFactory.getLongProperty(pId, defaultVal);
		} else {
			if (pDefaultValue == null  ||  pDefaultValue.length() == 0) {
				property = propertyFactory.getProperty(pId);
			} else {
				property = propertyFactory.getProperty(pId, pDefaultValue);
			}
		}
		return new MembersInjector<T>() {
		    public void injectMembers(T t) {
		      try {
		    	  if (!pField.isAccessible()) {
		    		  pField.setAccessible(true);
		    	  }
		    	  pField.set(t, property);
		      } catch (Throwable th) {
		    	  throw Exceptions.show(th);
		      }
		    }
		};
	}
	
	private <T> MembersInjector<T> newMembersInjector(final Field pField, String pId) {
		pField.setAccessible(true);
		final ILog log = logFactory.getLog(pId);
		return new MembersInjector<T>() {
		    public void injectMembers(T t) {
		      try {
		    	  if (!pField.isAccessible()) {
		    		  pField.setAccessible(true);
		    	  }
		    	  pField.set(t, log);
		      } catch (Throwable th) {
		    	  throw Exceptions.show(th);
		      }
		    }
		};
      }
}
