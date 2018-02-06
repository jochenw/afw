package com.github.jochenw.afw.rcm.impl;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rcm.impl.DefaultSqlReader;


public class DefaultSqlReaderTest {
	final String SQL =
			"-- This is a comment. It will be ignored.\n" +
	        "-- Now, let's create a table:\n" +
			"DROP TABLE IF EXISTS Tab1;\n" +
	        "CREATE TABLE Tab1 (\n" +
			"  id BIGINT NOT NULL PRIMARY KEY,\n" +
	        "  name VARCHAR(32) NOT NULL\n" +
			");\n" +
	        "DROP SEQUENCE IF EXISTS Seq1;\n" +
	        "CREATE SEQUENCE Seq1;\n";

	@Test
	public void test() throws Exception {
		final DefaultSqlReader sqlr = new DefaultSqlReader();
		final StringReader sr = new StringReader(SQL);
		final BufferedReader br = new BufferedReader(sr);
		Assert.assertTrue(sqlr.hasNextLine(br));
		Assert.assertEquals("DROP TABLE IF EXISTS Tab1", sqlr.nextLine(br));
		Assert.assertTrue(sqlr.hasNextLine(br));
		final String line = sqlr.nextLine(br);
		line.replaceAll("\\\\r\\\\n", "\n");
		Assert.assertEquals("CREATE TABLE Tab1 (\n" 
				+ "  id BIGINT NOT NULL PRIMARY KEY,\n" 
				+ "  name VARCHAR(32) NOT NULL\n"
				+ ")", line);
		Assert.assertTrue(sqlr.hasNextLine(br));
		Assert.assertEquals("DROP SEQUENCE IF EXISTS Seq1", sqlr.nextLine(br));
		Assert.assertTrue(sqlr.hasNextLine(br));
		Assert.assertEquals("CREATE SEQUENCE Seq1", sqlr.nextLine(br));
		Assert.assertFalse(sqlr.hasNextLine(br));
	}

}
