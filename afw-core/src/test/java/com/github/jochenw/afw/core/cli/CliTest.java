package com.github.jochenw.afw.core.cli;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.cli.Cli.Context;
import com.github.jochenw.afw.core.cli.Cli.UsageException;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.util.MutableBoolean;
import com.github.jochenw.afw.core.util.Objects;


/** Test suite for the {@link Cli} class.
 */
public class CliTest {
	/** Example of an options bean.
	 */
	public static class OptionsBean {
		private Path inputFile, outputFile, helperFile;
		private boolean verbose;
	}

	/**
	 * A simple test case for {@link Cli#parse(String[])},
	 * with no problems.
	 */
	@Test
	public void testSimpleCase() {
		final OptionsBean options = new Cli<>(new OptionsBean())
			.pathOption("inputFile", "if").existsRequired().fileRequired().required()
				.handler((c,p) -> c.getBean().inputFile = p).end()
			.pathOption("outputFile", "of").required()
				.handler((c,p) -> c.getBean().outputFile = p).end()
			.pathOption("helperFile", "hf")
			    .handler((c,p) -> c.getBean().helperFile = p).end()
			.booleanOption("verbose", "v")
			    .handler((c,b) -> c.getBean().verbose = b.booleanValue()).end()
			.parse(new @NonNull String[]{"--inputFile", "pom.xml", "-of", "target/test/output.xml", "-verbose"});
		assertNotNull(options);
		assertEquals(options.inputFile.toString(), "pom.xml");
		assertEquals(options.outputFile.toString().replace('\\', '/'), "target/test/output.xml");
		assertNull(options.helperFile);
		assertTrue(options.verbose);
	}

	/**
	 * Test case for duplicate option names.
	 */
	@Test
	public void testDuplicateName() {
		try {
			new Cli<>(new OptionsBean())
				.pathOption("inputFile").end()
				.stringOption("inputFile");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Duplicate option name: inputFile", e.getMessage());
		}

		try {
			new Cli<>(new OptionsBean())
				.pathOption("inputFile", "if").end()
				.stringOption("outputFile").end()
				.stringOption("if");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Duplicate option name: if", e.getMessage());
		}

		try {
			new Cli<>(new OptionsBean())
				.pathOption("inputFile").end()
				.stringOption("if", "inputFile").end();
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Duplicate option name: inputFile", e.getMessage());
		}
	}

	/** Test case for a missing required option.
	 */
	@Test
	public void testRequiredOptionMissing() {
		try {
			new Cli<>(new OptionsBean())
					.pathOption("inputFile", "if").existsRequired().fileRequired().required()
					.handler((c,p) -> c.getBean().inputFile = p).end()
					.pathOption("outputFile", "of").required()
					.handler((c,p) -> c.getBean().outputFile = p).end()
					.pathOption("helperFile", "hf")
					.handler((c,p) -> c.getBean().helperFile = p).end()
					.booleanOption("verbose", "v")
					.handler((c,b) -> c.getBean().verbose = b.booleanValue()).end()
					.parse(new @NonNull String[]{"-of", "target/test/output.xml", "-verbose"});
			fail("Expected Exception");
		} catch (UsageException e) {
			assertEquals("Required option missing: inputFile", e.getMessage());
		}
	}

	/** Test case for the --option=value format.
	 */
	@Test
	public void testValueInOption() {
		final OptionsBean options = new Cli<>(new OptionsBean())
				.pathOption("inputFile", "if").existsRequired().fileRequired().required()
					.handler((c,p) -> c.getBean().inputFile = p).end()
				.pathOption("outputFile", "of").required()
					.handler((c,p) -> c.getBean().outputFile = p).end()
				.pathOption("helperFile", "hf")
				    .handler((c,p) -> c.getBean().helperFile = p).end()
				.booleanOption("verbose", "v")
				    .handler((c,b) -> c.getBean().verbose = b.booleanValue()).end()
				.parse(new @NonNull String[]{"--inputFile=pom.xml", "-of", "target/test/output.xml", "-verbose=true"});
			assertNotNull(options);
			assertEquals(options.inputFile.toString(), "pom.xml");
			assertEquals(options.outputFile.toString().replace('\\', '/'), "target/test/output.xml");
			assertNull(options.helperFile);
			assertTrue(options.verbose);
	}

	/** Test case for a bean validator.
	 */
	@Test
	public void testBeanValidator() {
		final MutableBoolean validated = new MutableBoolean();
		final OptionsBean options = new Cli<>(new OptionsBean())
				.pathOption("inputFile", "if").existsRequired().fileRequired().required()
					.handler((c,p) -> c.getBean().inputFile = p).end()
				.pathOption("outputFile", "of").required()
					.handler((c,p) -> c.getBean().outputFile = p).end()
				.pathOption("helperFile", "hf")
				    .handler((c,p) -> c.getBean().helperFile = p).end()
				.booleanOption("verbose", "v")
				    .handler((c,b) -> c.getBean().verbose = b.booleanValue()).end()
				.beanValidator((b) -> {validated.set(); return null;})
				.parse(new @NonNull String[]{"--inputFile=pom.xml", "-of", "target/test/output.xml", "-verbose=true"});
			assertNotNull(options);
			assertEquals(options.inputFile.toString(), "pom.xml");
			assertEquals(options.outputFile.toString().replace('\\', '/'), "target/test/output.xml");
			assertNull(options.helperFile);
			assertTrue(options.verbose);
	}

	/** Test case for the error handler.
	 */
	@Test
	public void testErrorHandler() {
		final Cli<OptionsBean> cli = Cli.of(new OptionsBean())
				.stringOption("inputFile", "if").required().handler((c,s) -> c.getBean().inputFile = Paths.get(s)).end()
				.stringOption("outputFile", "of").required().handler((c,s) -> c.getBean().outputFile = Paths.get(s)).end()
				.errorHandler((s) -> new IllegalStateException(s));
		try {
			cli.parse(new @NonNull String[]{ "-if=pom.xml", "-of=/var/lib/of.log", "-h"});
		} catch (IllegalStateException e) {
			assertNull(e.getMessage());
		}
	}

    /** Test case for linux paths as arguments.
     */
	@Test
	public void testLinuxPaths() {
		final FailableBiConsumer<Context<OptionsBean>, String, ?> ofHandler = (c,s) -> {
			c.getBean().outputFile = Paths.get(s);
		};
		@SuppressWarnings("null")
		final @NonNull Cli<@NonNull OptionsBean> cli = Cli.of(new OptionsBean())
				.stringOption("inputFile", "if")
				    .required()
				    .handler((c,s) -> c.getBean().inputFile = Paths.get(s)).end()
				.stringOption("outputFile", "of")
				    .required()
				    .handler(ofHandler).end()
				.errorHandler((s) -> new IllegalStateException(s));
		final @NonNull String @NonNull [] args1 = new @NonNull String[] {
			"-if=pom.xml", "-of=/var/lib/of.log"
		};
		final OptionsBean ob1 = cli.parse(args1);
		assertNotNull(ob1);
		assertEquals("pom.xml", ob1.inputFile.toString());
		assertEquals("/var/lib/of.log", ob1.outputFile.toString().replace('\\', '/'));
		final @NonNull String[] args2 = new @NonNull String[] {
			"-if", "pom.xml", "-of", "/var/lib/of.log"
		};
		final OptionsBean ob2 = cli.parse(args2);
		assertNotNull(ob2);
		assertEquals("pom.xml", ob2.inputFile.toString());
		assertEquals("/var/lib/of.log", ob2.outputFile.toString().replace('\\', '/'));
		
	}

	/** Test for implementing a {@link Cli.CliClass}.
	 */
	@Test
	public void testCliClass() {
		final @NonNull FailableBiConsumer<@NonNull Context<@NonNull OptionsBean>, String, ?> ofHandler = (c,s) -> {
			c.getBean().outputFile = Paths.get(s);
		};
		final MutableBoolean configured = new MutableBoolean();
		final Function<String,RuntimeException> errorHandler = (s) -> new IllegalStateException(s);
		assertNull(TestCliClass.options);
		Cli.main(TestCliClass.class, OptionsBean.class, new @NonNull String[] {
			"-if", "pom.xml", "-of", "./logs/of.log"
		}, (cli) -> {
			configured.set();
			cli.stringOption("inputFile", "if").required().handler((c,s) -> c.getBean().inputFile = Paths.get(s)).end()
			   .stringOption("outputFile", "of").required().handler(ofHandler).end();
		}, errorHandler);
		assertTrue(configured.isSet());
		final OptionsBean ob = TestCliClass.options; 
		assertNotNull(ob);
		assertEquals("pom.xml", ob.inputFile.toString());
		assertEquals("./logs/of.log", ob.outputFile.toString().replace('\\', '/'));
	}

	/** Test class for the {@link Cli.CliClass}.
	 */
	public static class TestCliClass implements Cli.CliClass<OptionsBean> {
		private static OptionsBean options;

		/** Runs the Cli with the given options bean.
		 */
		@Override
		public void run(OptionsBean pOptionsBean) {
			options = pOptionsBean;
		}

		/**
		 * @return The optionś bean.
		 */
		public OptionsBean getOptions() {
			return Objects.requireNonNull(options);
		}
	}

}
