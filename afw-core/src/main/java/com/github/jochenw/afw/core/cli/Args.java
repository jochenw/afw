/**
 * 
 */
package com.github.jochenw.afw.core.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Objects;

/** A parser for command line arguments.
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

	protected static class ContextImpl implements Context {
		private final @Nonnull Listener listener;
		private final @Nonnull List<String> args;
		private String arg;
		private String name;

		public ContextImpl(@Nonnull Listener pListener, @Nonnull List<String> pArgs) {
			args = pArgs;
			listener = pListener;
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
			return !args.isEmpty();
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
