package com.github.jochenw.afw.jsgen.api;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public interface IJSGMethod extends IAnnotatable, IProtectable {
	public static class Param implements IAnnotatable {
		private final IAnnotatable annotatable = new DefaultAnnotatable();
		private final String name;
		private final JSGQName type;
		private final List<JSGQName> thrownExceptions = new ArrayList<>();

		public Param(String pName, JSGQName pType) {
			name = pName;
			type = pType;
		}
		
		@Override
		public IAnnotation getAnnotation(JSGQName pName) {
			return annotatable.getAnnotation(pName);
		}

		@Override
		public IAnnotation getAnnotation(Class<? extends Annotation> pName) {
			return annotatable.getAnnotation(pName);
		}

		@Override
		public boolean isAnnotatedWith(JSGQName pName) {
			return annotatable.isAnnotatedWith(pName);
		}

		@Override
		public boolean isAnnotatedWith(Class<? extends Annotation> pName) {
			return annotatable.isAnnotatedWith(pName);
		}

		String getName() {
			return name;
		}
	
		JSGQName getType() {
			return type;
		}

		Param throwing(JSGQName pException) {
			thrownExceptions.add(pException);
			return this;
		}

		Param throwing(JSGQName... pExceptions) {
			if (pExceptions != null) {
				for (JSGQName exc : pExceptions) {
					throwing(exc);
				}
			}
			return this;
		}
	}
	IJSGSource getSource();
	String getName();
	JSGQName getType();
	boolean isAbstract();
	boolean isFinal();
	boolean isStatic();
}
