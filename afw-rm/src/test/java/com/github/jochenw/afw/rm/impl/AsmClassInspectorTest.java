package com.github.jochenw.afw.rm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rm.api.ClassInfo;
import com.github.jochenw.afw.rm.api.Resource;

public class AsmClassInspectorTest {
	@Resource(type="sql", title="Test Resource", description="Just for testing", version="0.0.1")
	public static class TestResource {
	}

	@Test
	public void test() throws Exception {
		final String name = TestResource.class.getName();
		final int offset = name.lastIndexOf('.');
		if (offset == -1) {
			throw new IllegalStateException("Unable to parse class name: " + name);
		}
		final String className = name.substring(offset+1);
		final URL url = getClass().getResource(className + ".class");
		Assert.assertNotNull(url);
		try (InputStream istream = url.openStream()) {
			final ClassInfo classInfo = new AsmClassInspector().getClassInfo(istream);
			Assert.assertNotNull(classInfo);
			Assert.assertEquals(TestResource.class.getName(), classInfo.getClassName());
			Assert.assertEquals("sql", classInfo.getType());
			Assert.assertEquals("Test Resource", classInfo.getTitle());
			Assert.assertEquals("Just for testing", classInfo.getDescription());
			Assert.assertEquals("0.0.1", classInfo.getVersion());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
