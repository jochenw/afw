package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.jochenw.afw.core.util.AnnotationScanner.Annotation;

public class AnnotationScannerTest {
	private static final String EXAMPLE = "@SuppressWarnings(user=\"jwi\", reason=\"Not Given\", rule=\"SomeRule\")";

	@Test
	public void testValidAnnotations() {
		String warnings = EXAMPLE;
		testValid(warnings);
		testValid("    " + warnings);
		testValid(warnings + "    ");
		testValid("    " + warnings + "    ");
		testValid("This annotation is embedded " + warnings + " into some random text.");
		testValid(EXAMPLE.replace(", ", ","));  // Can we omit the blanks?
		testValid(EXAMPLE.replace(", ", ",\r"));  // Can we use arbitrary whitespace?
		testValid(EXAMPLE.replace(", ", ",\r\n"));  // Can we use arbitrary whitespace?
		testValid(EXAMPLE.replace(", ", ",\t"));  // Can we use arbitrary whitespace?
		testValid(EXAMPLE.replace(")", "    )"));
	}

	private void testValid(String pWarnings) {
		final List<Annotation> annotations = AnnotationScanner.parse(pWarnings);
		assertNotNull(annotations);
		assertEquals(1, annotations.size());
		final Annotation annotation = annotations.get(0);
		assertNotNull(annotation);
		assertEquals("SuppressWarnings", annotation.getName());
		final Map<String,String> attributes = annotation.getAttributes(); 
		assertNotNull(attributes);
		assertEquals(3, attributes.size());
		assertEquals("jwi", attributes.get("user"));
		assertEquals("Not Given", attributes.get("reason"));
		assertEquals("SomeRule", attributes.get("rule"));
	}

	private void testInvalid(String pText) {
		final List<Annotation> annotations = AnnotationScanner.parse(pText);
		assertTrue(annotations.isEmpty());
	}
	
	@Test
	public void testInvalidAnnotations() {
		try {
			testInvalid("@SuppressWarnings(user=\"jwi\", user=\"ans\", rule=\"SomeRule\")");
		} catch (IllegalArgumentException e) {
			assertEquals("Attribute user is present twice in annotation SuppressWarnings", e.getMessage());
		}
		testInvalid("@SuppressWarnings");
		for (int i = 0;  i < EXAMPLE.length();  i++) {
			testInvalid(EXAMPLE.substring(0,  i));
		}
	}
}
