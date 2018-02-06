package com.github.jochenw.afw.rcm.util;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rcm.api.Resource;
import com.github.jochenw.afw.rcm.util.AsmClassInfoScanner;
import com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.Annotation;
import com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.ClassInfo;

public class AsmAnnotationScannerTest {
	@Resource(type="class", version="0.1.3", title="Some Title", description="Some Description")
	public static class TestClass {
	}

	@Test
	public void testScanTestClass() throws Exception {
		final String className = TestClass.class.getName();
		final int offset = className.lastIndexOf('.');
		if (offset == -1) {
			throw new IllegalStateException("Unable to parse class name: " + className);
		}
		final String uri = className.substring(offset+1) + ".class";
		final URL url = TestClass.class.getResource(uri);
		try (final InputStream istream = url.openStream()) {
			final ClassInfo classInfo = new AsmClassInfoScanner().getClassInfo(istream);
			Assert.assertEquals(TestClass.class.getName(), classInfo.getClassName());
			final List<Annotation> annotations = classInfo.getAnnotations();
			Assert.assertEquals(1, annotations.size());
			final Annotation ann = annotations.get(0);
			Assert.assertNotNull(ann);
			Assert.assertEquals(8, ann.getValues().size());
			Assert.assertEquals(Resource.class.getName(), ann.getType());
			Assert.assertEquals("class", ann.getValue("type"));
			Assert.assertEquals("0.1.3", ann.getValue("version"));
			Assert.assertEquals("Some Title", ann.getValue("title"));
			Assert.assertEquals("Some Description", ann.getValue("description"));
		}
	}
}
