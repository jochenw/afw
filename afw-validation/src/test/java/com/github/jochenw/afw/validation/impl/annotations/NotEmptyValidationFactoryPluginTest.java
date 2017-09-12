package com.github.jochenw.afw.validation.impl.annotations;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;

import org.junit.Test;

import com.github.jochenw.afw.validation.api.annotations.AnnotationData;
import com.github.jochenw.afw.validation.api.annotations.IValidationFactoryPlugin;
import com.github.jochenw.afw.validation.api.annotations.NotEmpty;
import com.github.jochenw.afw.validation.api.annotations.NotNull;
import com.github.jochenw.afw.validation.util.Tests;

public class NotEmptyValidationFactoryPluginTest {
	@NotEmpty(code="NE01")
	private String f;

	@NotEmpty(code="NE02")
	private String getF() {
		return f;
	}
	
	@Test
	public void testField() {
		final Annotation annotation = Tests.requireAnnotation(this, "f", NotEmpty.class);
		f = null;
		final IValidationFactoryPlugin<String> plugin = new NotEmptyValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "f");
		assertNotNull(data);
		assertEquals("The property value must not be null.", plugin.validate(data, null));
		assertEquals("The property value must not be empty.", plugin.validate(data, ""));
		assertNull(plugin.validate(data, " "));
		assertNull(plugin.validate(data, "false"));
		assertNull(plugin.validate(data, "FALSE"));
		assertNull(plugin.validate(data, "0"));
		assertNull(plugin.validate(data, "no"));
		assertNull(plugin.validate(data, "NO"));
	}

	@Test
	public void testMethod() {
		final Annotation annotation = Tests.requireAnnotation(this, "getF", NotEmpty.class);
		f = null;
		final IValidationFactoryPlugin<String> plugin = new NotEmptyValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "f");
		assertNotNull(data);
		assertSame(NotNullValidationFactoryPlugin.PROPERTY_NULL_MESSAGE, plugin.validate(data, null));
		assertEquals("The property value must not be empty.", plugin.validate(data, ""));
		assertNull(plugin.validate(data, " "));
		assertNull(plugin.validate(data, "false"));
		assertNull(plugin.validate(data, "FALSE"));
		assertNull(plugin.validate(data, "0"));
		assertNull(plugin.validate(data, "no"));
		assertNull(plugin.validate(data, "NO"));
	}
}
