package com.github.jochenw.afw.lc.guice;

import java.lang.reflect.Field;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.lc.InjLog;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class GuiceInjLogListener implements TypeListener {
	final ILogFactory logFactory;

	public GuiceInjLogListener(ILogFactory pLogFactory) {
		logFactory = pLogFactory;
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
				}
			}
			clazz = clazz.getSuperclass();
		}
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
