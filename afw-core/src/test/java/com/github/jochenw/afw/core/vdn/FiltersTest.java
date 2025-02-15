package com.github.jochenw.afw.core.vdn;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.function.Predicate;

import org.junit.Test;

import com.github.jochenw.afw.core.util.MutableInteger;
import com.github.jochenw.afw.core.vdn.Filters.ComparatorBuilder;
import com.github.jochenw.afw.core.vdn.Filters.PredicateBuilder;

/** Test suite for the {@link Filters} class.
 */
public class FiltersTest {
	/** Test case for {@link Filters#limit(int, int)}.
	 */
	@Test
	public void testLimit() {
		final Predicate<Integer> alwaysTruePredicate = Filters.limit(-1, -1);
		final Predicate<Integer> firstTenPredicate = Filters.limit(0, 10);
		final Predicate<Integer> secondTenPredicate = Filters.limit(10,10);
		final Predicate<Integer> lastTenPredicate = Filters.limit(40, 10);
		for (int i = 0;  i <= 50;  i++) {
			final Integer iv = Integer.valueOf(i);
			if (i < 10) {
				assertTrue(alwaysTruePredicate.test(iv));
				assertTrue(firstTenPredicate.test(iv));
				assertFalse(secondTenPredicate.test(iv));
				assertFalse(lastTenPredicate.test(iv));
			} else if (i >= 10  &&  i < 20) {
				assertTrue(alwaysTruePredicate.test(iv));
				assertFalse(iv.toString(), firstTenPredicate.test(iv));
				assertTrue(secondTenPredicate.test(iv));
				assertFalse(lastTenPredicate.test(iv));
			} else if (i >= 40  &&  i < 50) {
				assertTrue(alwaysTruePredicate.test(iv));
				assertFalse(firstTenPredicate.test(iv));
				assertFalse(secondTenPredicate.test(iv));
				assertTrue(lastTenPredicate.test(iv));
			} else {
				assertTrue(alwaysTruePredicate.test(iv));
				assertFalse(firstTenPredicate.test(iv));
				assertFalse(secondTenPredicate.test(iv));
				assertFalse(lastTenPredicate.test(iv));
			}
		}
	}

	/** Test case for {@link Filters#predicate(Class)}
	 */
	@Test
	public void testPredicate() {
		final Predicate<MutableInteger> miPredicate = (mi) -> mi.getValue() %2 == 0;
		@SuppressWarnings("null")
		final PredicateBuilder<MutableInteger> pb = Filters.predicate(MutableInteger.class);
		final Predicate<MutableInteger> predicate =
				pb.add(miPredicate)
				   .add((mi) -> Integer.valueOf(mi.getValue()), (i) -> i.intValue() == 2)
				   .build();
		final MutableInteger mi = new MutableInteger();
		assertFalse(predicate.test(mi));
		mi.setValue(1);
		assertFalse(predicate.test(mi));
		mi.setValue(2);
		assertTrue(predicate.test(mi));
	}

	/** Test case for {@link Filters#comparator()}.
	 */
	private static class ComparableBean {
		private final int id;
		private final String name;
		public ComparableBean(int pId, String pName) {
		    id = pId;
		    name = pName;
		}
	}

	@Test
	public void testComparator() {
		final ComparatorBuilder<ComparableBean> cb = Filters.comparator();
		final Comparator<ComparableBean> comparator =
				cb.add((cb1,cb2) -> cb1.name.compareTo(cb2.name))
				  .add((cBean) -> Integer.valueOf(cBean.id),
					   (i1,i2) -> Integer.compare(i1.intValue(), i2.intValue()))
				.build();
		final ComparableBean cb0 = new ComparableBean(0, "May, Brian");
		final ComparableBean cb1 = new ComparableBean(1, "May, Brian");
		final ComparableBean cb2 = new ComparableBean(1, "May, Alan");
		assertEquals(0, comparator.compare(cb0,  cb0));
		assertEquals(-1, comparator.compare(cb0,  cb1));
		assertEquals(1, comparator.compare(cb1,  cb0));
		assertEquals(1, comparator.compare(cb1, cb2));
		assertEquals(-1, comparator.compare(cb2, cb1));
	}
}
