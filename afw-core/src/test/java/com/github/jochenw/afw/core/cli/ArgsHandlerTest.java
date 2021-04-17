package com.github.jochenw.afw.core.cli;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.github.jochenw.afw.core.util.MutableBoolean;


/** Test for the {@link ArgsHandler}.
 */
public class ArgsHandlerTest {
	private abstract class AbstractArgsHandler extends ArgsHandler<WmTestSuiteRunnerOptions> {
		@Override
		protected WmTestSuiteRunnerOptions newOptionBean() {
			return new WmTestSuiteRunnerOptions();
		}

		@Override
		protected RuntimeException error(String pMsg) {
			final PrintStream ps = System.err;
			if (pMsg != null) {
				ps.println(pMsg);
				ps.println();
			}
			ps.println("Usage: java " + ArgsHandlerTest.class + " <options>");
			ps.println();
			ps.println("Required options are:");
			ps.println("  -wmhd|-wmHomeDir <P> Sets the webMethods home directory.");
			ps.println("  -pp|-projectPath <P> Sets the project path");
			ps.println("  -isUrl <U>           Sets the IS test servers URL.");
			ps.println("  -isUser <U>          Sets the IS test servers user name.");
			ps.println("  -isPass <U>          Sets the IS test servers password.");
			ps.println();
			ps.println("Other options are:");
			ps.println("  -coverage            Requests, that code coverage is being generated.");
			ps.println("  -help|-h|/?          Prints this help message, and exits with error status.");
			if (pMsg == null) {
				return new IllegalStateException();
			} else {
				return new IllegalStateException(pMsg);
			}
		}

		@Override
		protected WmTestSuiteRunnerOptions parse(String[] pArgs) {
			final WmTestSuiteRunnerOptions options = super.parse(pArgs);
			if (options.wmHomeDir == null) {
				throw error("Required option missing: -wmHomeDir");
			}
			if (options.projectPath == null) {
				throw error("Required option missing: -projectPath");
			}
			if (options.isUrl == null) {
				throw error("Required option missing: -isUrl");
			}
			if (options.isUser == null) {
				throw error("Required option missing: -isUser");
			}
			if (options.isPass == null) {
				throw error("Required option missing: -isPass");
			}
			return options;
		}

		@Override
		protected void registerOptions(OptionRegistry<WmTestSuiteRunnerOptions> pRegistry) {
			super.registerOptions(pRegistry);
			pRegistry.register((pCtx, pOptions, pName, pMainName) -> {
				final Path p = pCtx.getSinglePathValue();
				if (!Files.isDirectory(p)) {
					throw error("Invalid argument for option " + pName
							    + ": Expected existing directory, got " + p);
				}
				pOptions.wmHomeDir = p;
			}, "wmHomeDir", "wmhd");
			pRegistry.register((pCtx, pOptions, pName, pMainName) -> {
				final Path p = pCtx.getSinglePathValue();
				if (!Files.isDirectory(p)) {
					throw error("Invalid argument for option " + pName
							    + ": Expected existing directory, got " + p);
				}
				pOptions.projectPath = p;
			}, "projectPath", "pp");
			pRegistry.register((pCtx, pOptions, pName, pMainName) -> {
				final String u = pCtx.getSingleValue();
				final URL url;
				try {
					url = new URL(u);
				} catch (MalformedURLException e) {
					throw error("Invalid argument for option " + pName
							    + ": Expected valid URL, got " + u);
				}
				pOptions.isUrl = url;
			}, "isUrl");
			pRegistry.register((pCtx, pOptions, pName, pMainName) -> {
				pOptions.isUser = pCtx.getSingleValue();
			}, "isUser");
			pRegistry.register((pCtx, pOptions, pName, pMainName) -> {
				pOptions.isPass = pCtx.getSingleValue();
			}, "isPass");
			pRegistry.register((pCtx, pOptions, pName, pMainName) -> {
				pOptions.coverage = true;
			}, "coverage");
		}
	}
	/** Option bean for {@link ArgsHandlerTest#testWmTestSuiteRunnerOptions()}.
	 */
	public static class WmTestSuiteRunnerOptions {
		Path projectPath, wmHomeDir;
		URL isUrl;
		String isUser, isPass;
		boolean coverage;
	}
	/** Test parsing the options for the {@link WmTestSuiteRunnerOptions}.
	 */
	@Test
	public void testWmTestSuiteRunnerOptions() {
		final MutableBoolean validated0 = new MutableBoolean();
		final ArgsHandler<WmTestSuiteRunnerOptions> argsHandler0 = new AbstractArgsHandler() {
			@Override
			protected void run(WmTestSuiteRunnerOptions pOptions) throws Exception {
				assertEquals("src", pOptions.projectPath.toString());
				assertEquals("target", pOptions.wmHomeDir.toString());
				assertEquals("http://127.0.0.1:5555", pOptions.isUrl.toString());
				assertEquals("Administrator", pOptions.isUser);
				assertEquals("manage", pOptions.isPass);
				assertFalse(pOptions.coverage);
				validated0.set();
			}
		};
		argsHandler0.run(new String[]{"-projectPath", "src",
				                       "-wmHomeDir", "target",
				                       "-isUrl", "http://127.0.0.1:5555",
				                       "-isUser", "Administrator",
				                       "-isPass", "manage"});
		assertTrue(validated0.isSet());
		try {
			argsHandler0.run(new String[]{"-wmHomeDir", "target",
                    					   "-isUrl", "http://127.0.0.1:5555",
                    					   "-isUser", "Administrator",
                    					   "-isPass", "manage"});
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Required option missing: -projectPath", e.getMessage());
		}
		try {
			argsHandler0.run(new String[]{"-projectPath", "src",
                    					   "-isUrl", "http://127.0.0.1:5555",
                    					   "-isUser", "Administrator",
                    					   "-isPass", "manage"});
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Required option missing: -wmHomeDir", e.getMessage());
		}
	}

}
