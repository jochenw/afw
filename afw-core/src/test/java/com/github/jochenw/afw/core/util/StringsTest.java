/**
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

public class StringsTest {
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
}
