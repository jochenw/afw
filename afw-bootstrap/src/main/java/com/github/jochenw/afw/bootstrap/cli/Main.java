package com.github.jochenw.afw.bootstrap.cli;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jochenw.afw.bootstrap.Launcher;
import com.github.jochenw.afw.bootstrap.cli.Args.ErrorHandler;
import com.github.jochenw.afw.bootstrap.cli.Args.Options;
import com.github.jochenw.afw.bootstrap.log.FileLogger;
import com.github.jochenw.afw.bootstrap.log.Logger;
import com.github.jochenw.afw.bootstrap.log.Logger.Level;
import com.github.jochenw.afw.bootstrap.log.SystemOutLogger;


/** This class, called the application launcher, is the booter's
 * command line interface. The launcher's purpose is to run with
 * a minimal classpath, before the actual application. It can do
 * things like upgrading the application, because the applications
 * files are not yet in use. Finally, the launcher builds the
 * applications classpath, and calls the applications actual main
 * class with the remaining command line arguments. (The launcher
 * consumes some command line arguments, up to, and including the
 * option terminator "--".)
 */
public class Main {
	public static void main(String[] pArgs) {
		final ErrorHandler errorHandler = new ErrorHandler() {
			@Override
			public RuntimeException error(String pMsg) {
				throw usage(pMsg);
			}
		};
		final Options options = new Args().parse(pArgs, errorHandler);
		new Launcher().run(options);
	}

	public static RuntimeException usage(String pMsg) {
		final PrintStream ps = System.err;
		if (pMsg != null) {
			ps.println(pMsg);
			ps.println();
		}
		// In what follows, we do not want to emit more than 80 characters per line.
		// The pseudo ruler below should help with that.
		//          01234567890123456789012345678901234567890123456789012345678901234567890123456789
		//          0         1         2         3         4         5         6         7
		ps.println("Usage: java " + Main.class.getName() + " [<LAUNCHER_ARGS>] [-- <MAIN_ARGS>]");
		ps.println();
		ps.println("Possible launcher arguments are:");
		ps.println(" --propertyFile=<F> Specifies the property file with the launchers");
		ps.println("                    configuration. This option is required. Possible");
		ps.println("                    abbreviation: --pf=<F>");
		ps.println(" --logFile=<F>      Specifies the launchers log file. (Optional, default is");
		ps.println("                    STDOUT.) Possible abbreviation: --lf=<F>");
		ps.println(" --logLevel=<L>     Specifies the launchers log level, either of TRACE,DEBUG,");
		ps.println("                    INFO,WARN,ERROR,FATAL. (Optional, default is INFO.)");
		ps.println("                    Possible abbreviation: --ll=<L>");
		ps.println(" --                 Specifies the end of the launcher arguments. All remaining");
		ps.println("                    command line arguments (the main argument) will be passed");
		ps.println("                    to the main class.");
		ps.println(" --help             Print this help message, and exit with status error (rc=1).");
		ps.println(" --version          Print the launchers version number, and exit with status");
		ps.println("                    success (rc=0).");
		System.exit(1);
		return null; // We do not actually return, so it's safe to return null.
		             // Officially, we return an Exception, though, so the compilers analysis will
		             // detect the right thing, if we write
		             //     throw usage("Message");
	}
}
