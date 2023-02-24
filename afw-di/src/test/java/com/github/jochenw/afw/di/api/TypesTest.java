package com.github.jochenw.afw.di.api;

import static org.junit.Assert.*;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.junit.Test;

/** Test for the {@link Types} class.
 */
public class TypesTest {
	/** Test for the public default constructor. (This default
	 * constructor isn't actually used, but it is considered
	 * as part of the coverage.)
	 */
	@Test
	public void testConstructor() {
		assertNotNull(new Types());
	}

	/** Test for {@link Types.Type#Type() creating} an instance with
	 * a generic type.
	 */
	@Test
	public void testCreateGenericType() {
		final Types.Type<String> stringType = new Types.Type<String>() {};
		assertEquals(String.class, stringType.getRawType());
		final Types.Type<List<String>> listStringType = new Types.Type<List<String>>() {};
		final ParameterizedType pt = (ParameterizedType) listStringType.getRawType();
		assertEquals(List.class, pt.getRawType());
		final java.lang.reflect.Type[] parameterTypes = pt.getActualTypeArguments();
		assertEquals(1, parameterTypes.length);
		assertEquals(String.class, parameterTypes[0]);
		try {
			new Types.Type<>(String.class);
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Unsupported type: " + String.class, e.getMessage());
		}
	}

}
