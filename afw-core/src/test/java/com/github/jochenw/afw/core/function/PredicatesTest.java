/**
 * 
 */
package com.github.jochenw.afw.core.function;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;


/** Test for the {@link Predicates} class.
 */
public class PredicatesTest {
	/**
	 * Test method for {@link com.github.jochenw.afw.core.function.Predicates#always(boolean)}.
	 */
	@Test
	public void testAlways() {
		final Predicate<String> truePred = Predicates.always(true);
		validate(truePred, true);
		final Predicate<String> falsePred = Predicates.always(false);
		validate(falsePred, false);
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.function.Predicates#alwaysTrue()}.
	 */
	@Test
	public void testAlwaysTrue() {
		final Predicate<String> truePred = Predicates.alwaysTrue();
		validate(truePred, true);
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.function.Predicates#alwaysFalse()}.
	 */
	@Test
	public void testAlwaysFalse() {
		final Predicate<String> truePred = Predicates.alwaysFalse();
		validate(truePred, false);
	}

    /** Validates, that the given (constant) predicate produces the expected results
     * @param pPredicate The predicate, that is being tested.
     * @param pStatus The predicates constant result value.
     */
	protected void validate(Predicate<? extends Object> pPredicate, boolean pStatus) {
		@SuppressWarnings("unchecked")
		final Predicate<String> strPred = (Predicate<String>) pPredicate;
		assertEquals(pStatus, strPred.test(null));
		assertEquals(pStatus, strPred.test("42"));
		assertEquals(pStatus, strPred.test("0"));
		assertEquals(pStatus, strPred.test("false"));
		assertEquals(pStatus, strPred.test("true"));
		@SuppressWarnings("unchecked")
		final Predicate<Integer> intPred = (Predicate<Integer>) pPredicate;
		assertEquals(pStatus, intPred.test(null));
		assertEquals(pStatus, intPred.test(Integer.valueOf(42)));
		assertEquals(pStatus, intPred.test(Integer.valueOf(0)));
		assertEquals(pStatus, intPred.test(Integer.valueOf(1)));
		@SuppressWarnings("unchecked")
		final Predicate<Boolean> boolPred = (Predicate<Boolean>) pPredicate;
		assertEquals(pStatus, boolPred.test(null));
		assertEquals(pStatus, boolPred.test(Boolean.TRUE));
		assertEquals(pStatus, boolPred.test(Boolean.FALSE));
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.function.Predicates#anyOf(Object,Predicate[])}.
	 */
	@Test
	public void testAnyOfVarArgs() {
		final TriFunction<Predicate<Boolean>, Predicate<Boolean>, Predicate<Boolean>, Predicate<Boolean>[]> arrayCreator = (p1, p2, p3) -> {
			@SuppressWarnings("unchecked")
			final Predicate<Boolean>[] array = (Predicate<Boolean>[]) Array.newInstance(Predicate.class, 3);
			array[0] = p1;
			array[1] = p2;
			array[2] = p3;
			return array;
		};
		final Predicate<Boolean> truep = Predicates.alwaysTrue();
		final Predicate<Boolean> falsep = Predicates.alwaysFalse();
		final Predicate<Boolean>[] list0 = arrayCreator.apply(truep, truep, truep);
		assertTrue(Predicates.anyOf(Boolean.TRUE, list0));
		assertTrue(Predicates.anyOf(Boolean.FALSE, list0));
		final Predicate<Boolean>[] list1 = arrayCreator.apply(truep, falsep, truep);
		assertTrue(Predicates.anyOf(Boolean.TRUE, list1));
		assertTrue(Predicates.anyOf(Boolean.FALSE, list1));
		final Predicate<Boolean>[] list2 = arrayCreator.apply(truep, truep, (Boolean b) -> b.booleanValue());
		assertTrue(Predicates.anyOf(Boolean.TRUE, list2));
		assertTrue(Predicates.anyOf(Boolean.FALSE, list2));
		final Predicate<Boolean>[] list3 = arrayCreator.apply(falsep, falsep, falsep);
		assertFalse(Predicates.anyOf(Boolean.TRUE, list3));
		assertFalse(Predicates.anyOf(Boolean.FALSE, list3));
		@SuppressWarnings("unchecked")
		final Predicate<Boolean>[] list4 = (Predicate<Boolean>[]) Array.newInstance(Predicate.class, 0);
		assertFalse(Predicates.anyOf(Boolean.FALSE, list4));
		assertFalse(Predicates.anyOf(Boolean.TRUE, list4));
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.function.Predicates#anyOf(Iterable,Object)}.
	 */
	@Test
	public void testAnyOf() {
		final Predicate<Boolean> truep = Predicates.alwaysTrue();
		final Predicate<Boolean> falsep = Predicates.alwaysFalse();
		final List<Predicate<Boolean>> list0 = Arrays.asList(truep, truep, truep);
		assertTrue(Predicates.anyOf(list0, Boolean.TRUE));
		assertTrue(Predicates.anyOf(list0, Boolean.FALSE));
		final List<Predicate<Boolean>> list1 = Arrays.asList(truep, falsep, truep);
		assertTrue(Predicates.anyOf(list1, Boolean.TRUE));
		assertTrue(Predicates.anyOf(list1, Boolean.FALSE));
		final List<Predicate<Boolean>> list2 = Arrays.asList(truep, truep, (Boolean b) -> b.booleanValue());
		assertTrue(Predicates.anyOf(list2, Boolean.TRUE));
		assertTrue(Predicates.anyOf(list2, Boolean.FALSE));
		final List<Predicate<Boolean>> list3 = Arrays.asList(falsep, falsep, falsep);
		assertFalse(Predicates.anyOf(list3, Boolean.TRUE));
		assertFalse(Predicates.anyOf(list3, Boolean.FALSE));
		final List<Predicate<Boolean>> list4 = new ArrayList<>();
		assertFalse(Predicates.anyOf(list4, Boolean.FALSE));
		assertFalse(Predicates.anyOf(list4, Boolean.FALSE));
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.function.Predicates#allOf(Iterable,Object)}.
	 */
	@Test
	public void testAllOf() {
		final Predicate<Boolean> truep = Predicates.alwaysTrue();
		final Predicate<Boolean> falsep = Predicates.alwaysFalse();
		final List<Predicate<Boolean>> list0 = Arrays.asList(truep, truep, truep);
		assertTrue(Predicates.allOf(list0, Boolean.TRUE));
		assertTrue(Predicates.allOf(list0, Boolean.FALSE));
		final List<Predicate<Boolean>> list1 = Arrays.asList(truep, falsep, truep);
		assertFalse(Predicates.allOf(list1, Boolean.TRUE));
		assertFalse(Predicates.allOf(list1, Boolean.FALSE));
		final List<Predicate<Boolean>> list2 = Arrays.asList(truep, truep, (Boolean b) -> b.booleanValue());
		assertTrue(Predicates.allOf(list2, Boolean.TRUE));
		assertFalse(Predicates.allOf(list2, Boolean.FALSE));
		final List<Predicate<Boolean>> list3 = Arrays.asList();
		assertTrue(Predicates.allOf(list3, Boolean.FALSE));
		assertTrue(Predicates.allOf(list3, Boolean.TRUE));
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.function.Predicates#allOf(Iterable,Object)}.
	 */
	@Test
	public void testAllOfVarArgs() {
		final TriFunction<Predicate<Boolean>, Predicate<Boolean>, Predicate<Boolean>, Predicate<Boolean>[]> arrayCreator = (p1, p2, p3) -> {
			@SuppressWarnings("unchecked")
			final Predicate<Boolean>[] array = (Predicate<Boolean>[]) Array.newInstance(Predicate.class, 3);
			array[0] = p1;
			array[1] = p2;
			array[2] = p3;
			return array;
		};
		final Predicate<Boolean> truep = Predicates.alwaysTrue();
		final Predicate<Boolean> falsep = Predicates.alwaysFalse();
		final Predicate<Boolean>[] list0 = arrayCreator.apply(truep, truep, truep);
		assertTrue(Predicates.allOf(Boolean.TRUE, list0));
		assertTrue(Predicates.allOf(Boolean.FALSE, list0));
		final Predicate<Boolean>[] list1 = arrayCreator.apply(truep, falsep, truep);
		assertFalse(Predicates.allOf(Boolean.TRUE, list1));
		assertFalse(Predicates.allOf(Boolean.FALSE, list1));
		final Predicate<Boolean>[] list2 = arrayCreator.apply(truep, truep, (Boolean b) -> b.booleanValue());
		assertTrue(Predicates.allOf(Boolean.TRUE, list2));
		assertFalse(Predicates.allOf(Boolean.FALSE, list2));
		@SuppressWarnings("unchecked")
		final Predicate<Boolean>[] list3 = (Predicate<Boolean>[]) Array.newInstance(Predicate.class, 0);
		assertTrue(Predicates.allOf(Boolean.FALSE, list3));
		assertTrue(Predicates.allOf(Boolean.TRUE, list3));
	}

}
