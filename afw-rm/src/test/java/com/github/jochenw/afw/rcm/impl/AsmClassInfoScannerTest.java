package com.github.jochenw.afw.rcm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rcm.api.Resource;
import com.github.jochenw.afw.rcm.util.AsmClassInfoScanner;
import com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.Annotation;
import com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.ClassInfo;

public class AsmClassInfoScannerTest {
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
			final ClassInfo classInfo = new AsmClassInfoScanner().getClassInfo(istream);
			Assert.assertNotNull(classInfo);
			Assert.assertEquals(TestResource.class.getName(), classInfo.getClassName());
			Annotation resAnnotation = null;
			for (Annotation annotation : classInfo.getAnnotations()) {
				if (Resource.class.getName().equals(annotation.getType())) {
					resAnnotation = annotation;
					break;
				}
			}
			Assert.assertNotNull(resAnnotation);
			Assert.assertEquals("sql", resAnnotation.getValue("type"));
			Assert.assertEquals("Test Resource", resAnnotation.getValue("title"));
			Assert.assertEquals("Just for testing", resAnnotation.getValue("description"));
			Assert.assertEquals("0.0.1", resAnnotation.getValue("version"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
