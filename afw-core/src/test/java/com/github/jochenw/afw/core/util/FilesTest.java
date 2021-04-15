package com.github.jochenw.afw.core.util;

import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;


/** Test for {@link Files}.
 */
public class FilesTest {
	/** Test case for {@link Files#resolve(String)} on Linux.
	 */
	@Test
	public void testRelativizeOnLinux() {
		Assume.assumeTrue("Not running on Linux/Unix", Systems.isLinuxOrUnix());
		final String instanceDir = "/f/SoftwareAG/webMethods99/IntegrationServer/instances/default/";
		final Path logFileDir = java.nio.file.Paths.get(instanceDir).resolve("logs");
		String lclBaseLogUri = "lidl/lcl/base.log";
		Path lidlLclBaseLog1 = Files.resolve(logFileDir, lclBaseLogUri);
		Assert.assertEquals(instanceDir + "logs/" + lclBaseLogUri, lidlLclBaseLog1.toString());
		Path lidlLclBaseLog2 = Files.resolve(logFileDir, instanceDir + "logs/" + lclBaseLogUri);
		Assert.assertTrue((instanceDir + "logs/" + lclBaseLogUri).equals(lidlLclBaseLog2.toString())
				          ||  lclBaseLogUri.equals(lidlLclBaseLog2.toString()));
		try {
			final String invalidPath = instanceDir.replace("webMethods99", "webMethods103");
			final Path p = Files.resolve(logFileDir, invalidPath + lclBaseLogUri);
			Assert.fail("Expected Exception, got " + p);
		} catch (IllegalArgumentException e) {
		}
	}

	/** Test case for {@link Files#resolve(String)} on Windows.
	 */
	@Test
	public void testRelativizeOnWindows() {
		Assume.assumeTrue("Not running on Windows", Systems.isWindows());
		final String instanceDir = "F:/SoftwareAG/webMethods99/IntegrationServer/instances/default/";
		final Path logFileDir = java.nio.file.Paths.get(instanceDir).resolve("logs");
		String lclBaseLogUri = "lidl/lcl/base.log";
		Path lidlLclBaseLog1 = Files.resolve(logFileDir, lclBaseLogUri);
		assertEquals(instanceDir + "logs/" + lclBaseLogUri, lidlLclBaseLog1.toString());
		Path lidlLclBaseLog2 = Files.resolve(logFileDir, logFileDir.toString() + "/" + lclBaseLogUri);
		assertEquals(instanceDir + "logs/" + lclBaseLogUri, lidlLclBaseLog2.toString());
		try {
			final String invalidPath = instanceDir.replace("webMethods99", "webMethods103");
			Files.resolve(logFileDir, invalidPath + lclBaseLogUri);
			Assert.fail("Expected Exception");
		} catch (IllegalArgumentException e) {
		}
		try {
			final String invalidPath = instanceDir.replace("webMethods99", "webMethods103").replace('/', '\\');
			Files.resolve(logFileDir, invalidPath + lclBaseLogUri);
			Assert.fail("Expected Exception");
		} catch (IllegalArgumentException e) {
		}
	}

	protected void assertEquals(String pExpectedPath, String pActualPath) {
		final String validPath1 = pExpectedPath;
		final String validPath2 = pExpectedPath.replace('\\', '/');
		if (!validPath1.equals(pActualPath)  &&  !validPath2.equals(pActualPath)) {
			if (pExpectedPath.equals(validPath2)) {
				return; // Also okay
			} else {
			    Assert.fail("Expected " + validPath1 + ", or " + validPath2 + ", got " + pActualPath);
		    }
		}
	}
}