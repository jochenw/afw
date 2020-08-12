package com.github.jochenw.afw.bootstrap.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jochenw.afw.bootstrap.log.Logger.Level;



public class Args {
	public static class Options {
		private Path propertyFile;
		private Path logFile;
		private Level logLevel;
		private List<String> mainArgs;
		private boolean helpRequired, versionRequired;

		/**
		 * @return the propertyFile
		 */
		public Path getPropertyFile() {
			return propertyFile;
		}
		/**
		 * @param pPropertyFile the propertyFile to set
		 */
		public void setPropertyFile(Path pPropertyFile) {
			propertyFile = pPropertyFile;
		}
		/**
		 * @return the logFile
		 */
		public Path getLogFile() {
			return logFile;
		}
		/**
		 * @param pLogFile the logFile to set
		 */
		public void setLogFile(Path pLogFile) {
			logFile = pLogFile;
		}
		/**
		 * @return the logLevel
		 */
		public Level getLogLevel() {
			return logLevel;
		}
		/**
		 * @param pLogLevel the logLevel to set
		 */
		public void setLogLevel(Level pLogLevel) {
			logLevel = pLogLevel;
		}
		/**
		 * @return the mainArgs
		 */
		public List<String> getMainArgs() {
			return mainArgs;
		}
		/**
		 * @param pMainArgs the mainArgs to set
		 */
		public void setMainArgs(List<String> pMainArgs) {
			mainArgs = pMainArgs;
		}
		public boolean isHelpRequired() {
			return helpRequired;
		}
		public void setHelpRequired(boolean pHelpRequired) {
			helpRequired = pHelpRequired;
		}
		public boolean isVersionRequired() {
			return versionRequired;
		}
		public void setVersionRequired(boolean pVersionRequired) {
			versionRequired = pVersionRequired;
		}
	}
	public interface ErrorHandler {
		public RuntimeException error(String pMsg);
	}
	public static class ArgsException extends RuntimeException {
		private static final long serialVersionUID = -3017442924825635329L;
	
		/**
		 * @param pMessage
		 */
		public ArgsException(String pMessage) {
			super(pMessage);
		}
	
		/**
		 * @param pCause
		 */
		public ArgsException(Throwable pCause) {
			super(pCause);
		}
	}

	private static final ErrorHandler DEFAULT_ERROR_HANDLER = new ErrorHandler() {
		public RuntimeException error(String pMsg) {
		    return new ArgsException(pMsg);
		}
	};

	public final Options parse(String[] pArgs, ErrorHandler pErrorHandler) {
		final ErrorHandler errorHandler = pErrorHandler == null ? DEFAULT_ERROR_HANDLER : pErrorHandler; 
		final Options options = new Options();
		final List<String> args = new ArrayList<String>(Arrays.asList(pArgs));
		while (!args.isEmpty()) {
			final String arg = args.remove(0);
			final String value;
			String opt;
			if (arg.startsWith("--")) {
				opt = arg.substring(2);
			} else if (arg.startsWith("-")  ||  arg.startsWith("/")) {
				opt = arg.substring(1);
			} else {
				throw errorHandler.error("Invalid option: " + arg);
			}
			final int offset = opt.indexOf('=');
			if (offset == -1) {
				value = null;
			} else {
				value = opt.substring(offset+1);
				opt = opt.substring(0, offset);
			}
			if ("propertyFile".equals(opt)  ||  "pf".equals(opt)) {
				if (value == null) {
					throw errorHandler.error("Option " + opt + " requires an argument (property file).");
				}
				if (options.getPropertyFile() != null) {
					throw errorHandler.error("Option " + opt + " must be used only once.");
				}
				final Path f = Paths.get(value);
				if (!Files.isRegularFile(f)) {
					throw errorHandler.error("Invalid value for option " + opt + ": " + value
											  + " (Doesn't exist, or is not a file.)");
				}
				if (!Files.isReadable(f)) {
					throw errorHandler.error("Invalid value for option " + opt + ": " + value
											  + " (File isn't readable.)");
				}
				options.setPropertyFile(f);
			} else if ("logFile".equals(opt)  ||  "lf".equals(opt)) {
				if (value == null) {
					throw errorHandler.error("Option " + opt + " requires an argument (property file).");
				}
				if (options.getLogFile() != null) {
					throw errorHandler.error("Option " + opt + " must be used only once.");
				}
				final Path f = Paths.get(value);
				final Path dir = f.getParent();
				if (dir != null  &&  !Files.isDirectory(dir)) {
					throw errorHandler.error("Invalid value for option " + opt + ": " + value
							                  + " (Directory doesn't exist, or is not a directory.)");
				}
				if (!Files.isWritable(f)) {
					throw errorHandler.error("Invalid value for option " + opt + ": " + value
							                  + " (Directory, or file, isn't writable.)");
				}
				options.setLogFile(f);
			} else if ("logLevel".equals(opt)  ||  "ll".equals(opt)) {
				if (value == null) {
					throw errorHandler.error("Option " + opt + " requires an argument (log level).");
				}
				if (options.getLogFile() != null) {
					throw errorHandler.error("Option " + opt + " must be used only once.");
				}
				final Level level;
				try {
					level = Level.valueOf(value.toUpperCase());
				} catch (IllegalArgumentException e) {
					throw errorHandler.error("Invalid value for option " + opt + ": " + value
			                  + " (Expected TRACE,DEBUG,INFO,WARN,ERROR, or FATAL.)");
				}
				options.setLogLevel(level);
			} else if ("help".equals(opt)  ||  "h".equals(opt)  ||  "?".equals(opt)) {
				options.setHelpRequired(true);
				return options;
			} else if ("version".equals(opt)) {
				options.setVersionRequired(true);
				return options;
			} else if ("".equals(opt)) {
				options.setMainArgs(args);
				break;
			} else {
				throw errorHandler.error("Invalid argument: " + arg);
			}
		}
		if (options.getPropertyFile() == null) {
			throw errorHandler.error("Required option not given: --propertyFile=<FILE>.");
		}
		if (options.getMainArgs() == null) {
			options.setMainArgs(new ArrayList<String>());
		}
		return options;
	}
}
