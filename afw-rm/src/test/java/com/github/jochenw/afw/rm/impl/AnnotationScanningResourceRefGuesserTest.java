package com.github.jochenw.afw.rm.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rm.api.RmLogger;
import com.github.jochenw.afw.rm.api.RmResourceInfo;
import com.github.jochenw.afw.rm.api.RmResourceRef;
import com.github.jochenw.afw.rm.api.RmResourceRefGuesser.RmResourceInfoRequest;

public class AnnotationScanningResourceRefGuesserTest {
	private static final String SQL_SCRIPT =
		"-- @Resource(type=\"sql\", version=\"0.0.1\", title=\"Test Script\", description=\"Description\")\n" +
	    "CREATE TABLE foo(id BIGINT NOT NULL PRIMARY KEY, name VARCHAR(10) NOT NULL)";

	@Test
	public void testSqlScript() throws Exception {
		final RmResourceInfo info = parse(SQL_SCRIPT);
		Assert.assertNotNull(info);
		Assert.assertEquals("sql", info.getType());
		Assert.assertEquals("Test Script", info.getTitle());
		Assert.assertEquals("Description", info.getDescription());
		Assert.assertArrayEquals(new int[] {0,0,1}, info.getVersion().getNumbers());
	}

	private RmResourceInfo parse(String pResource) throws IOException {
		final RmLogger logger = new SimpleRmLogger();
		final RmResourceRef ref = new RmResourceRef() {
			@Override
			public String getUri() {
				return AnnotationScanningResourceRefGuesserTest.class.getSimpleName();
			}

			@Override
			public String getLocation() {
				return AnnotationScanningResourceRefGuesserTest.class.getCanonicalName();
			}
		};
		final RmResourceInfoRequest request = new RmResourceInfoRequest() {
			@Override
			public RmLogger getLogger() {
				return logger;
			}

			@Override
			public RmResourceRef getResourceRef() {
				return ref;
			}

			@Override
			public InputStream open() throws IOException {
				final byte[] bytes = pResource.getBytes(StandardCharsets.UTF_8);
				return new ByteArrayInputStream(bytes);
			}
			
		};
		return new AnnotationScanningResourceRefGuesser().getInfo(request);
	}
}
