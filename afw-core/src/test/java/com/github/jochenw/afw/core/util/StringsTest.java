package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

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
}
