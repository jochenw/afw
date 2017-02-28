package com.github.jochenw.afw.jsgen.api;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface IAnnotatable {
	public interface IAnnotation {
		JSGQName getType();
		Map<String,Object> getValues();
	}

	IAnnotation getAnnotation(JSGQName pName);
	IAnnotation getAnnotation(Class<? extends Annotation> pName);
	boolean isAnnotatedWith(JSGQName pName);
	boolean isAnnotatedWith(Class<? extends Annotation> pName);
}
