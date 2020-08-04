package com.github.jochenw.afw.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.github.jochenw.afw.bootstrap.log.AbstractLogger;
import com.github.jochenw.afw.bootstrap.log.Logger;

public class Main {
	public static void main(String[] pLauncherArgs) {
		Path propertyFile = null;
		final List<String> applicationArgs = new ArrayList<>();
		final List<String> args = new ArrayList<>(Arrays.asList(pLauncherArgs));
		while (!args.isEmpty()) {
			String value = null;
			final String arg = args.remove(0);
			String opt;
			if (arg.startsWith("--")) {
				opt = arg.substring(2);
			} else if (arg.startsWith("-")  ||  arg.startsWith("/")) {
				opt = arg.substring(1);
			} else {
				throw usage("Invalid argument: " + arg);
			}
			final int offset = opt.indexOf('=');
			if (offset != -1) {
				value = opt.substring(offset+1);
				opt = opt.substring(0,  offset);
			}
			if ("propertyFile".equals(opt)) {
				if (value == null) {
					throw usage("Option " + opt + " requires an argument: (Property file name)");
				}
				if (propertyFile != null) {
					throw usage("Option " + opt + " may be used only once.");
				} else {
					propertyFile = Paths.get(value);
					if (!Files.isRegularFile(propertyFile)  ||  !Files.isReadable(propertyFile)) {
						throw usage("Invalid argument for option " + opt + ": Property file "
								+ propertyFile + " doesn't exist, is unreadable, or is not a file.");
					}
				}
			} else if ("help".equals(opt)  ||  "h".equals(opt)  ||  "?".equals(opt)) {
				throw usage(null);
			} else if ("".equals(opt)) {
				applicationArgs.addAll(args);
				args.clear();
			}
		}
		if (propertyFile == null) {
			throw usage("Required option missing: --propertyFile=<FILE>");
		}
		final Properties props = new Properties();
		try (InputStream in = Files.newInputStream(propertyFile)) {
			if (propertyFile.getFileName().toString().endsWith(".xml")) {
				props.loadFromXML(in);
			} else {
				props.load(in);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		final Logger logger = new AbstractLogger(System.out) {};
		new Launcher(logger).run(props, applicationArgs);
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
