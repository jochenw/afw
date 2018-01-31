package com.github.jochenw.afw.rm.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rm.api.RmResourceRef;


public class ClassPathResourceRefRepositoryTest {
	@Test
	public void test() {
		final ClassPathResourceRefRepository repo = new ClassPathResourceRefRepository(Thread.currentThread().getContextClassLoader(), "org/junit");
		final List<RmResourceRef> resources = repo.getResources(null);
		boolean foundOrgJunitTest = false;
		for (RmResourceRef res : resources) {
			if ("org/junit/Test.class".equals(res.getUri())) {
				foundOrgJunitTest = true;
				break;
			}
		}
		Assert.assertTrue(foundOrgJunitTest);
	}

}
