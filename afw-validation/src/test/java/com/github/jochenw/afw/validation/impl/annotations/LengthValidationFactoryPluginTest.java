package com.github.jochenw.afw.validation.impl.annotations;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;

import org.junit.Test;

import com.github.jochenw.afw.validation.api.annotations.AnnotationData;
import com.github.jochenw.afw.validation.api.annotations.IValidationFactoryPlugin;
import com.github.jochenw.afw.validation.api.annotations.Length;
import com.github.jochenw.afw.validation.util.Tests;

public class LengthValidationFactoryPluginTest {
	@Length(code="LE01", minInclusive=3)
	private String f1;
	@Length(code="LE01", minExclusive=3)
	private String f2;

	@Length(code="LE02", minInclusive=3)
	private String getF1() {
		return f1;
	}
	@Length(code="LE02", minExclusive=3)
	private String getF2() {
		return f2;
	}
	@Length(code="LE03", maxInclusive=6)
	private String f3;
	@Length(code="LE03", maxExclusive=5)
	private String f4;

	@Length(code="LE05", maxInclusive=6)
	private String getF3() {
		return f3;
	}

	@Length(code="LE05", maxExclusive=5)
	private String getF4() {
		return f4;
	}
	
	@Test
	public void testFieldMinInclusive() {
		final Annotation annotation = Tests.requireAnnotation(this, "f1", Length.class);
		f1 = null;
		final IValidationFactoryPlugin<String> plugin = new LengthValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "f1");
		assertNotNull(data);
		String MIN_IS_3 = "The property value must have at least 3 characters, inclusive.";
		test(plugin, data, "The property value must not be null.", MIN_IS_3, MIN_IS_3, null, null,
		     MIN_IS_3, MIN_IS_3, MIN_IS_3, null, null, null, null, null, null, null, null);
	}

	@Test
	public void testMethodMinInclusive() {
		final Annotation annotation = Tests.requireAnnotation(this, "getF1", Length.class);
		f1 = null;
		final IValidationFactoryPlugin<String> plugin = new LengthValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "getF1");
		assertNotNull(data);
		String MIN_IS_3 = "The property value must have at least 3 characters, inclusive.";
		test(plugin, data, "The property value must not be null.", MIN_IS_3, MIN_IS_3, null, null, MIN_IS_3, MIN_IS_3, MIN_IS_3,
			 null, null, null, null, null, null, null, null);
	}

	@Test
	public void testFieldMaxInclusive() {
		final Annotation annotation = Tests.requireAnnotation(this, "f3", Length.class);
		f3 = null;
		final IValidationFactoryPlugin<String> plugin = new LengthValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "f3");
		String MAX_IS_6 = "The property value must have at most 6 characters, inclusive.";
		test(plugin, data, "The property value must not be null.", null, null, null, null, null,
			 null, null, null, null, null, null, MAX_IS_6, MAX_IS_6, MAX_IS_6, MAX_IS_6);
	}

	@Test
	public void testFieldMaxExclusive() {
		final Annotation annotation = Tests.requireAnnotation(this, "f4", Length.class);
		f3 = null;
		final IValidationFactoryPlugin<String> plugin = new LengthValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "f4");
		String MAX_IS_5 = "The property value must have at most 5 characters.";
		test(plugin, data, "The property value must not be null.", null, null, MAX_IS_5, MAX_IS_5, null,
			 null, null, null, null, null, MAX_IS_5, MAX_IS_5, MAX_IS_5, MAX_IS_5, MAX_IS_5);
	}

	@Test
	public void tesMethoddMaxInclusive() {
		final Annotation annotation = Tests.requireAnnotation(this, "getF3", Length.class);
		f3 = null;
		final IValidationFactoryPlugin<String> plugin = new LengthValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "getF3");
		String MAX_IS_6 = "The property value must have at most 6 characters, inclusive.";
		test(plugin, data, "The property value must not be null.", null, null, null, null, null,
			 null, null, null, null, null, null, MAX_IS_6, MAX_IS_6, MAX_IS_6, MAX_IS_6);
	}

	@Test
	public void tesMethoddMaxExclusive() {
		final Annotation annotation = Tests.requireAnnotation(this, "getF4", Length.class);
		f3 = null;
		final IValidationFactoryPlugin<String> plugin = new LengthValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "getF4");
		String MAX_IS_5 = "The property value must have at most 5 characters.";
		test(plugin, data, "The property value must not be null.", null, null, MAX_IS_5, MAX_IS_5, null,
			 null, null, null, null, null, MAX_IS_5, MAX_IS_5, MAX_IS_5, MAX_IS_5, MAX_IS_5);
	}

	
	final String[] validatedStringValues = new String[] {
			/* 0 */ null, /* 1 */ "", /* 2 */ " ", /* 3 */ "false", /* 4 */ "FALSE", /* 5 */ "0", /* 6 */ "no", /* 7 */ "NO",
			/* 8 */ "   ", /* 9 */ "    ", /* 10 */ "0123", /* 11 */ "01234", /* 12 */ "       ",
			/* 13 */ "        ", /* 14 */ "0123456", /* 15 */ "01234567"
	};
	private void test(IValidationFactoryPlugin<String> pPlugin, AnnotationData pData,
			          int pIndex, String pExpect) {
		final String v = validatedStringValues[pIndex];
		final String g = pPlugin.validate(pData, v);
		if (pExpect == null) {
			assertNull(String.valueOf(pIndex) + ": Expected null for input " + v + ", got " + g, g);
		} else {
			assertEquals(String.valueOf(pIndex), pExpect, g);
		}
	}

	private void test(IValidationFactoryPlugin<String> pPlugin, AnnotationData pData,
			          String... pExpectedValues) {
		assertEquals(validatedStringValues.length, pExpectedValues.length);
		for (int i = 0;  i < validatedStringValues.length; i++) {
			test(pPlugin, pData, i, pExpectedValues[i]);
		}
	}
	
	@Test
	public void testFieldMinExclusive() {
		final Annotation annotation = Tests.requireAnnotation(this, "f2", Length.class);
		f2 = null;
		final IValidationFactoryPlugin<String> plugin = new LengthValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "f2");
		assertNotNull(data);
		String MIN_IS_3 = "The property value must have at least 3 characters.";
		test(plugin, data, "The property value must not be null.", MIN_IS_3, MIN_IS_3, null, null,
		     MIN_IS_3,  MIN_IS_3, MIN_IS_3, MIN_IS_3, null, null, null, null, null, null, null);
	}

	@Test
	public void testMethodMinExclusive() {
		final Annotation annotation = Tests.requireAnnotation(this, "getF2", Length.class);
		f2 = null;
		final IValidationFactoryPlugin<String> plugin = new LengthValidationFactoryPlugin();
		final AnnotationData data = plugin.compile(annotation, getClass(), String.class, "getF2");
		String MIN_IS_3 = "The property value must have at least 3 characters.";
		test(plugin, data, "The property value must not be null.", MIN_IS_3, MIN_IS_3, null, null, MIN_IS_3, MIN_IS_3,
			 MIN_IS_3, MIN_IS_3, null, null, null, null, null, null, null);
	}
}
