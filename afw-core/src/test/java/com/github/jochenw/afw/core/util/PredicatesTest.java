/**
 * 
 */
package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.function.Predicate;

import org.junit.Test;

/**
 * @author jwi
 *
 */
public class PredicatesTest {
	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Predicates#always(boolean)}.
	 */
	@Test
	public void testAlways() {
		final Predicate<String> truePred = Predicates.always(true);
		validate(truePred, true);
		final Predicate<String> falsePred = Predicates.always(false);
		validate(falsePred, false);
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Predicates#alwaysTrue()}.
	 */
	@Test
	public void testAlwaysTrue() {
		final Predicate<String> truePred = Predicates.alwaysTrue();
		validate(truePred, true);
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Predicates#alwaysFalse()}.
	 */
	@Test
	public void testAlwaysFalse() {
		final Predicate<String> truePred = Predicates.alwaysFalse();
		validate(truePred, false);
	}

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
}
