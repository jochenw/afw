/**
 * 
 */
package com.github.jochenw.afw.core.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

import com.github.jochenw.afw.core.cli.Args.Context;
import com.github.jochenw.afw.core.cli.Args.Listener;

/** Test case for the {@link Args command line argument parser}.
 */
public class ArgsTest {
	private Listener newListener(List<String> pArgs) {
		return new Listener() {
			@Override
			public void option(Context pCtx, String pName) {
				if (pCtx.isValueAvailable()) {
					pArgs.add(pName);
					pArgs.add(Objects.requireNonNull(pCtx.getValue()));
				} else {
					assertEquals(pName, pCtx.getName());
					pArgs.add(pName);
					pArgs.add(null);
				}
			}
		};
	}

	/**
	 * Test case for simple command line handling.
	 */
	@Test
	public void testSimpleCase() {
		final List<String> list = new ArrayList<>();
		assertNull(Args.parse(newListener(list), "--file", "foo.txt", "-otherFile", "bar.txt", "--anotherFile=baz.log", "--noChecks"));
		assertResult(list, "file", "foo.txt", "otherFile", "bar.txt", "anotherFile", "baz.log", "noChecks", null);
	}

	/**
	 * Test case for an option, that needs a value, but none is specified.
	 */
	@Test
	public void testMissingValue() {
		final List<String> list = new ArrayList<>();
		final Listener listener1 = newListener(list);
		final Listener listener2 = new Listener() {
			@Override
			public void option(Context pCtx, String pName) {
				if ("anotherFile".equals(pName)) {
					final String value = pCtx.getValue();
					assertEquals("baz.log", value);
				} else if ("noChecks".equals(pName)) {
					try {
						pCtx.getValue();
					} catch (ArgsException e) {
						assertEquals("Option noChecks requires an argument.", e.getMessage());
					}
				} else {
					listener1.option(pCtx, pName);
				}
			}
			
		};
		assertNull(Args.parse(listener2, "--file", "foo.txt", "-otherFile", "bar.txt", "--anotherFile=baz.log", "--noChecks"));
		assertResult(list, "file", "foo.txt", "otherFile", "bar.txt");
	}
	
	private void assertResult(List<String> pList, String... pValues) {
		assertEquals(pList.size(), pValues.length);
		for (int i = 0;  i < pList.size();  i++) {
			assertEquals(pList.get(i), pValues[i]);
		}
	}

	/**
	 * Test for an invalid option.
	 */
	@Test
	public void testIllegalArgument() {
		final List<String> list = new ArrayList<String>();
		try {
			Args.parse(newListener(list), "--file", "foo.txt", "otherFile");
			fail("Expected Exception");
		} catch (ArgsException e) {
			assertEquals("Invalid argument: otherFile", e.getMessage());
		}
	}


	/** Test for extra arguments.
	 */
	@Test
	public void testExtraArguments() {
		final List<String> list = new ArrayList<String>();
		final String[] extraArgs = Args.parse(newListener(list), "--file", "foo.txt", "--", "a", "b", "c");
		assertNotNull(extraArgs);
		assertResult(Arrays.asList(extraArgs), "a", "b", "c");
		assertResult(list, "file", "foo.txt");
	}

	/** Test for error handler returning null.
	 */
	@Test
	public void testErrorHandlerReturningNull() {
		final Listener listener = new Listener() {
			@Override
			public void option(Context pCtx, String pName) {
				// Does nothing.
			}

			@Override
			public RuntimeException error(String pMsg) {
				return null;
			}
		};
		try {
			Args.parse(listener, "--file", "foo.txt", "otherFile");
			fail("Expected Exception");
		} catch (ArgsException e) {
			assertEquals("Invalid argument: foo.txt", e.getMessage());
		}

		final Listener listener2 = new Listener() {
			@Override
			public void option(Context pCtx, String pName) {
				if (pCtx.isValueAvailable()) {
					pCtx.getValue();
				}
			}

			@Override
			public RuntimeException error(String pMsg) {
				return null;
			}
		};
		Args.parse(listener2, "--file");

	}
}
