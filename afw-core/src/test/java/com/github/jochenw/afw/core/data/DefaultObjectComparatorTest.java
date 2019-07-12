package com.github.jochenw.afw.core.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.jochenw.afw.core.data.IObjectComparator.Listener;

import org.junit.Assert;

public class DefaultObjectComparatorTest {
	@Test
	public void testAtomicValues() {
		compare("3", "2", "", "Expected \"3\", got \"2\"");
		compare("2", Integer.valueOf(2), "", "Expected \"2\", got 2");
		compare(Boolean.TRUE, "true", "", "Expected true, got \"true\"");
	}

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
