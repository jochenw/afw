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
package com.github.jochenw.afw.core.el;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.el.tree.ElExpression;


/** Test for the {@link ElEvaluator}.
 */
public class ElEvaluatorTest {
	private static final ElEvaluator evaluator = new ElEvaluator(new DefaultPropertyResolver());
	private static final ElReader reader = new ElReader();

	/**
	 * Test case for a boolean value in a non-atomic object.
	 */
	@Test
	public void testBooleanExpression() {
		final ElExpression expr = reader.parse("nested.value");
		Assert.assertTrue((Boolean) evaluator.evaluate(expr, toMap("nested", toMap("value", Boolean.TRUE))));
	}
	/**
	 * Test case for a boolean expression, that uses atomic values.
	 */
	@Test
	public void testSimpleExpression() {
		final ElExpression expr = reader.parse("id != 'foo'  ||  4 > num");
		Assert.assertTrue(((Boolean) evaluator.evaluate(expr, toMap("id", "bar", "num", Long.valueOf(5)))).booleanValue());
		Assert.assertFalse(((Boolean) evaluator.evaluate(expr, toMap("id", "foo", "num", Long.valueOf(4)))).booleanValue());
		Assert.assertTrue(((Boolean) evaluator.evaluate(expr, toMap("id", "foo", "num", Long.valueOf(3)))).booleanValue());
		try {
			Assert.assertTrue(((Boolean) evaluator.evaluate(expr, toMap("id", "bar", "num", "5"))).booleanValue());
			Assert.fail("Expected exception");
		} catch (IllegalStateException e) {
			Assert.assertEquals("Unable to compare an instance of java.lang.Long, and an instance of java.lang.String", e.getMessage());
		}
		final ElExpression expr2 = reader.parse("id != 'foo'  ||  4 > num.toInt");
		Assert.assertTrue(((Boolean) evaluator.evaluate(expr2, toMap("id", "bar", "num", "5"))).booleanValue());
		Assert.assertFalse(((Boolean) evaluator.evaluate(expr2, toMap("id", "foo", "num", "4"))).booleanValue());
		Assert.assertTrue(((Boolean) evaluator.evaluate(expr2, toMap("id", "foo", "num", Integer.valueOf(3)))).booleanValue());
	}

	/** Converts the given key/value pairs into a map.
	 * @param pArgs The list of key/value pairs.
	 * @return The created map.
	 */
	protected Map<String,Object> toMap(Object... pArgs) {
		final Map<String,Object> map = new HashMap<>();
		for (int i = 0;  i < pArgs.length;  i += 2) {
			final String key = (String) pArgs[i];
			final Object value = pArgs[i+1];
			map.put(key, value);
		}
		return map;
	}
}
