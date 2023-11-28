/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.jochenw.afw.core.function.Functions;


/** Utility class for unit tests.
 */
public class Tests {
	/** A pair of files. Typically, the input file must not be modified.
	 * The output file, however, has been created by copying the input file,
	 * and may be modified.
	 */
	public static class TestFilePair {
		private final File inputFile;
		private final File outputFile;
		TestFilePair(File pInputFile, File pOutputFile) {
			inputFile = pInputFile;
			outputFile = pOutputFile;
		}
		/** Returns the input file.
		 * @return The input file.
		 */
		public File getInputFile() {
			return inputFile;
		}
		/** Returns the output file.
		 * @return The output file.
		 */
		public File getOutputFile() {
			return outputFile;
		}
	}
	/**
	 * Returns the URL of a resource file.
	 * @param pResource The resource file's URI.
	 * @return the URL of the requested resource file, which has been located using
	 *   the current threads context class loader.
	 */
	public static URL requireResource(String pResource) {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return requireResource(cl, pResource);
	}

	/**
	 * Asserts, that the given object is not null.
	 * @param pMessage The error message, which is being used, if the test fails.
	 * @param pObject The object, that is being tested.
	 */
	public static void assertNotNull(String pMessage, Object pObject) {
		if (pObject == null) {
			fail(pMessage);
		}
	}
	
	/**
	 * Returns the URL of a resource file.
	 * @param pCl The class loader, which is being used to locate the resource file.
	 * @param pResource The resource file's URI.
	 * @return the URL of the requested resource file, which has been located using
	 *   the given context class loader.
	 */
	public static final URL requireResource(ClassLoader pCl, String pResource) {
		final URL url = pCl.getResource(pResource);
		assertNotNull("Failed to locate resource: " + pResource + " (via ClassLoader " + pCl + ")", url);
		return url;
	}

	/**
	 * Returns a File object, which represents a resource file.
	 * @param pResource The resource file's URI.
	 * @return A File object, which represents the requested resource file, which has been located using
	 *   the current threads context class loader.
	 */
	public static File requireFileResource(String pResource) {
		final URL url = requireResource(pResource);
		return assertFileResource(url);
	}

	/**
	 * Returns a File object, which represents a resource file.
	 * @param pCl The class loader, which is being used to locate the resource file.
	 * @param pResource The resource file's URI.
	 * @return A File object, which represents the requested resource file, which has been located using
	 *   the given context class loader.
	 */
	public static File requireFileResource(ClassLoader pCl, String pResource) {
		final URL url = requireResource(pCl, pResource);
		return assertFileResource(url);
	}

	/**
	 * Asserts, that the given numbers are equal.
	 * @param pMessage The error message, which is being used, if the test fails.
	 * @param pExpect The expected integer value.
	 * @param pGot The actual integer value.
	 */
	public static void assertEquals(String pMessage, int pExpect, int pGot) {
		if (pExpect != pGot) {
			fail(pMessage);
		}
	}

	/**
	 * Asserts, that the given objects are equal.
	 * @param pMessage The error message, which is being used, if the test fails.
	 * @param pExpect The expected object.
	 * @param pGot The actual object.
	 */
	public static void assertEquals(String pMessage, Object pExpect, Object pGot) {
		if (!pExpect.equals(pGot)) {
			fail(pMessage);
		}
	}

	/**
	 * Asserts, that the byte arrays are equal.
	 * @param pExpect The expected byte array.
	 * @param pGot The actual byte array.
	 */
	public static void assertEquals(byte[] pExpect, byte[] pGot) {
		assertEquals(pExpect.length, pGot.length);
		for (int i = 0;  i < pExpect.length;  i++) {
			assertEquals(pExpect[i], pGot[i]);
		}
	}

	/**
	 * Asserts, that the given objects are the same objects.
	 * @param pExpect The expected object.
	 * @param pGot The actual object.
	 */
	public static void assertSame(Object pExpect, Object pGot) {
		if (pExpect != pGot) {
			fail("Assertion failed, expected " + pExpect + ", got " + pGot);
		}
	}

	/**
	 * Asserts, that the given objects are equal.
	 * @param pExpect The expected object.
	 * @param pGot The actual object.
	 */
	public static void assertEquals(Object pExpect, Object pGot) {
		if (!pExpect.equals(pGot)) {
			fail("Assertion failed, expected " + pExpect + ", got " + pGot);
		}
	}

	/**
	 * Asserts, that the given numbers are equal.
	 * @param pExpect The expected integer value.
	 * @param pGot The actual integer value.
	 */
	public static void assertEquals(int pExpect, int pGot) {
		if (pExpect != pGot) {
			fail("Assertion failed, expected " + pExpect + ", got " + pGot);
		}
	}

	/**
	 * Asserts, that the given URL represents a file resource.
	 * @param pUrl The URL, that is being checked.
	 * @return A File object, which represents the same resource.
	 */
	public static File assertFileResource(URL pUrl) {
		assertEquals("file", pUrl.getProtocol());
		return new File(pUrl.getFile());
	}

	/** Asserts, that the given value is true.
	 * @param pMessage An error message, that is being used, if the test fails.
	 * @param pValue The value, that is being tested.
	 */
	public static void assertTrue(String pMessage, boolean pValue) {
		if (!pValue) {
			fail(pMessage);
		}
	}

	/** Asserts, that the given value is true.
	 * @param pValue The value, that is being tested.
	 */
	public static void assertTrue(boolean pValue) {
		if (!pValue) {
		    fail("Assertion failed, expected true, got false");
		}
	}

	/**
	 * Called to indicate, that a test has failed.
	 * @param pMessage The error message
	 * @throws IllegalStateException Thrown as an indication of the failed test.
	 */
	public static void fail(String pMessage) {
		throw new IllegalStateException(pMessage);
	}
	
	/**
	 * Called to create a test directory.
	 * @param pTestClass The test class, for which a test directory is being created.
	 * @return Path of the created test directory. Uses the test classes unqualified
	 *   name.
	 */
	public static Path requireTestDirectory(Class<?> pTestClass) {
		return requireTestDirectory(pTestClass.getSimpleName());
	}

	/**
	 * Called to create a test directory.
	 * @param pTestName Name of the test directory.
	 * @return Path of the created test directory.
	 */
	public static Path requireTestDirectory(String pTestName) {
		final Path junitWorkDir = requireTestDirectory().toPath();
		final Path testDir = junitWorkDir.resolve(pTestName);
		Functions.run(() -> Files.createDirectories(testDir));
		return testDir;
	}

	/** Called to create a test directory by copying it from the given
	 * source directory.
	 * @param pTestClass The test class, for which a test directory is being created.
	 * @param pSourceDir The source directory, that is being copied.
	 * @return Path of the created test directory. Uses the test classes unqualified
	 *   name.
	 */
	public static Path setupTestDirectory(Class<?> pTestClass, Path pSourceDir) {
		final Path testDir = requireTestDirectory(pTestClass);
		com.github.jochenw.afw.core.util.FileUtils.removeDirectory(testDir);
		com.github.jochenw.afw.core.util.FileUtils.copyDirectory(pSourceDir, testDir);
		return testDir;
	}

	/**
	 * Returns the test directories parent directory ("target/junit-work")
	 * @return The test directories parent directory ("target/junit-work")
	 */
	public static File requireTestDirectory() {
		final File targetDir = new File("target");
		assertTrue(targetDir.isDirectory());
		final File testDir = new File(targetDir, "junit-work");
		if (!testDir.isDirectory()  &&   !testDir.mkdir()) {
			fail("Unable to create directory: " + testDir.getAbsolutePath());
		}
		return testDir;
	}

	/**
	 * Copies the given source file to the given target file.
	 * @param pSource The source file, that is being copied.
	 * @param pTarget The target file, that is being created by copying the source file.
	 * @throws IOException The copy operation has failed.
	 */
	public static void copy(File pSource, File pTarget) throws IOException {
		try (InputStream in = new FileInputStream(pSource);
			 OutputStream out = new FileOutputStream(pTarget)) {
			Streams.copy(in, out);
		}
	}

	/**
	 * Creates a test file pair by copying 
	 * @param pUri The URI of a resource file, that is being copied.
	 * @param pInputSuffix Suffix of the input file, that is being replaced to create the output file name.
	 * @param pOutputSuffix Suffix of the output file, that replaces the input file suffix in the output
	 *   files name.
	 * @return The created {@link TestFilePair}.
	 * @throws IOException Creating the files failed.
	 */
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

	/**
	 * Asserts, that the given arrays are equal
	 * @param pGot The actual array.
	 * @param pExpect The expected array.
	 */
	public static void assertArrayEquals(Object[] pGot, Object... pExpect) {
		for (int i = 0;  i < pGot.length;  i++) {
			assertEquals(String.valueOf(i), pExpect[i], pGot[i]);
		}
		assertEquals(pExpect.length, pGot.length);
	}

	/**
	 * Asserts, that the given maps are equal
	 * @param pGot The actual map.
	 * @param pExpect The expected map, as a sequence of key/value pairs.
	 */
	public static void assertMapEquals(Map<String, String> pGot, String... pExpect) {
		for (int i = 0;  i < pExpect.length;  i += 2) {
			final String key = pExpect[i];
			final String expect = pExpect[i+1];
			final String got = pGot.get(key);
			assertEquals(i + ": " + key, expect, got);
		}
		assertEquals(pExpect.length/2, pGot.size());
	}

	/**
	 * Asserts, that the given files have the same content.
	 * @param pExpect A {@link Path file}, which provides the expected content.
	 * @param pGot A {@link Path file}, which provides the actual content.
	 */
	public static void assertSameContent(Path pExpect, Path pGot) {
		try (InputStream is1 = Files.newInputStream(pExpect);
			 BufferedInputStream bis1 = new BufferedInputStream(is1);
		     InputStream is2 = Files.newInputStream(pGot);
			 BufferedInputStream bis2 = new BufferedInputStream(is2)) {
			assertSameContent(bis1, bis2);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	/**
	 * Asserts, that the given streams have the same content.
	 * @param pExpect An {@link InputStream}, which provides the expected content.
	 * @param pGot An {@link InputStream}, which provides the actual content.
	 * @throws IOException Reading either of the content streams failed.
	 */
	public static void assertSameContent(InputStream pExpect, InputStream pGot)
			throws IOException {
		long offset = 0;
		for (;;) {
			int b1 = pExpect.read();
			int b2 = pGot.read();
			assertEquals(String.valueOf(offset), b1, b2);
			if (b1 == -1) {
				break;
			}
		}
	}

	/**
	 * Asserts, that the given files, and the given stream, have the same content.
	 * @param pExpect A {@link Path file}, which provides the expected content.
	 * @param pGot An {@link InputStream}, which provides the actual content.
	 */
	public static void assertSameContent(Path pExpect, InputStream pGot) {
		try (InputStream is = Files.newInputStream(pExpect);
			 BufferedInputStream bis = new BufferedInputStream(is)) {
			assertSameContent(bis, pGot);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	
	/** Asserts, that executing the given {@link Runnable} will cause an
	 * Exception of the given type with the given message.
	 * @param <O> Type of the expected Exception
	 * @param pType Type of the expected Exception
	 * @param pMsg Message of the expected Exception
	 * @param pRunnable The {@link Runnable}, that is being executed to trigger the exception. 
	 */
	public static <O extends Throwable> void assertThrows(Class<O> pType, String pMsg, Runnable pRunnable) {
		Throwable th;
		try {
			pRunnable.run();
			throw new IllegalStateException("Expected Exception");
		} catch (UncheckedIOException e) {
			th = e.getCause();
		} catch (UndeclaredThrowableException e) {
			th = e.getCause();
		} catch (Throwable e) {
			th = e;
		}
		assertSame(pType, th.getClass());
		assertEquals(pMsg, th.getMessage());
	}
}
