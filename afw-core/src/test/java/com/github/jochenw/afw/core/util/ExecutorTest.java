package com.github.jochenw.afw.core.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.github.jochenw.afw.core.util.Executor.Listener;

/** Test case for {@link Executor}.
 */
public class ExecutorTest {
	/** Test case for {@link Executor#run(java.nio.file.Path, String[], String[], Consumer, Consumer, java.util.function.IntConsumer)}
	 * on Windows.
	 */
	@Test
	public void testEchoCmdToStdoutWithoutListenerOnWindows() {
		Assume.assumeTrue(Systems.isWindows());
		runTestWithoutListener("Okay\r\n", "", "cmd", "/c", "echo", "Okay");
	}

	protected void runTestWithoutListener(String pExpectedStdOut, String pExpectedStdErr, String... pCmd) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ByteArrayOutputStream baes = new ByteArrayOutputStream();
		final int status = new Executor().run(null, pCmd, null,
				                              (in) -> { Streams.copy(in, baos);},
				                              (in) -> { Streams.copy(in, baes);}, null);
		Assert.assertEquals(0, status);
		Assert.assertEquals(pExpectedStdOut, baos.toString());
		Assert.assertEquals(pExpectedStdErr, baes.toString());
	}

	/** Test case for {@link Executor#run(java.nio.file.Path, String[], String[], Consumer, Consumer, java.util.function.IntConsumer)}
	 * on Non-Windows.
	 */
	@Test
	public void testEchoCmdToStdoutWithoutListenerOnNonWindows() {
		Assume.assumeFalse(Systems.isWindows());
		runTestWithoutListener("Okay\n", "", "sh", "-c", "echo \"Okay\"");
		runTestWithoutListener("Okay", "", "sh", "-c", "echo -n \"Okay\"");
	}

	/** Test case for {@link Executor#run(java.nio.file.Path, String[], String[], Listener)}
	 * on Windows.
	 */
	@Test
	public void testEchoCmdToStdoutWithListenerOnWindows() {
		Assume.assumeTrue(Systems.isWindows());
		runTestWithListener("Okay\r\n", "", "cmd", "/c", "echo", "Okay");
	}

	protected void runTestWithListener(String pExpectedStdOut, String pExpectedStdErr, String... pCmd) {
		final MutableBoolean statusSeen = new MutableBoolean();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ByteArrayOutputStream baes = new ByteArrayOutputStream();
		final Listener listener = new Listener() {
			@Override
			public Consumer<InputStream> getStdOutputConsumer() {
				return (in) -> { Streams.copy(in, baos);};
			}

			@Override
			public Consumer<InputStream> getErrorOutputConsumer() {
				return (in) -> { Streams.copy(in, baes);};
			}

			@Override
			public void accept(int pStatus) {
				statusSeen.set();
				Assert.assertEquals(0, pStatus);
			}
		};
		final int status = new Executor().run(null, pCmd, null, listener);
		Assert.assertEquals(0, status);
		Assert.assertTrue(statusSeen.isSet());
		Assert.assertEquals(pExpectedStdOut, baos.toString());
		Assert.assertEquals(pExpectedStdErr, baes.toString());
	}

	/** Test case for {@link Executor#run(java.nio.file.Path, String[], String[], Listener)}
	 * on Windows.
	 */
	@Test
	public void testEchoCmdToStdoutWithListenerOnNonWindows() {
		Assume.assumeFalse(Systems.isWindows());
		runTestWithListener("Okay\n", "", "sh", "-c", "echo 'Okay'");
		runTestWithListener("Okay", "", "sh", "-c", "echo -n 'Okay'");
		// Disabled, doesn't really work on Linux.
		//runTestWithListener("", "Okay", "sh", "-c", "echo 1>&2 -n \"Okay\"");
	}
}
