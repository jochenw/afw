package com.github.jochenw.afw.core.el;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.el.tree.ElExpression;


public class ElEvaluatorTest {
	private static final ElEvaluator evaluator = new ElEvaluator(new DefaultPropertyResolver());
	private static final ElReader reader = new ElReader();

	@Test
	public void test() {
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