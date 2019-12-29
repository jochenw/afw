package com.github.jochenw.afw.core.cli;

import com.github.jochenw.afw.core.cli.Options;
import static org.junit.Assert.*;

import org.junit.Test;

public class OptionsTest {

	@Test
	public void testQseIsOptions() {
		final Options options = new Options()
				.pathOption().names("scanDir").description("Sets the scan directory (Default: Current Directory)").end()
				.pathOption().names("outFile").description("Sets the output file (Default: STDOUT)").end()
				.pathOption().names("logFile").description("Sets the log file (Default: STDERR").end()
				.pathOption().names("rulesFile").description("Sets the rules file (Required)").required().end()
				.intOption(-1).names("maxNumberOfErrors").description("Sets the maximum number of errors (Default -1=unlimited).").end()
				.intOption(-1).names("maxNumberOfWarnings").description("Sets the maximum number of warnings (Default -1=unlimited).").end()
				.intOption(-1).names("maxNumberOfOtherIssues").description("Sets the maximum number of other issues (Default -1=unlimited).").end()
				.booleanOption(false).names("h", "?", "help").description("Prints this help message, and exits with error status.").end();
		final Options.Result result = options.process(new String[] {"-maxNumberOfErrors", "5"}, null);
		assertFalse(result.getBoolValue("h"));
		assertNull(result.getStrValue("scanDir"));
		assertEquals(5, result.getIntValue("maxNumberOfErrors"));
		assertEquals(-1, result.getIntValue("maxNumberOfWarnings"));
		assertNull(result.getRemainingArgs());
	}

}
