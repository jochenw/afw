package com.github.jochenw.afw.validation.impl.annotations;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;

import org.junit.Test;

import com.github.jochenw.afw.validation.api.annotations.AnnotationData;
import com.github.jochenw.afw.validation.api.annotations.IValidationFactoryPlugin;
import com.github.jochenw.afw.validation.api.annotations.NotNull;
import com.github.jochenw.afw.validation.util.Tests;

public class NotNullValidationFactoryPluginTest {
	@NotNull(code="NN01")
	private String f;

	@NotNull(code="NN02")
	private String getF() {
		return f;
	}
	
	@Test
	public void testField() {
		final Annotation annotation = Tests.requireAnnotation(this, "f", NotNull.class);
		f = null;
		final IValidationFactoryPlugin<Object> plugin = new NotNullValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "f");
		assertSame(NotNullValidationFactoryPlugin.PROPERTY_NULL_MESSAGE, plugin.validate(data, null));
		assertNull(plugin.validate(data, ""));
		assertNull(plugin.validate(data, " "));
		assertNull(plugin.validate(data, ""));
		assertNull(plugin.validate(data, "false"));
		assertNull(plugin.validate(data, "FALSE"));
		assertNull(plugin.validate(data, "0"));
		assertNull(plugin.validate(data, "no"));
		assertNull(plugin.validate(data, "NO"));
	}

	@Test
	public void testMethod() {
		final Annotation annotation = Tests.requireAnnotation(this, "getF", NotNull.class);
		f = null;
		final IValidationFactoryPlugin<Object> plugin = new NotNullValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "f");
		assertSame(NotNullValidationFactoryPlugin.PROPERTY_NULL_MESSAGE, plugin.validate(data, null));
		assertNull(plugin.validate(data, ""));
		assertNull(plugin.validate(data, " "));
		assertNull(plugin.validate(data, ""));
		assertNull(plugin.validate(data, "false"));
		assertNull(plugin.validate(data, "FALSE"));
		assertNull(plugin.validate(data, "0"));
		assertNull(plugin.validate(data, "no"));
		assertNull(plugin.validate(data, "NO"));
	}
}
