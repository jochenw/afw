/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.log.ILog.Level;


/**
 * Test suite for the {@link Strings} class.
 */
public class StringsTest {
	/** Test case for {@link Strings#toString()}.
	 */
    @Test
    public void testToString() {
        final Object[] array = new Object[]{"a", Long.valueOf(-1), Boolean.FALSE};
        final Map<String,Object> map = new TreeMap<>();
        map.put("a", Integer.valueOf(2));
        map.put("b", array);
        map.put("c", Boolean.TRUE);
        assertEquals("null", Strings.toString(null));
        assertEquals("0", Strings.toString(Integer.valueOf(0)));
        assertEquals("true", Strings.toString(Boolean.TRUE));
        assertEquals("[a, -1, false]", Strings.toString(array));
        assertEquals("<a, -1, false>", Strings.toString(Arrays.asList(array)));
        assertEquals("(a, -1, false)", Strings.toString(Collections.unmodifiableCollection(Arrays.asList(array))));
        assertEquals("{a => 2, b => [a, -1, false], c => true}", Strings.toString(map));
    }

	/** Test case for {@link Strings#isEmpty(String)}, and {@link Strings#isTrimmedEmpty(String)}.
	 */
    @Test
    public void testIsEmpty() {
        assertTrue(Strings.isEmpty(null));
        assertTrue(Strings.isTrimmedEmpty(null));
        assertTrue(Strings.isEmpty(""));
        assertTrue(Strings.isTrimmedEmpty(""));
        assertFalse(Strings.isEmpty(" \t"));
        assertTrue(Strings.isTrimmedEmpty(" \t"));
        assertFalse(Strings.isEmpty(" 0\t"));
        assertFalse(Strings.isTrimmedEmpty(" 0\t"));
    }

    /** Test case for {@link Strings#parseVersionNumber(String)}.
     */
    @Test
    public void testParseVersionNumber() {
    	Assert.assertArrayEquals(new int[] {2,3,0}, Strings.parseVersionNumber("2.3.0"));
    	Assert.assertArrayEquals(new int[] {2,3,0,5,7}, Strings.parseVersionNumber("2.3.0.5.7"));
    	try {
    		Strings.parseVersionNumber(null);
    		Assert.fail("Expected exception");
    	} catch (IllegalArgumentException e) {
    		Assert.assertEquals("Missing, or empty, version string", e.getMessage());
    	}
    	try {
    		Strings.parseVersionNumber("");
    		Assert.fail("Expected exception");
    	} catch (IllegalArgumentException e) {
    		Assert.assertEquals("Missing, or empty, version string", e.getMessage());
    	}
    	try {
    		Strings.parseVersionNumber(".2.3.0");
    		Assert.fail("Expected exception");
    	} catch (IllegalArgumentException e) {
    		Assert.assertEquals("Invalid version string: .2.3.0", e.getMessage());
    	}
    	try {
    		Strings.parseVersionNumber("2.3..0");
    		Assert.fail("Expected exception");
    	} catch (IllegalArgumentException e) {
    		Assert.assertEquals("Invalid version string: 2.3..0", e.getMessage());
    	}
    	try {
    		Strings.parseVersionNumber("2.3.0.");
    		Assert.fail("Expected exception");
    	} catch (IllegalArgumentException e) {
    		Assert.assertEquals("Invalid version string, ends with '.': 2.3.0.", e.getMessage());
    	}
    }
   
    /** Test for {@link Strings#formatCb(Appendable, String, Object...)}.
     * @throws Exception The test failed.
     */
    @Test
    public void testFormatCb() throws Exception {
    	final String formatStr = "Hello, {} {}!";
    	assertEquals("Hello, beautiful world!", Strings.formatCb(formatStr, "beautiful", "world"));
    	assertEquals("Hello, funny 42!", Strings.formatCb(formatStr, "funny", Integer.valueOf(42)));
    	try {
    		Strings.formatCb(formatStr);
    		fail("Expected exception");
    	} catch (IllegalArgumentException e) {
    		assertEquals("Format string requires at least 0 arguments, but only 0 are given.", e.getMessage());
    	}
    	try {
    		Strings.formatCb(formatStr, (Object[]) null);
    		fail("Expected exception");
    	} catch (IllegalArgumentException e) {
    		assertEquals("Format string requires at least one argument, but none are given.", e.getMessage());
    	}
    	try {
    		Strings.formatCb(formatStr, "a", "b", "c");
    		fail("Expected exception");
    	} catch (IllegalArgumentException e) {
    		assertEquals("Format string requires only 2 arguments, but 3 are given.", e.getMessage());
    	}
    }

    /**
     * Test for {@link Strings#formatLz(int, int)}.
     * @throws Exception The test failed.
     */
    @Test
    public void testFormatLz() throws Exception {
    	assertEquals("0", Strings.formatLz(0, 9));
    	assertEquals("9", Strings.formatLz(9, 9));
    	assertEquals("00", Strings.formatLz(0, 10));
    	assertEquals("09", Strings.formatLz(9, 10));
    	assertEquals("00", Strings.formatLz(0, 99));
    	assertEquals("09", Strings.formatLz(9, 99));
       	assertEquals("000", Strings.formatLz(0, 100));
    	assertEquals("009", Strings.formatLz(9, 100));
    	assertEquals("000", Strings.formatLz(0, 999));
    	assertEquals("009", Strings.formatLz(9, 999));
    	assertEquals("0000", Strings.formatLz(0, 1000));
    	assertEquals("0009", Strings.formatLz(9, 1000));
    	assertEquals("0000", Strings.formatLz(0, 9999));
    	assertEquals("0009", Strings.formatLz(9, 9999));
    	assertEquals("00000", Strings.formatLz(0, 10000));
    	assertEquals("00009", Strings.formatLz(9, 10000));
    	assertEquals("00000", Strings.formatLz(0, 99999));
    	assertEquals("00009", Strings.formatLz(9, 99999));
    	assertEquals("000000", Strings.formatLz(0, 100000));
    	assertEquals("000009", Strings.formatLz(9, 100000));
    	assertEquals("000000", Strings.formatLz(0, 999999));
    	assertEquals("000009", Strings.formatLz(9, 999999));
    	try {
    		Strings.formatLz(0, 9999999);
    		fail("Expected exception");
    	} catch (IllegalArgumentException e) {
    		assertEquals("The total number of items must be lower than 1000000", e.getMessage());
    	}
    }

    /** Test case for {@link Strings#matcher(String)}.
     */
    @Test
    public void testMatcher() {
    	final Predicate<String> fooMatcher = Strings.matcher("Foo");
    	assertTrue(fooMatcher.test("Foo"));
    	assertFalse(fooMatcher.test("foo"));
    	assertFalse(fooMatcher.test("0"));
    	final Predicate<String> notFooMatcher = Strings.matcher("!Foo");
    	assertFalse(notFooMatcher.test("Foo"));
    	assertTrue(notFooMatcher.test("foo"));
    	assertTrue(notFooMatcher.test("0"));
    	final Predicate<String> fooMatcherCaseInsensitive = Strings.matcher("Foo/i");
    	assertTrue(fooMatcherCaseInsensitive.test("Foo"));
    	assertTrue(fooMatcherCaseInsensitive.test("foo"));
    	assertFalse(fooMatcherCaseInsensitive.test("0"));
    	final Predicate<String> notFooMatcherCaseInsensitive = Strings.matcher("!Foo/i");
    	assertFalse(notFooMatcherCaseInsensitive.test("Foo"));
    	assertFalse(notFooMatcherCaseInsensitive.test("foo"));
    	assertTrue(notFooMatcherCaseInsensitive.test("0"));
    	final Predicate<String> wmMatcher = Strings.matcher("Wm*");
    	assertTrue(wmMatcher.test("Wm"));
    	assertTrue(wmMatcher.test("Wm9"));
    	assertTrue(wmMatcher.test("WmFoo"));
    	assertFalse(wmMatcher.test("wmFoo"));
    	assertFalse(wmMatcher.test("wM9"));
    	final Predicate<String> notWmMatcher = Strings.matcher("!Wm*");
    	assertFalse(notWmMatcher.test("Wm"));
    	assertFalse(notWmMatcher.test("Wm9"));
    	assertFalse(notWmMatcher.test("WmFoo"));
    	assertTrue(notWmMatcher.test("wmFoo"));
    	assertTrue(notWmMatcher.test("wM9"));
    	final Predicate<String> wmMatcherCaseInsensitive = Strings.matcher("Wm*/i");
    	assertTrue(wmMatcherCaseInsensitive.test("Wm"));
    	assertTrue(wmMatcherCaseInsensitive.test("Wm9"));
    	assertTrue(wmMatcherCaseInsensitive.test("WmFoo"));
    	assertTrue(wmMatcherCaseInsensitive.test("wmFoo"));
    	assertTrue(wmMatcherCaseInsensitive.test("wM9"));
    	final Predicate<String> notWmMatcherCaseInsensitive = Strings.matcher("!Wm*/i");
    	assertFalse(notWmMatcherCaseInsensitive.test("Wm"));
    	assertFalse(notWmMatcherCaseInsensitive.test("Wm9"));
    	assertFalse(notWmMatcherCaseInsensitive.test("WmFoo"));
    	assertFalse(notWmMatcherCaseInsensitive.test("wmFoo"));
    	assertFalse(notWmMatcherCaseInsensitive.test("wM9"));
    	final Predicate<String> reMatcher = Strings.matcher("re:ab+C");
    	assertFalse(reMatcher.test("aC"));
    	assertTrue(reMatcher.test("abC"));
    	assertTrue(reMatcher.test("abbbbbbC"));
    	assertFalse(reMatcher.test("aBC"));
    	assertFalse(reMatcher.test("abCd"));
    	final Predicate<String> notReMatcher = Strings.matcher("!re:ab+C");
    	assertTrue(notReMatcher.test("aC"));
    	assertFalse(notReMatcher.test("abC"));
    	assertFalse(notReMatcher.test("abbbbbbC"));
    	assertTrue(notReMatcher.test("aBC"));
    	assertTrue(notReMatcher.test("abCd"));
    	final Predicate<String> reMatcherCaseInsensitive = Strings.matcher("re:ab+C/i");
    	assertFalse(reMatcherCaseInsensitive.test("aC"));
    	assertTrue(reMatcherCaseInsensitive.test("aBC"));
    	assertTrue(reMatcherCaseInsensitive.test("abbbbbbC"));
    	assertTrue(reMatcherCaseInsensitive.test("aBC"));
    	assertFalse(reMatcherCaseInsensitive.test("abCd"));
    	final Predicate<String> notReMatcherCaseInsensitive = Strings.matcher("!re:ab+C/i");
    	assertTrue(notReMatcherCaseInsensitive.test("aC"));
    	assertFalse(notReMatcherCaseInsensitive.test("aBC"));
    	assertFalse(notReMatcherCaseInsensitive.test("abbbbbbC"));
    	assertFalse(notReMatcherCaseInsensitive.test("aBC"));
    	assertTrue(notReMatcherCaseInsensitive.test("abCd"));
    }

    /** Test case for {@link Strings#matchers(String,String)}.
     */
    @Test
    public void testMatchers() {
    	final Predicate<String> pred = Strings.matchers("Foo,Wm*,!Wmx,re:ab+C", ",");
    	assertTrue(pred.test("Foo"));
    	assertFalse(pred.test("foo"));
    	assertTrue(pred.test("Wm"));
    	assertFalse(pred.test("Wmx"));
    	assertTrue(pred.test("Wm10"));
    	assertTrue(pred.test("WmRoot"));
    	assertFalse(pred.test("wm"));
    	assertTrue(pred.test("abC"));
    	assertFalse(pred.test("ac"));
    	assertFalse(pred.test("Bar"));
    	final Predicate<String> predCi = Strings.matchers("Foo/i,Wm*/i,!Wmx/i,re:ab+C/i", ",");
    	assertTrue(predCi.test("Foo"));
    	assertTrue(predCi.test("foo"));
    	assertTrue(predCi.test("Wm"));
    	assertTrue(predCi.test("Wm10"));
    	assertTrue(predCi.test("WmRoot"));
    	assertTrue(predCi.test("wm"));
    	assertFalse(predCi.test("wmx"));
    	assertFalse(predCi.test("Wmx"));
    	assertFalse(predCi.test("aC"));
    	assertFalse(predCi.test("ac"));
    	assertTrue(predCi.test("aBC"));
    	assertTrue(predCi.test("abc"));
    	assertFalse(predCi.test("Bar"));
    }

    /** Test case for {@link Strings#split(String, String)}.
     */
    @Test
    public void testSplit() {
    	final String value0 = "a b c d";
    	final List<String> splittedValue0 = Strings.split(value0, " ");
    	assertEquals(4, splittedValue0.size());
    	assertEquals("a", splittedValue0.get(0));
    	assertEquals("b", splittedValue0.get(1));
    	assertEquals("c", splittedValue0.get(2));
    	assertEquals("d", splittedValue0.get(3));
    	final String value1 = "a, b, c,  d";
    	final List<String> splittedValue1 = Strings.split(value1, ", ");
    	assertEquals(4, splittedValue1.size());
    	assertEquals("a", splittedValue1.get(0));
    	assertEquals("b", splittedValue1.get(1));
    	assertEquals("c", splittedValue1.get(2));
    	assertEquals(" d", splittedValue1.get(3));
    }

    /** Test case for {@link Strings#list(String...)}.
     */
    @Test
    public void testList() {
    	final ArrayList<String> values = Strings.list("a", "c", "b");
    	assertEquals(3, values.size());
    	assertEquals("a", values.get(0));
    	assertEquals("c", values.get(1));
    	assertEquals("b", values.get(2));
    }

    /** Test case for {@link Strings#list(String...)}.
     */
    @Test
    public void testArray() {
    	final String[] values = Strings.array("a", "c", "b");
    	assertEquals(3, values.length);
    	assertEquals("a", values[0]);
    	assertEquals("c", values[1]);
    	assertEquals("b", values[2]);
    }

    /** Test case for {@link Strings#join(String,Class)}.
     */
    @Test
    public void testJoinEnums() {
    	final String joinedNames = Strings.join("|", Level.class);
    	assertEquals("TRACE|DEBUG|INFO|WARN|ERROR|FATAL", joinedNames);
    }

    /** Test case for {@link Strings#join(String,String...)}
     */
    @Test
    public void testJoinStrings() {
    	final List<String> namesList = Arrays.asList(Person.SOME_PERSONS).stream().map(Person::getName).collect(Collectors.toList());
    	final String[] namesArray = namesList.toArray(new String[namesList.size()]);
    	assertEquals(Person.SOME_PERSONS_STRING, Strings.join(", ", namesArray));
    	assertEquals(Person.SOME_PERSONS_STRING.replace(", ", ","), Strings.join(",", namesArray));
    }

    /** Test case for {@link Strings#join(String,Iterable)}
     */
    @Test
    public void testJoinStringList() {
    	final List<String> namesList = Arrays.asList(Person.SOME_PERSONS).stream().map(Person::getName).collect(Collectors.toList());
    	assertEquals(Person.SOME_PERSONS_STRING, Strings.join(", ", namesList));
    	assertEquals(Person.SOME_PERSONS_STRING.replace(", ", ","), Strings.join(",", namesList));
    }

    /** Test case for {@link Strings#notEmpty(String,String)}.
     */
    @Test
    public void testNotEmptyStringString() {
    	final String DEFAULT = "default";
    	assertSame(DEFAULT, Strings.notEmpty(null, DEFAULT));
    	assertSame(DEFAULT, Strings.notEmpty("", DEFAULT));
    	assertEquals("0", Strings.notEmpty("0", DEFAULT));
    	assertEquals("false", Strings.notEmpty("false", DEFAULT));
    }

    /** Test case for {@link Strings#notEmpty(String,Supplier)}.
     */
    @Test
    public void testNotEmptyStringSupplier() {
    	final String DEFAULT = "default";
    	assertSame(DEFAULT, Strings.notEmpty(null, () -> DEFAULT));
    	assertSame(DEFAULT, Strings.notEmpty("", () -> DEFAULT));
    	assertEquals("0", Strings.notEmpty("0", () -> DEFAULT));
    	assertEquals("false", Strings.notEmpty("false", () -> DEFAULT));
    }

    private static class Person {
    	private final String name;
    	private Person(String pName) {
    		name = pName;
    	}
    	public String getName() { return name; }
    	public static Person of(String pName) {
    		return new Person(pName);
    	}
    	public static Person[] SOME_PERSONS = new Person[] {
        	of("Dennis Ritchie"),
        	of("Ken Thompson"),
    		of("Linus Torvalds"),
    		of("Michael Widenius")
    	};
    	public static String SOME_PERSONS_STRING =
    		"Dennis Ritchie, Ken Thompson, Linus Torvalds, Michael Widenius";
    }

    /**
     * Test case for {@link Strings#tokenize(String, String)}.
     */
    @Test
    public void testTokenizeStringString() {
    	final List<String> names = Strings.tokenize(", ", Person.SOME_PERSONS_STRING);
    	final List<String> names2 = Strings.tokenize(",", Person.SOME_PERSONS_STRING);
    	assertEquals(Person.SOME_PERSONS.length, names.size());
    	assertEquals(Person.SOME_PERSONS.length, names2.size());
    	for (int i = 0;  i < names.size();  i++) {
    		assertEquals(Person.SOME_PERSONS[i].getName(), names.get(i));
    		if (i == 0) {
        		assertEquals(Person.SOME_PERSONS[i].getName(), names2.get(i));
    		} else {
        		assertEquals(" " + Person.SOME_PERSONS[i].getName(), names2.get(i));
    		}
    	}
    }
    /** Test case for {@link Strings#join(CharSequence, Iterable, Functions.FailableFunction)}.
     */
    @Test
    public void testJoinStringIterableFunction() {
    	final String actual = Strings.join(", ", Arrays.asList(Person.SOME_PERSONS), Person::getName);
    	assertEquals(Person.SOME_PERSONS_STRING, actual);
    }

    /** Test case for {@link Strings#join(CharSequence, Object[], Functions.FailableFunction)}.
     */
    @Test
    public void testJoinStringArrayFunction() {
    	final String actual = Strings.join(", ", Person.SOME_PERSONS, Person::getName);
    	assertEquals(Person.SOME_PERSONS_STRING, actual);
    }

    /** Test case for {@link Strings#join(CharSequence, Functions.FailableFunction, Object...)}
     */
    @Test
    public void testJoinStringFunctionArray() {
    	final String actual = Strings.join(", ", Person::getName, Person.SOME_PERSONS);
    	assertEquals(Person.SOME_PERSONS_STRING, actual);
    }

    /** Test case for {@link Strings#requireNonNull(String,String)}.
     */
    @Test
    public void testRequireNonEmptyStringString() {
    	final String abc = "abc";
    	assertSame(abc, Strings.requireNonEmpty(abc, "abc"));
    	assertSame(" ", Strings.requireNonEmpty(" ", "abc"));
    	try {
    		Strings.requireNonEmpty(null, "abc");
    		fail("Expected Exception");
    	} catch (NullPointerException e) {
    		assertEquals(e.getMessage(), "String value must not be null: abc");
    	}
    	try {
    		Strings.requireNonEmpty("", "abc");
    		fail("Expected Exception");
    	} catch (IllegalArgumentException e) {
    		assertEquals(e.getMessage(), "String value must not be empty: abc");
    	}
    }

    /** Test case for {@link Strings#requireTrimmedNonEmpty(String,String)}.
     */
    @Test
    public void testRequireTrimmedNonEmptyStringString() {
    	final String abc = "abc";
    	assertSame(abc, Strings.requireTrimmedNonEmpty(abc, "abc"));
    	try {
    		Strings.requireTrimmedNonEmpty(null, "abc");
    		fail("Expected Exception");
    	} catch (NullPointerException e) {
    		assertEquals(e.getMessage(), "String value must not be null: abc");
    	}
    	try {
    		Strings.requireTrimmedNonEmpty("", "abc");
    		fail("Expected Exception");
    	} catch (IllegalArgumentException e) {
    		assertEquals(e.getMessage(), "String value must not be empty: abc");
    	}
    	try {
    		Strings.requireTrimmedNonEmpty(" ", "abc");
    		fail("Expected Exception");
    	} catch (IllegalArgumentException e) {
    		assertEquals(e.getMessage(), "String value must not be empty (after trimming): abc");
    	}
    }

    /** Test case for {@link Strings#requireNonNull(String,String)}.
     */
    @Test
    public void testRequireNonNullStringString() {
    	final String abc = "abc";
    	assertSame(abc, Strings.requireNonNull(abc, "abc"));
    	try {
    		Strings.requireNonNull(null, "abc is null");
    		fail("Expected Exception");
    	} catch (NullPointerException e) {
    		assertEquals("abc is null", e.getMessage());
    	}
    }
   
    /** Test case for {@link Strings#append(StringBuilder, Object)}.
     */
    @Test
    public void testAppendStringBuilderObject() {
    	final Function<Object,String> tester = (o) -> {
    		final StringBuilder sb = new StringBuilder();
    		Strings.append(sb, o);
    		return sb.toString();
    	};
    	assertEquals("abc", tester.apply("abc"));
    	assertEquals("null", tester.apply(null));
    	assertEquals("42", tester.apply(Integer.valueOf(42)));
    	assertEquals("true", tester.apply(Boolean.TRUE));
    }

    /** Test case for {@link Strings#append(StringBuilder, Object...)}.
     */
    @Test
    public void testAppendStringBuilderObjects() {
    	final Function<Object[],String> tester = (objs) -> {
    		final StringBuilder sb = new StringBuilder();
    		Strings.append(sb, objs);
    		return sb.toString();
    	};
    	assertEquals("abc, def", tester.apply(new String[] {"abc", "def"}));
    	assertEquals("null", tester.apply(null));
    	assertEquals("42", tester.apply(new Object[] {Integer.valueOf(42)}));
    	assertEquals("true", tester.apply(new Object[] {Boolean.TRUE}));
    }
}
