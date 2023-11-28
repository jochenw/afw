package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;


/** Test for the {@link Locales} class.
 */
public class LocalesTest {
	/** Test case for {Locales#requireLocale(String)}.
	 */
	@Test
	public void testRequireLocale() {
		assertEquals(Locale.ENGLISH, Locales.requireLocale("en"));
		assertEquals(Locale.ENGLISH, Locales.requireLocale("EN"));
		assertEquals(Locale.US, Locales.requireLocale("en-US"));
		assertEquals(Locale.US, Locales.requireLocale("EN-US"));
		assertEquals(Locale.US, Locales.requireLocale("en-us"));
		assertEquals(Locale.US, Locales.requireLocale("en_US"));
		assertEquals(Locale.US, Locales.requireLocale("EN_US"));
		assertEquals(Locale.US, Locales.requireLocale("en_us"));
		assertEquals(Locale.UK, Locales.requireLocale("en_gb"));
		assertEquals(Locale.GERMANY, Locales.requireLocale("de-de"));
		try {
			Locales.requireLocale("$DOES_NOT_EXIST$");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid language tag: $DOES_NOT_EXIST$", e.getMessage());
		}
	}

	/** Test case for {Locales#getLocale(String)}.
	 */
	public void testGetLocale() {
		assertEquals(Locale.ENGLISH, Locales.getLocale("en"));
		assertEquals(Locale.ENGLISH, Locales.getLocale("EN"));
		assertEquals(Locale.US, Locales.getLocale("en-US"));
		assertEquals(Locale.US, Locales.getLocale("EN-US"));
		assertEquals(Locale.US, Locales.getLocale("en-us"));
		assertEquals(Locale.US, Locales.getLocale("en_US"));
		assertEquals(Locale.US, Locales.getLocale("EN_US"));
		assertEquals(Locale.US, Locales.getLocale("en_us"));
		assertEquals(Locale.UK, Locales.getLocale("en_gb"));
		assertEquals(Locale.GERMANY, Locales.getLocale("de-de"));
		assertNull(Locales.getLocale("$DOES_NOT_EXIST$"));
	}
}
