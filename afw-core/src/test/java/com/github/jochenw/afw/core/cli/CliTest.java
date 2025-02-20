package com.github.jochenw.afw.core.cli;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.Test;

import com.github.jochenw.afw.core.cli.Cli.Context;
import com.github.jochenw.afw.core.cli.Cli.UsageException;
import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.MutableBoolean;
import com.github.jochenw.afw.core.util.Strings;


/** Test suite for the {@link Cli} class.
 */
public class CliTest {
	/** Example of an options bean.
	 */
	public static class OptionsBean {
		private Path inputFile, outputFile, helperFile;
		private boolean verbose;
		private int intValue;
		private URL urlValue;
		private File legacyFile;
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
			    .handler((c,b) -> {
			    	c.getBean().verbose = b.booleanValue();
			    }).end()
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
				.pathOption("inputFile").property("inputFile").end()
				.stringOption("inputFile");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Duplicate option name: inputFile", e.getMessage());
		}

		try {
			Cli.of(new OptionsBean())
				.pathOption("inputFile", "if").property("inputFile").end()
				.stringOption("outputFile").property("outputFile").end()
				.stringOption("if");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Duplicate option name: if", e.getMessage());
		}

		try {
			Cli.of(new OptionsBean())
				.pathOption("inputFile").property("inputFile").end()
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

	/** Test case for a boolean option without value.
	 */
	@Test
	public void testBooleanOptionWithoutValue() {
		final OptionsBean ob = new OptionsBean();
		assertFalse(ob.verbose);
		Cli.of(ob)
		    .pathOption("i").property("inputFile").end()
		    .boolOption("v").property("verbose").end()
		    .parse("-i", "foo", "-v");
		assertTrue(ob.verbose);
	}

	/** Test of a boolean option with explicitly specified true as
	 * the value.
	 */
	@Test
	public void testBooleanOptionWithExplicitTrue() {
		{
			final OptionsBean ob = new OptionsBean();
			assertFalse(ob.verbose);
			Cli.of(ob)
				.pathOption("i").property("inputFile").end()
				.boolOption("v").property("verbose").end()
				.parse("-i", "foo", "--v=true");
			assertTrue(ob.verbose);
		}
		{
			final OptionsBean ob = new OptionsBean();
			assertFalse(ob.verbose);
			Cli.of(ob)
				.pathOption("i").property("inputFile").end()
				.boolOption("v").property("verbose").end()
				.parse("-i", "foo", "-v", "true");
			assertTrue(ob.verbose);
		}
	}

	/** Test of a boolean option with explicitly specified false as
	 * the value.
	 */
	@Test
	public void testBooleanOptionWithExplicitFalse() {
		{
			final OptionsBean ob = new OptionsBean();
			assertFalse(ob.verbose);
			Cli.of(ob)
				.pathOption("i").property("inputFile").end()
				.boolOption("v").property("verbose").end()
				.parse("-i", "foo", "--v=false");
			assertFalse(ob.verbose);
		}
		{
			final OptionsBean ob = new OptionsBean();
			assertFalse(ob.verbose);
			Cli.of(ob)
				.pathOption("i").property("inputFile").end()
				.boolOption("v").property("verbose").end()
				.parse("-i", "foo", "-v", "false");
			assertFalse(ob.verbose);
		}
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
		Functions.FailableFunction<@Nullable UsageException,@NonNull RuntimeException,?> usageHandler =
				(ue) -> new IllegalStateException(ue == null ? null : ue.getMessage());
		final Cli<OptionsBean> cli = Cli.of(new OptionsBean());
		assertNull(cli.getUsageHandler());
		cli.stringOption("inputFile", "if").required().handler((c,s) -> c.getBean().inputFile = Paths.get(s)).end()
		   .stringOption("outputFile", "of").required().handler((c,s) -> c.getBean().outputFile = Paths.get(s)).end()
		   .usageHandler(usageHandler);
		assertSame(usageHandler, cli.getUsageHandler());
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

	/** Test case for an invalid option.
	 */
	@Test
	public void testInvalidOption() {
		try {
			Cli.of(new OptionsBean())
				.pathOption("inputFile").property("inputFile").end()
				.pathOption("outputFile").property("outputFile").end()
				.parse("-if", "foo", "whatever", "-outputFile", "bar");
			fail("Expected Exception");
		} catch (UsageException ue) {
			assertFalse(Exceptions.hasCause(ue));
			assertEquals("Invalid option name: if", ue.getMessage());
		}
	}

	/**
	 * Test case for {@link Cli#extraArgsHandler(Functions.FailableBiConsumer)}
	 */
	@Test
	public void testExtraArgs() {
		/** Test using an extra argument without a handler. This is supposed to
		 * trigger a usage exception.
		 */
		try {
			Cli.of(new OptionsBean())
				.pathOption("inputFile").property("inputFile").end()
				.pathOption("outputFile").property("outputFile").end()
				.parse("-inputFile", "foo", "whatever", "-outputFile", "bar");
			fail("Expected Exception");
		} catch (UsageException ue) {
			assertFalse(Exceptions.hasCause(ue));
			assertEquals("Unexpected non-option argument: whatever", ue.getMessage());
		}
		final List<String> extraArgs = new ArrayList<>();
		Cli<OptionsBean> cli = Cli.of(new OptionsBean());
		final FailableBiConsumer<@NonNull Cli<OptionsBean>, @NonNull String, ?> handler =
			(FailableBiConsumer<@NonNull Cli<OptionsBean>, @NonNull String, ?>) (c,s) -> {
				assertNotNull(c);
				assertSame(cli, c);
				assertNotNull(s);
				extraArgs.add(s);
			};
		assertNull(cli.getExtraArgsHandler());
		cli.pathOption("inputFile").property("inputFile").end()
			.pathOption("outputFile").property("outputFile").end()
			.extraArgsHandler(handler)
		    .parse("abc", "-inputFile", "foo", "whatever", "-outputFile", "bar", "xyz", "baz");
		assertSame(handler, cli.getExtraArgsHandler());
		assertEquals(4, extraArgs.size());
		assertEquals("abc", extraArgs.get(0));
		assertEquals("whatever", extraArgs.get(1));
		assertEquals("xyz", extraArgs.get(2));
		assertEquals("baz", extraArgs.get(3));
	}

	/**
	 * Test for options, that are dynamically added, based on an
	 * "action" parameter.
	 */
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
	protected DnfOptions testActions(@NonNull String... pArgs) {
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
		final Function<@NonNull String[],LoggingOptions> optParser = (args) -> {
			return Cli.of(new LoggingOptions())
					.stringOption("logFile", "lf").handler((c,s) -> c.getBean().logFile = s).end()
					.enumOption(Level.class, "logLevel", "ll").caseInsensitive().handler((c,l) -> c.getBean().logLevel = l).end()
					.parse(args);
		};
		final BiConsumer<Level,@NonNull String[]> tester = (level, array) -> {
			final LoggingOptions opts = optParser.apply(array);
			if (level == null) {
				assertNull(opts.logLevel);
			} else {
				assertEquals(level, opts.logLevel);
			}
		};
		tester.accept(null, new @NonNull String[] {});
		tester.accept(Level.INFO, new @NonNull String[] {"-logLevel", "INFO"});
		tester.accept(Level.WARN, new @NonNull String[] {"-logLevel", "WARN"});
		try {
			tester.accept(Level.WARN, new @NonNull String[] {"-logLevel", "warn"});
		} catch (UsageException ue) {
			assertEquals("Invalid value for option logLevel: warn", ue.getMessage());
		}
	}

	/** Test case for {@link Cli#error(String)}, and {@link Cli#error(String,Throwable)}.
	 */
	@Test
	public void testCliError() {
		final Cli<?> cli = Cli.of(new OptionsBean());
		final String msg = "This is the error message.";
		final IllegalStateException ise0 = cli.error(msg);
		assertNotNull(ise0);
		assertSame(msg, ise0.getMessage());
		assertTrue(ise0.getCause() == null  ||  ise0.getCause() == ise0);
		final RuntimeException rte = new RuntimeException();
		final IllegalStateException ise1 = cli.error(msg, rte);
		assertNotNull(ise1);
		assertSame(msg, ise1.getMessage());
		assertSame(rte, ise1.getCause());
	}

	/** Test case for {@link Cli.Context#error(String)}, and
	 * {@link Cli.Context#error(String, Throwable)}.
	 */
	@Test
	public void testContextError() {
		final String msg = "This is the error message";
		final BiConsumer<String,Throwable> tester = (m,th) -> {
			@SuppressWarnings("null")
			final @NonNull String[] args = (@NonNull String[]) new String[] {"-error", "foo"};
			Cli.of(new OptionsBean())
					.stringOption("error").handler((c,s) -> {
						if (th == null) {
							throw c.error(m);
						} else {
							throw c.error(m, th);
						}
					}).end().parse(args);
		};
		try {
			tester.accept(msg, null);
			fail("Expected Exception");
		} catch (IllegalStateException ise) {
			assertSame(msg, ise.getMessage());
			assertTrue(ise.getCause() == null || ise == ise.getCause());
		}
		final RuntimeException rte = new RuntimeException();
		try {
			tester.accept(msg,  rte);
		} catch (IllegalStateException ise) {
			assertSame(msg, ise.getMessage());
			assertSame(rte, ise.getCause());
		}
	}

	/** Test case for {@link Cli#intOption(String, String...)}.
	 */
	@Test
	public void testIntOption() {
		final OptionsBean ob = new OptionsBean();
		assertEquals(0, ob.intValue);
		final @NonNull String[] args = {"-iv", "42"};

		final OptionsBean ob2 = Cli.of(ob)
				.intOption("iv").property("intValue").end().parse(args);
		assertSame(ob2, ob);
		assertEquals(42, ob.intValue);
	}

	/** Test case for {@link Cli#urlOption(String, String...)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testUrlOption() throws Exception {
		final OptionsBean ob = new OptionsBean();
		assertNull(ob.urlValue);
		final @NonNull String[] args = {"-uv", "http://127.0.0.1/"};

		final OptionsBean ob2 = Cli.of(ob)
				.urlOption("uv").property("urlValue").end().parse(args);

		assertSame(ob2, ob);
		final URL url = Strings.asUrl("http://127.0.0.1/");
		assertEquals(url, ob.urlValue);
	}

	/** Test case for {@link Option#property(String,Functions.FailableFunction)}.
	 */
	@Test
	public void testPropertyStringFunction() {
		final @NonNull String[] args = {"-lf", "."};
		final OptionsBean ob = Cli.of(new OptionsBean())
				.pathOption("legacyFile", "lf").property("legacyFile", (p) -> p.toFile()).end()
				.parse(args);
		final File file = ob.legacyFile;
		assertNotNull(file);
		assertEquals(".", file.getPath());
	}

	/** Test case for {@link Cli.Context#getOptName()}.
	 */
	@Test
	public void testGetOptName() {
		final List<String> optNames = new ArrayList<>();
		final @NonNull String[] args = { "-iFile", "foo", "-of", "bar", "-helperFile", "baz" };
		final FailableBiConsumer<Context<OptionsBean>,Path,?> argsHandler = (c,p) -> optNames.add(c.getOptName());
		Cli.of(new OptionsBean())
			.pathOption("inputFile", "iFile", "if").handler(argsHandler).end()
			.pathOption("outputFile", "oFile", "of").handler(argsHandler).end()
			.pathOption("helperFile", "hFile", "hf").handler(argsHandler).end()
			.parse(args);
		assertEquals(3, optNames.size());
		assertEquals("iFile", optNames.get(0));
		assertEquals("of", optNames.get(1));
		assertEquals("helperFile", optNames.get(2));
 	}

	/** Test for {@link Cli#usage(String)}.
	 */
	@Test
	public void testCliUsage() {
		final String msg = "This is the error message.";
		final UsageException ue = Cli.of(new OptionsBean()).usage(msg);
		assertSame(msg, ue.getMessage());
		assertFalse(Exceptions.hasCause(ue));
	}

	/** Test for the {@link Cli#validator(Functions.FailableFunction)}.
	 */
	@Test
	public void testCliValidator() {
		final @NonNull FailableFunction<@NonNull OptionsBean,@Nullable String,?> validator = (ob) -> {
			if (ob.inputFile != null  &&  ob.inputFile.toString().equals("foo")) {
				return "Expected inputFile bar, got foo";
			}
			return null;
		};
		final Supplier<Cli<OptionsBean>> cliSupplier = () -> {
			Cli<OptionsBean> cli = Cli.of(new OptionsBean())
					.pathOption("inputFile").property("inputFile").end();
			assertNull(cli.getValidator());
			cli.validator(validator);
			assertSame(validator, cli.getValidator());
			return cli;
		};
		final OptionsBean ob1 = cliSupplier.get().parse("-inputFile", "bar");
		assertNotNull(ob1);
		assertNotNull(ob1.inputFile);
		assertEquals("bar", ob1.inputFile.toString());
		try {
			cliSupplier.get().parse("-inputFile", "foo");
			fail("Expected Exception");
		} catch (UsageException ue) {
			assertEquals("Expected inputFile bar, got foo", ue.getMessage());
			assertFalse(Exceptions.hasCause(ue));
		}
	}

	/** Test for {@link Option#repeatable()}.
	 */
	@Test
	public void testRepeatable() {
		/** Repeating an option without repeatable() is supposed to cause an error.
		 */
		try {
			Cli.of(new OptionsBean())
			    .stringOption("inputFile", "if").handler((c,s) -> c.getBean().inputFile = Paths.get(s)).end()
			    .parse("-inputFile", "foo", "-if", "bar");
			fail("Expected Exception");
		} catch (UsageException ue) {
			assertEquals("The option if may be used only once.", ue.getMessage());
			assertFalse(Exceptions.hasCause(ue));
		}
		/** With repeatable() we can do that.
		 */
		final List<String> values = new ArrayList<>();
		Cli.of(new OptionsBean())
	        .stringOption("inputFile", "if").repeatable().handler((c,l) -> {
	        	values.addAll(l);
	        }).end()
	        .parse("-inputFile", "foo", "-if", "bar");
		assertEquals(2, values.size());
		assertEquals("foo", values.get(0));
		assertEquals("bar", values.get(1));		
	}

	/** Tests using <pre>--optName=optValue</pre>.
	 */
	@Test
	public void testInlineValues() {
		{
			final OptionsBean ob0 = new OptionsBean();
			assertNull(ob0.inputFile);
			Cli.of(ob0)
				.pathOption("if").property("inputFile").end()
				.parse("-if", "foo");
			assertNotNull(ob0.inputFile);
			assertEquals("foo", ob0.inputFile.toString());
		}
		{
			final OptionsBean ob0 = new OptionsBean();
			assertNull(ob0.inputFile);
			Cli.of(ob0)
				.pathOption("if").property("inputFile").end()
				.parse("-if=foo");
			assertNotNull(ob0.inputFile);
			assertEquals("foo", ob0.inputFile.toString());
		}
	}

	/** Test for a missing option value.
	 */
	@Test
	public void testMissingOptionValue() {
		try {
			final OptionsBean ob0 = new OptionsBean();
			assertNull(ob0.inputFile);
			Cli.of(ob0)
				.pathOption("inputFile", "if").property("inputFile").end()
				.parse("-if");
			fail("Expected Exception");
		} catch (UsageException ue) {
			assertFalse(Exceptions.hasCause(ue));
			assertEquals("Option requires an argument: if", ue.getMessage());
		}
	}

	/** Test for a default value of an option, that isn't used.
	 */
	@Test
	public void testDefaultValueForUnusedOption() {
		final OptionsBean ob0 = new OptionsBean();
		assertNull(ob0.inputFile);
		Cli.of(ob0)
		    .pathOption("inputFile", "if").property("inputFile").defaultValue("pom.xml").end()
			.parse();
		assertEquals(Paths.get("pom.xml"), ob0.inputFile);
	}

	/** Test for a default value of an option, that is used.
	 */
	@Test
	public void testDefaultValueForUsedOption() {
		final OptionsBean ob0 = new OptionsBean();
		assertNull(ob0.inputFile);
		Cli.of(ob0)
		    .pathOption("inputFile", "if").property("inputFile").defaultValue("pom.xml").end()
			.parse("-if", "pam.xml");
		assertEquals(Paths.get("pam.xml"), ob0.inputFile);
	}

	/** Test a missing argument handler.
	 */
	@Test
	public void testMissingArgumentHandler() {
		try {
			final OptionsBean ob0 = new OptionsBean();
			assertNull(ob0.inputFile);
			Cli.of(ob0)
				.pathOption("inputFile", "if").end()
				.parse("-if");
			fail("Expected Exception");
		} catch (NoSuchElementException nsee) {
			assertFalse(Exceptions.hasCause(nsee));
			assertEquals("No argument handler has been specified for option inputFile, and any value would be discarded.",
					     nsee.getMessage());
		}
	}
	/** Test triggering the 'help' option.
	 */
	@Test
	public void testHelpMessage() {
		final List<@NonNull String> list = Arrays.asList("-help", "-h", "--help");
		for (@NonNull String optName : list) {
			try {
				Cli.of(new OptionsBean())
					.pathOption("if").property("inputFile").end()
					.parse("-if", "foo", optName);
				fail("Expected Exception");
			} catch (UsageException ue) {
				assertFalse(Exceptions.hasCause(ue));
				assertNull(ue.getMessage());
			}
		}
	}

	/**
	 * Test for {@link Cli#validate()}
	 */
	@Test
	public void testValidate() {
		try {
			final Cli<OptionsBean> cli = Cli.of(new OptionsBean())
				.pathOption("if").property("inputFile").end();
			cli.pathOption("of");
			cli.parse("-if", "foo");
			fail("Expected Exception");
		} catch (NoSuchElementException nsee) {
			assertFalse(Exceptions.hasCause(nsee));
			assertEquals("No argument handler has been specified for option of,"
					+ " and any value would be discarded.", nsee.getMessage());
		}
	}

	/** Some things, that are basically nonsense, but we want to enforce being
	 * covered.
	 */
	@Test
	public void testCoverageNonsense() {
		// Array of secondary names is null.
		Cli.of(new OptionsBean()).stringOption("if", (@NonNull String[]) null);
		for (String fileName : Arrays.asList("pom.xml", "-pom.xml", "--pom.xml")) {
			@SuppressWarnings("null")
			final @NonNull String flName = fileName;
			final OptionsBean ob = Cli.of(new OptionsBean()).pathOption("if").property("inputFile").end()
					.boolOption("verbose").property("verbose").end()
					.parse("-if", flName, "-verbose");
			assertEquals(Paths.get(flName), ob.inputFile);
		}
	}
}
