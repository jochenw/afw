package com.github.jochenw.afw.bootstrap;

import java.io.PrintStream;

import com.github.jochenw.afw.bootstrap.cli.Args;
import com.github.jochenw.afw.bootstrap.cli.Args.ErrorHandler;
import com.github.jochenw.afw.bootstrap.cli.Args.Options;

public class Main {
	public static void main(String[] pLauncherArgs) {
		final Options options = new Args().parse(pLauncherArgs, new ErrorHandler() {
			@Override
			public RuntimeException error(String pMsg) {
				throw usage(pMsg);
			}
		});
		new Launcher().run(options);
	}

	private static RuntimeException usage(String pMessage) {
		final PrintStream ps = System.err;
		if (pMessage != null) {
			ps.println(pMessage);
			ps.println();
		}
		//          01234567890123456789012345678901234567890123456789012345678901234567890123456789
		//          0         1         2         3         4         5         6         7
		ps.println("Usage: java " + Main.class.getName() + " <LAUNCHER_ARGS> [-- <APPLICATION_ARGS>]");
		ps.println();
		ps.println("Possible launcher arguments are:");
		ps.println("  --propertyFile=<PF> Specifies the property file to read as the launcher");
		ps.println("                      configuration. See 'Launcher Properties' below.");
		ps.println();
		ps.println("Possible application args are specified by the launched applications main class.");
		ps.println();
		ps.println("Launcher Properties:");
		ps.println("  launcher.base.dir=<DIR>   Specifies the launchers base directory. File names");
		ps.println("                            are relative to the base directory.");
		ps.println("  main.class=<FULLY_QUALIFIED_CLASS_NAME> The launched applications main class.");
		ps.println("  update.property.url=<URL> URL of the update property file, which is being read");
		ps.println("                            for the uptodate check.");
		ps.println("  update.current.version=<V> Specifies the current application version. An");
		ps.println("                             update is performed, if the version in the update");
		ps.println("                             property file is different.");
		ps.println("  main.classpath.[0..N]=<FILE>    Specifies the classpath elements, which are");
		ps.println("                                  used to launch the main application.");
		System.exit(1);
		return null;
	}
}
