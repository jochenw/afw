package com.github.jochenw.afw.core.cli;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.Test;

import com.github.jochenw.afw.core.cli.Cli.Context;
import com.github.jochenw.afw.core.cli.Cli.UsageException;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.util.MutableBoolean;


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
		final OptionsBean options = Cli.of(new OptionsBean())
			.pathOption("inputFile", "if").existsRequired().fileRequired().required()
				.handler((c,p) -> c.getBean().inputFile = p).end()
			.pathOption("outputFile", "of").required()
				.handler((c,p) -> c.getBean().outputFile = p).end()
			.pathOption("helperFile", "hf")
			    .handler((c,p) -> c.getBean().helperFile = p).end()
			.boolOption("verbose", "v")
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
			Cli.of(new OptionsBean())
				.pathOption("inputFile").end()
				.stringOption("inputFile");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Duplicate option name: inputFile", e.getMessage());
		}

		try {
			Cli.of(new OptionsBean())
				.pathOption("inputFile", "if").end()
				.stringOption("outputFile").end()
				.stringOption("if");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Duplicate option name: if", e.getMessage());
		}

		try {
			Cli.of(new OptionsBean())
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
			Cli.of(new OptionsBean())
					.pathOption("inputFile", "if").existsRequired().fileRequired().required()
					.handler((c,p) -> c.getBean().inputFile = p).end()
					.pathOption("outputFile", "of").required()
					.handler((c,p) -> c.getBean().outputFile = p).end()
					.pathOption("helperFile", "hf")
					.handler((c,p) -> c.getBean().helperFile = p).end()
					.boolOption("verbose", "v")
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
		final OptionsBean options = Cli.of(new OptionsBean())
				.pathOption("inputFile", "if").existsRequired().fileRequired().required()
					.handler((c,p) -> c.getBean().inputFile = p).end()
				.pathOption("outputFile", "of").required()
					.handler((c,p) -> c.getBean().outputFile = p).end()
				.pathOption("helperFile", "hf")
				    .handler((c,p) -> c.getBean().helperFile = p).end()
				.boolOption("verbose", "v")
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
		final OptionsBean options = Cli.of(new OptionsBean())
				.pathOption("inputFile", "if").existsRequired().fileRequired().required()
					.handler((c,p) -> c.getBean().inputFile = p).end()
				.pathOption("outputFile", "of").required()
					.handler((c,p) -> c.getBean().outputFile = p).end()
				.pathOption("helperFile", "hf")
				    .handler((c,p) -> c.getBean().helperFile = p).end()
				.boolOption("verbose", "v")
				    .handler((c,b) -> c.getBean().verbose = b.booleanValue()).end()
				.validator((b) -> {validated.set(); return null;})
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
	public void testUsageHandler() {
		@NonNull Function<@Nullable UsageException,@NonNull RuntimeException> usageHandler =
				(ue) -> new IllegalStateException(ue == null ? null : ue.getMessage());
		final Cli<OptionsBean> cli = Cli.of(new OptionsBean())
				.stringOption("inputFile", "if").required().handler((c,s) -> c.getBean().inputFile = Paths.get(s)).end()
				.stringOption("outputFile", "of").required().handler((c,s) -> c.getBean().outputFile = Paths.get(s)).end()
				.usageHandler(usageHandler);
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
				.usageHandler((s) -> new IllegalStateException(s));
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

	private static class DnfOptions {
		private String command;
		// For command = "check"
		private boolean dependencies, duplicates;
		// For command = "search"
		private boolean all;
		private List<String> keywords = new ArrayList<>();
	}

	@Test
	public void testActions() {
		final DnfOptions checkOptions = testActions("check", "--duplicates");
		assertNotNull(checkOptions);
		assertEquals("check", checkOptions.command);
		assertTrue(checkOptions.duplicates);
		assertFalse(checkOptions.dependencies);
		assertFalse(checkOptions.all);
		assertTrue(checkOptions.keywords.isEmpty());
		final DnfOptions searchOptions = testActions("search", "--all", "kernel");
		assertNotNull(searchOptions);
		
	}

	/** Parses the argument string into an instance of {@link DnfOptions}.
	 * @param pArgs The argument string
	 * @return The configured instance of {@link DnfOptions}.
	 */
	protected DnfOptions testActions(String... pArgs) {
		final DnfOptions opts = new DnfOptions();
		final FailableBiConsumer<Context<DnfOptions>,Boolean,?> depsHandler =
				(ct,b) -> opts.dependencies = b.booleanValue();
		final FailableBiConsumer<Context<DnfOptions>,Boolean,?> dupsHandler =
				(ct,b) -> opts.duplicates = b.booleanValue();
		final FailableBiConsumer<Context<DnfOptions>,Boolean,?> allHandler =
				(ct,b) -> opts.all = b.booleanValue();
		return Cli.of(opts)
		   .extraArgsHandler((cli, cmd) -> {
			   if (opts.command == null) {
				   switch (cmd) {
				   case "check":
					   cli.boolOption("dependencies", "deps").handler(depsHandler).end();
					   cli.boolOption("duplicates", "dups").handler(dupsHandler).end();
					   break;
				   case "search":
					   cli.boolOption("all", "a").handler(allHandler).end();
					   cli.extraArgsHandler((ct,s) -> opts.keywords.add(s));
					   break;
				   default:
					   throw cli.error("Invalid command: Expected check|search, got " + cmd);
				   }
				   opts.command = cmd;
			   } else {
				   throw cli.error("Command may be given only once.");
			   }
		   }).parse(pArgs);
	}

	/** Options class for {@link #testEnumOptions()}.
	 */
	private static class LoggingOptions {
		@SuppressWarnings("unused")
		private String logFile;
		private Level logLevel;
		public LoggingOptions() {}
	}

	/** Test case for {@link Enum enum} options.
	 */
	@Test
	public void testEnumOptions() {
		final Function<String[],LoggingOptions> optParser = (args) -> {
			return Cli.of(new LoggingOptions())
					.stringOption("logFile", "lf").handler((c,s) -> c.getBean().logFile = s).end()
					.enumOption(Level.class, "logLevel", "ll").caseInsensitive().handler((c,l) -> c.getBean().logLevel = l).end()
					.parse(args);
		};
		final BiConsumer<Level,String[]> tester = (level, array) -> {
			final LoggingOptions opts = optParser.apply(array);
			if (level == null) {
				assertNull(opts.logLevel);
			} else {
				assertEquals(level, opts.logLevel);
			}
		};
		tester.accept(null, new String[] {});
		tester.accept(Level.INFO, new String[] {"-logLevel", "INFO"});
		tester.accept(Level.WARN, new String[] {"-logLevel", "WARN"});
		try {
			tester.accept(Level.WARN, new String[] {"-logLevel", "warn"});
		} catch (UsageException ue) {
			assertEquals("Invalid value for option logLevel: warn", ue.getMessage());
		}
	}
}
