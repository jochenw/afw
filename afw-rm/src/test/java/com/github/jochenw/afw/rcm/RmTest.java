package com.github.jochenw.afw.rcm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rcm.Rcm;
import com.github.jochenw.afw.rcm.RcmBuilder;
import com.github.jochenw.afw.rcm.api.ComponentFactory;
import com.github.jochenw.afw.rcm.api.JdbcConnectionProvider;
import com.github.jochenw.afw.rcm.impl.XmlFileResourceRegistry;

public class RmTest {
	@Test
	public void test1() throws Exception {
		String schemaPath = "target/test1/schema";
		final File schemaDir = new File(schemaPath);
		if (!schemaDir.isDirectory()  &&  !schemaDir.mkdirs()) {
			throw new IOException("Unable to create directory: " + schemaDir);
		}
		
		final String prefix = "com/github/jochenw/afw/rcm/test1";
		final String propsUri1 = prefix + "/test.properties.xml";
		final String propsUri2 = prefix + "/test.properties";
		final URL url1 = Thread.currentThread().getContextClassLoader().getResource(propsUri1);
		final URL url2 = Thread.currentThread().getContextClassLoader().getResource(propsUri2);
		final Properties props = new Properties();
		if (url1 == null) {
			if (url2 == null) {
				throw new IllegalStateException("Unable to locate " + propsUri1 + ", or " + propsUri2);
			} else {
				try (InputStream istream = url2.openStream()) {
					props.load(istream);
				}
			}
		} else {
			try (InputStream istream = url1.openStream()) {
				props.loadFromXML(istream);
			}
		}
		for (Map.Entry<Object,Object> en : props.entrySet()) {
			final String value = en.getValue().toString();
			final int offset = value.indexOf("${dbDir}");
			if (offset != -1) {
				final String v = value.substring(0, offset) + schemaPath + value.substring(offset+"${dbDir}".length());
				System.out.println(value + " => " + v);
				en.setValue(v);
			}
		}
		final RcmBuilder rcmBuilder = Rcm.builder()
				.resourceRepository(prefix)
				.properties(props)
				.installedResourceRegistry(new XmlFileResourceRegistry(new File(schemaDir, "resource-registry.xml")));
		final Rcm rcm = rcmBuilder.build();
		rcm.run();

		final ComponentFactory cf = rcm.getComponentFactory();
		final JdbcConnectionProvider jcp = cf.requireInstance(JdbcConnectionProvider.class);
		try (Connection conn = jcp.open()) {
			try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM Users")) {
				Long count = null; 
				try (ResultSet rs = countStmt.executeQuery()) {
					while(rs.next()) {
						count = Long.valueOf(rs.getLong(1));
					}
				}
				Assert.assertEquals(3l, count.longValue());
			}
			try (PreparedStatement readStmt = conn.prepareStatement("SELECT id, uid, email FROM Users WHERE id=?")) {
				assertUser(readStmt, 1, "jwi", "jwi@company.com");
				assertUser(readStmt, 2, "ans", "ans@company.com");
				assertUser(readStmt, 3, "thu", "thu@company.com");
			}
		}
	}

	private void assertUser(PreparedStatement pStmt, long pId, String pUid, String pEmail) throws SQLException {
		pStmt.setLong(1, pId);
		try (ResultSet rs = pStmt.executeQuery()) {
			if (!rs.next()) {
				throw new IllegalStateException("Expected result row");
			}
			Assert.assertEquals(pId, rs.getLong(1));
			Assert.assertEquals(pUid, rs.getString(2));
			Assert.assertEquals(pEmail, rs.getString(3));
		}
	}
}
