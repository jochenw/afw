package com.github.jochenw.afw.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/** Test for the {@link Tupel} class.
 */
public class TupelTest {
	/** Test case for the Tupel constructor.
	 */
	@Test
	public void testConstructor() {
		final Integer i = Integer.valueOf(13);
		final String s = "foo";
		final Tupel<Integer,String> t1 = new Tupel<Integer,String>(i, s);
		assertEquals(i, t1.getAttribute1());
		assertEquals(s, t1.getAttribute2());
	}

	/** Test case for the {@link Tupel#equals(Object)}
	 */
	@Test
	public void testEqual() {
		final Integer i = Integer.valueOf(13);
		final String s = "foo";
		final Tupel<Integer,String> t1 = new Tupel<Integer,String>(i, s);
		assertTrue(t1.equals(new Tupel<Integer,String>(i, s)));
		assertFalse(t1.equals(new Tupel<String,Integer>(s,i)));
	}

	/** Test case for the {@link Tupel#hashCode()}
	 */
	@Test
	public void testHashCode() {
		final Integer i = Integer.valueOf(13);
		final String s = "foo";
		final Tupel<Integer,String> t1 = new Tupel<Integer,String>(i, s);
		assertEquals(t1.hashCode(), new Tupel<Integer,String>(i, s).hashCode());
	}

	/** Test case for the {@link Tupel#of(Object,Object)}
	 */
	@Test
	public void testOf() {
		final Integer i = Integer.valueOf(13);
		final String s = "foo";
		final Tupel<Integer,String> t1 = Tupel.of(i, s);
		assertEquals(i, t1.getAttribute1());
		assertEquals(s, t1.getAttribute2());
		assertEquals(t1, new Tupel<Integer,String>(i, s));
	}
}
