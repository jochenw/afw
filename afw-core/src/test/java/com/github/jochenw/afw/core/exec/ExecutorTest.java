package com.github.jochenw.afw.core.exec;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.github.jochenw.afw.core.util.Systems;

public class ExecutorTest {
	/** Test case for {@link Executor#run(java.nio.file.Path, String[], String[], Consumer, Consumer, java.util.function.IntConsumer)}
	 * on Windows.
	 */
	@Test
	public void testEchoCmdToStdoutOnWindows() {
		Assume.assumeTrue(Systems.isWindows());
		runTest("Okay\r\n", "", "cmd", "/c", "echo", "Okay");
	}

	protected void runTest(String pExpectedStdOut, String pExpectedStdErr, String... pCmd) {
		final List<String> cmdLine = new ArrayList<>(Arrays.asList(pCmd));
		final String cmd = cmdLine.remove(0);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ByteArrayOutputStream baes = new ByteArrayOutputStream();
		final int status = Executor.builder()
				.stdOut(baos)
				.stdErr(baes)
				.exec(cmd)
				.args(cmdLine)
				.build().run();
		Assert.assertEquals(0, status);
		Assert.assertEquals(pExpectedStdOut, baos.toString());
		Assert.assertEquals(pExpectedStdErr, baes.toString());
	}

	/** Test case for {@link Executor#run(java.nio.file.Path, String[], String[], Consumer, Consumer, java.util.function.IntConsumer)}
	 * on Non-Windows.
	 */
	@Test
	public void testEchoCmdToStdoutOnNonWindows() {
		Assume.assumeFalse(Systems.isWindows());
		runTest("Okay\n", "", "sh", "-c", "echo \"Okay\"");
		runTest("Okay", "", "sh", "-c", "echo -n \"Okay\"");
	}
}
