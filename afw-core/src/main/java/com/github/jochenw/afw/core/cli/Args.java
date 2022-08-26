/**
 * 
 */
package com.github.jochenw.afw.core.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Objects;


/** A parser for command line arguments.
 * @deprecated Use the {@link Cli} class.
 */
public class Args {
	/** The Context object is provided by the command line parser when invoking
	 * the {@link Args.Listener}.
	 */
	public interface Context {
		/** Returns, whether a value is available for the
		 * current option. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method.
		 * @return True, if the current option has a value value.
		 *   If so, the method {@link #getValue()} can be
		 *   invoked safely to retrieve the value.
		 *   Otherwise, the value false will be returned, and
		 *   invoking {@link #getValue()} will raise an
		 *   exception.
		 */
		public boolean isValueAvailable();
		/** Returns the current options value. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method.
		 * @return The current options value.
		 * @throws ArgsException No value is available. Use
		 *   {@link #isValueAvailable()} to prevent this.
		 */
		public String getValue() throws ArgsException;
		/** Returns the current options value. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method. Additionally, invoking this method
		 * indicates, that it is an error, if this option
		 * is being repeated.
		 * @return The current options value.
		 * @throws ArgsException No value is available. Use
		 *   {@link #isValueAvailable()} to prevent this.
		 */
		public String getSingleValue() throws ArgsException;
		/** Returns the current listener.
		 * @return The current listener.
		 */
		public Args.Listener getListener();
		/** Returns the current options value as a
		 * {@link Path} object. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method.
		 * @return The current options value.
		 * @throws ArgsException No value is available. Use
		 *   {@link #isValueAvailable()} to prevent this.
		 */
		public default Path getPathValue() throws ArgsException {
			return Paths.get(getValue());
		}
		/** Returns the current options value as a
		 * {@link Path} object. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method. Additionally, invoking this method
		 * indicates, that it is an error, if this option
		 * is being repeated.
		 * @return The current options value.
		 * @throws ArgsException No value is available. Use
		 *   {@link #isValueAvailable()} to prevent this.
		 */
		public default Path getSinglePathValue() throws ArgsException {
			return Paths.get(getSingleValue());
		}
		/** Returns the current options value as a
		 * {@link Path} object. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method. Additionally, invoking this method
		 * indicates, that it is an error, if this option
		 * is being repeated.
		 * @param pPredicate A predicate, which tests, whether
		 * the option value is valid. If the predicate returns
		 * false, then an {@link ArgsException} is thrown with
		 * the given error message.
		 * @param pMsg The error message, which is used if the
		 * predicate returns false.
		 * @return The current options value.
		 * @throws ArgsException No value is available. Use
		 *   {@link #isValueAvailable()} to prevent this.
		 */
		public default Path getSinglePathValue(Predicate<Path> pPredicate, String pMsg) throws ArgsException {
			final Path p = getSinglePathValue();
			if (!pPredicate.test(p)) {
				final RuntimeException rte = getListener().error(pMsg);
				if (rte == null) {
					throw new ArgsException(pMsg);
				} else {
					throw rte;
				}
			}
			return p;
		}
		/** Returns the current options value as an
		 * integer number. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method.
		 * @return The current options value.
		 * @throws ArgsException No value is available. Use
		 *   {@link #isValueAvailable()} to prevent this.
		 */
		public default int getIntValue() throws ArgsException {
			final String v = getValue();
			try {
				return Integer.parseInt(v);
			} catch (NumberFormatException e) {
				final String msg = "Invalid argument for option " + getName() + ": Expected integer value, got " + v;
				final RuntimeException rte = getListener().error(msg);
				if (rte == null) {
					throw new ArgsException(msg);
				} else {
					throw rte;
				}
			}
		}
		/** Returns the current options value as an
		 * integer number. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method. Additionally, invoking this method
		 * indicates, that it is an error, if this option
		 * is being repeated.
		 * @return The current options value.
		 * @throws ArgsException No value is available. Use
		 *   {@link #isValueAvailable()} to prevent this.
		 */
		public default int getSingleIntValue() throws ArgsException {
			final String v = getSingleValue();
			try {
				return Integer.parseInt(v);
			} catch (NumberFormatException e) {
				final String msg = "Invalid argument for option " + getName() + ": Expected integer value, got " + v;
				final RuntimeException rte = getListener().error(msg);
				if (rte == null) {
					throw new ArgsException(msg);
				} else {
					throw rte;
				}
			}
		}
		/** Returns the current options value as an
		 * integer number. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method. Additionally, invoking this method
		 * indicates, that it is an error, if this option
		 * is being repeated.
		 * @param pPredicate A predicate, which tests, whether
		 * the option value is valid. If the predicate returns
		 * false, then an {@link ArgsException} is thrown with
		 * the given error message.
		 * @param pMsg The error message, which is used if the
		 * predicate returns false.
		 * @return The current options value.
		 * @throws ArgsException No value is available. Use
		 *   {@link #isValueAvailable()} to prevent this.
		 */
		public default int getSingleIntValue(IntPredicate pPredicate, String pMsg) throws ArgsException {
			final int i = getSingleIntValue();
			if (!pPredicate.test(i)) {
				final RuntimeException rte = getListener().error(pMsg);
				if (rte == null) {
					throw new ArgsException(pMsg);
				} else {
					throw rte;
				}
			}
			return i;
		}
		
		/** Returns the current options name. Invoking
		 * this method is valid only within the
		 * {@link Args.Listener#option(Context, String)}
		 * method.
		 * @return The current options value.
		 */
		public String getName();
	}
	/** The Listener object must be provided by the command line parsers user.
	 */
	public interface Listener {
		/** Called, whenever a new option is detected. The listener
		 * may invoke the method {@link Args.Context#getValue()},
		 * if an option value is required.
		 * @param pCtx The context object, which the listener may use
		 *   to retrieve an option value.
		 * @param pName The options name. For example, in the case of
		 *   "--file", this would be "file".
		 */
		void option(Context pCtx, String pName);
		/** By default, the error handler throws an {@link ArgsException}.
		 * Instead, the error handler may choose to return, and continue
		 * parsing.
		 * @param pMsg The error message.
		 * @return An exception, which should be thrown by the caller.
		 */
		public default RuntimeException error(String pMsg) {
			return new ArgsException(pMsg);
		}
	}

	/** Default implementation of {@link Context}.
	 */
	protected static class ContextImpl implements Context {
		private final @Nonnull Listener listener;
		private final @Nonnull List<String> args;
		private String arg;
		private String name;
		private final Set<String> singleOptionNames = new HashSet<>();

		/**
		 * @param pListener
		 * @param pArgs
		 */
		public ContextImpl(@Nonnull Listener pListener, @Nonnull List<String> pArgs) {
			args = Objects.requireNonNull(pArgs, "Args");
			listener = Objects.requireNonNull(pListener, "Listener");
		}

		@Override
		public String getName() {
			return name;
		}

		/** Sets the current options name.
		 * @param pName The current options name.
		 */
		public void setName(String pName) {
			name = pName;
		}

		/** Sets the current argument.
		 * @param pArg The current argument.
		 */
		public void setArg(String pArg) {
			arg = pArg;
		}

		@Override
		public boolean isValueAvailable() {
			if (arg != null) {
				if (arg.indexOf('=') != -1) {
					return true;
				}
			}
			if (args.isEmpty()) {
				return false;
			}
			final String v = args.get(0);
			return v != null  &&  !v.startsWith("-");
		}

		@Override
		public String getValue() throws ArgsException {
			if (arg != null) {
				final int offset = arg.indexOf('=');
				if (offset != -1) {
					return arg.substring(offset+1);
				}
			}
			if (args.isEmpty()) {
				String msg = "Option " + name + " requires an argument.";
				final RuntimeException rte = listener.error(msg);
				if (rte != null) {
					throw rte;
				} else {
					throw new ArgsException(msg);
				}
			} else {
				return args.remove(0);
			}
		}

		@Override
		public String getSingleValue() throws ArgsException {
		    if (!singleOptionNames.add(getName())) {
                        throw listener.error("The option " + name + " may be used only once.");
                    }
		    return getValue();
		}

		@Override
		public Listener getListener() {
			return listener;
		}
	}
	
	/**
	 * Called to parse the command line arguments, typically from within a main method.
	 * @param pListener A listener, which is being notified about options, that are detected.
	 * @param pArgs The command line arguments.
	 * @return Additional arguments, which are following the command line options. (These
	 *   will only be available, if an option "--" is used to terminate the options.)
	 */
	public @Nullable String[] parse(@Nonnull String[] pArgs, Listener pListener) {
		final @Nonnull Listener listener = Objects.requireNonNull(pListener);
		final List<String> argList = Arrays.asList(Objects.requireNonNull(pArgs));
		final List<String> args = new ArrayList<>(argList);
		final ContextImpl ctx = new ContextImpl(pListener, args);
		while (!args.isEmpty()) {
			final String arg = args.remove(0);
			final String argAndValue;
			ctx.setArg(arg);
			if ("--".equals(arg)) {
				break;
			} else if (arg.startsWith("--")) {
				argAndValue = arg.substring(2);
			} else if (arg.startsWith("-")  ||  arg.startsWith("/")) {
				argAndValue = arg.substring(1);
			} else {
				final String msg = "Invalid argument: " + arg;
				final RuntimeException rte = listener.error(msg);
				if (rte == null) {
					throw new ArgsException(msg);
				} else {
					throw rte;
				}
			}
			final String opt;
			final int offset = argAndValue.indexOf('=');
			if (offset == -1) {
				opt = argAndValue;
			} else {
				opt = argAndValue.substring(0, offset);
			}
			ctx.setName(opt);
			listener.option(ctx, opt);
		}
		if (args.isEmpty()) {
			return null;
		} else {
			return args.toArray(new String[args.size()]);
		}
	}

	/**
	 * Called to parse the command line arguments, typically from within a main method.
	 * @param pListener A listener, which is being notified about options, that are detected.
	 * @param pArgs The command line arguments.
	 * @return Additional arguments, which are following the command line options. (These
	 *   will only be available, if an option "--" is used to terminate the options.)
	 */
	public static String[] parse(Listener pListener, String... pArgs) {
		return new Args().parse(pArgs, pListener);
	}
}
