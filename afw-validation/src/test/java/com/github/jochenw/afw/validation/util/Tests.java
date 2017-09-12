package com.github.jochenw.afw.validation.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Tests {
	public static Annotation requireAnnotation(Object pBean, String pProperty, Class<? extends Annotation> pAnnotationClass) {
		for (Field f : pBean.getClass().getDeclaredFields()) {
			if (f.getName().equals(pProperty)) {
				final Annotation annotation = f.getDeclaredAnnotation(pAnnotationClass);
				if (annotation != null) {
					return annotation;
				}
			}
		}
		for (Method m : pBean.getClass().getDeclaredMethods()) {
			if (m.getName().equals(pProperty)) {
				final Annotation annotation = m.getDeclaredAnnotation(pAnnotationClass);
				if (annotation != null) {
					return annotation;
				}
			}
		}
		throw new IllegalStateException("No annotation @" + pAnnotationClass.getSimpleName() + " found on property " + pProperty
				+ " in class " + pBean.getClass().getName());
	}
}
