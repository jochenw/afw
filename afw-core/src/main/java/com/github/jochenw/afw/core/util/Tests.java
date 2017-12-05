package com.github.jochenw.afw.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

public class Tests {
	public static class TestFilePair {
		private final File inputFile;
		private final File outputFile;
		TestFilePair(File pInputFile, File pOutputFile) {
			inputFile = pInputFile;
			outputFile = pOutputFile;
		}
		public File getInputFile() {
			return inputFile;
		}
		public File getOutputFile() {
			return outputFile;
		}
	}
	public static URL requireResource(String pResource) {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return requireResource(cl, pResource);
	}

	public static void assertNotNull(String pMessage, Object pObject) {
		if (pObject == null) {
			fail(pMessage);
		}
	}
	
	public static final URL requireResource(ClassLoader pCl, String pResource) {
		final URL url = pCl.getResource(pResource);
		assertNotNull("Failed to locate resource: " + pResource + " (via ClassLoader " + pCl + ")", url);
		return url;
	}

	public static File requireFileResource(String pResource) {
		final URL url = requireResource(pResource);
		return assertFileResource(url);
	}

	public static File requireFileResource(ClassLoader pCl, String pResource) {
		final URL url = requireResource(pCl, pResource);
		return assertFileResource(url);
	}

	public static void assertEquals(String pMessage, int pExpect, int pGot) {
		if (pExpect != pGot) {
			fail(pMessage);
		}
	}

	public static void assertEquals(String pMessage, Object pExpect, Object pGot) {
		if (!pExpect.equals(pGot)) {
			fail(pMessage);
		}
	}

	public static void assertEquals(Object pExpect, Object pGot) {
		if (!pExpect.equals(pGot)) {
			fail("Assertion failed, expected " + pExpect + ", got " + pGot);
		}
	}

	public static void assertEquals(int pExpect, int pGot) {
		if (pExpect != pGot) {
			fail("Assertion failed, expected " + pExpect + ", got " + pGot);
		}
	}

	public static File assertFileResource(URL pUrl) {
		assertEquals("file", pUrl.getProtocol());
		return new File(pUrl.getFile());
	}

	public static void assertTrue(String pMessage, boolean pValue) {
		if (!pValue) {
			fail(pMessage);
		}
	}

	public static void assertTrue(boolean pValue) {
		if (!pValue) {
		    fail("Assertion failed, expected true, got false");
		}
	}

	public static void fail(String pMessage) {
		throw new IllegalStateException(pMessage);
	}
	
	public static File requireTestDirectory() {
		final File targetDir = new File("target");
		assertTrue(targetDir.isDirectory());
		final File testDir = new File(targetDir, "junit-work");
		if (!testDir.isDirectory()  &&   !testDir.mkdir()) {
			fail("Unable to create directory: " + testDir.getAbsolutePath());
		}
		return testDir;
	}

	public static void copy(File pSource, File pTarget) throws IOException {
		try (InputStream in = new FileInputStream(pSource);
			 OutputStream out = new FileOutputStream(pTarget)) {
			Streams.copy(in, out);
		}
	}

	public static TestFilePair getTestFiles(String pUri, String pInputSuffix, String pOutputSuffix) throws IOException {
		final File inputFile = Tests.requireFileResource(pUri);
		assertTrue(inputFile.isFile());
		assertTrue(inputFile.canRead());
		final File testDir = requireTestDirectory();
		final File testInputFile = new File(testDir, inputFile.getName());
		copy(inputFile, testInputFile);
		String name = inputFile.getName();
		final int offset = name.indexOf(pInputSuffix);
		if (offset == -1) {
			throw new IllegalStateException("The input files name does not contain the input suffix " + pInputSuffix + ": " + inputFile.getAbsolutePath());
		}
		name = name.substring(0, offset) + pOutputSuffix + name.substring(offset + pInputSuffix.length());
		final File testOutputFile = new File(testDir, name);
		return new TestFilePair(testInputFile, testOutputFile);
	}

	public static void assertArrayEquals(Object[] pGot, Object... pExpect) {
		for (int i = 0;  i < pGot.length;  i++) {
			assertEquals(String.valueOf(i), pExpect[i], pGot[i]);
		}
		assertEquals(pExpect.length, pGot.length);
	}

	public static void assertMapEquals(Map<String, String> pGot, String... pExpect) {
		for (int i = 0;  i < pExpect.length;  i += 2) {
			final String key = pExpect[i];
			final String expect = pExpect[i+1];
			final String got = pGot.get(key);
			assertEquals(i + ": " + key, expect, got);
		}
		assertEquals(pExpect.length/2, pGot.size());
	}
}
