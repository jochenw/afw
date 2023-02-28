package com.github.jochenw.afw.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.jochenw.afw.core.data.IObjectComparator.Listener;

import org.junit.Assert;

/** Test for the {@link DefaultObjectComparator}.
 */
public class DefaultObjectComparatorTest {
	/** Test case for atomic values.
	 */
	@Test
	public void testAtomicValues() {
		compare("3", "2", "", "Expected \"3\", got \"2\"");
		compare("2", Integer.valueOf(2), "", "Expected \"2\", got 2i");
		compare(Long.valueOf(2), Integer.valueOf(2), "", "Expected 2l, got 2i");
		compare(Byte.valueOf((byte) 2), Short.valueOf((short) 2), "", "Expected 2, got 2s");
		compare(Boolean.TRUE, "true", "", "Expected true, got \"true\"");
	}

	/** Test case for list values.
	 */
	@Test
	public void testListValues() {
		Object[] objectArrayFourElements = new Object[] {"42", "17", "3", Boolean.TRUE};
		String[] stringArrayThreeElements = new String[] {"42", "17", "3"};
		compare(stringArrayThreeElements, objectArrayFourElements, "[3]", "Unexpected object found: true");
		compare(objectArrayFourElements, stringArrayThreeElements, "[3]", "Expected object not found: true");
		compare(stringArrayThreeElements, Arrays.asList(objectArrayFourElements), "[3]", "Unexpected object found: true");
		compare(Arrays.asList(objectArrayFourElements), stringArrayThreeElements, "[3]", "Expected object not found: true");
		compare(stringArrayThreeElements, Arrays.asList(objectArrayFourElements).iterator(), "[3]", "Unexpected object found: true");
		compare(Arrays.asList(objectArrayFourElements).iterator(), stringArrayThreeElements, "[3]", "Expected object not found: true");
		compare(objectArrayFourElements, new Object[] {"42", Integer.valueOf(17), "3", Boolean.FALSE},
				"[1]", "Expected \"17\", got 17i", "[3]", "Expected true, got false");
	}

	/** Test case for map values.
	 */
	@Test
	public void testMapValues() {
		final Map<String,Object> twoElementMap = new HashMap<>();
		twoElementMap.put("foo", "bar");
		twoElementMap.put("answer", Long.valueOf(42));
		final Map<String,Object> otherTwoElementMap = new HashMap<>();
		otherTwoElementMap.put("foo", "bar");
		otherTwoElementMap.put("answer", Integer.valueOf(42));
		compare(twoElementMap, otherTwoElementMap, "answer", "Expected 42l, got 42i");
		final Map<String,Object> threeElementMap = new HashMap<>(twoElementMap);
		threeElementMap.put("baz", Boolean.TRUE);
		compare(twoElementMap, threeElementMap, "baz", "Unexpected element: true");
		compare(threeElementMap, twoElementMap, "baz", "Expected element not found: true");
	}

	/** Compares an actual object against an expected-
	 * @param pExpect The expected object
	 * @param pGot The actual object.
	 * @param pIssues The list of expected issues.
	 */
	protected void compare(Object pExpect, Object pGot, String... pIssues) {
		final List<String> issues = new ArrayList<String>();
		new DefaultObjectComparator().compare(new Listener() {
			@Override
			public void difference(String pContext, String pDescription) {
				issues.add(pContext);
				issues.add(pDescription);
			}
		}, pExpect, pGot);
		if (pIssues != null) {
			Assert.assertEquals(pIssues.length, issues.size());
			for (int i = 0;  i < pIssues.length;  i++) {
				Assert.assertEquals(pIssues[i], issues.get(i));
			}
		}
	}
}
