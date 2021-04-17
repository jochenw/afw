package com.github.jochenw.afw.core.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.cli.Args.Context;
import com.github.jochenw.afw.core.cli.Args.Listener;
import com.github.jochenw.afw.core.util.Exceptions;

/**
 * Abstract base class for deriving Main classes. The implementation follows
 * the assumption, that subclasses should be derivable from a Main class.
 * @param <O> Type of the option bean.
 */
public abstract class ArgsHandler<O extends Object> {
	/**
	 * A registry for supported options.
     * @param <T> Type of the option bean.
	 */
	public interface OptionRegistry<T> {
		/** Called to register a new option with the given handler, and the given option names.
		 * @param pHandler The option handler, which is being invoked, if the given option is
		 *   detected.
		 * @param pOptionNames One or more option names, typically the full option name, and one
		 *   or more abbreviations.
		 */
		public void register(@Nonnull OptionHandler<T> pHandler, @Nonnull String... pOptionNames);
	}
	/** A handler for an option, that has been detected.
     * @param <T> Type of the option bean.
	 */
	@FunctionalInterface
	public interface OptionHandler<T> {
		/** Called to process an option value.
		 * @param pCtx
		 * @param pOptionBean
		 * @param pOptionName The actual option name, as given by the user.
		 * @param pMainOptionName The main option name.
		 * @throws Exception
		 */
		public void accept(Args.Context pCtx, T pOptionBean, String pOptionName, String pMainOptionName) throws Exception;
	}
	/** A registered option
     * @param <T> Type of the option bean.
	 */
	public interface Option<T> extends OptionHandler<T> {
		/** Returns the main option name.
		 * @return The main option name.
		 */
		public String getMainOptionName();
	}

	protected O parse(String[] pArgs) {
		final O o = newOptionBean();
		Args.parse(getListener(o), pArgs);
		return o;
	}

	/** Called to process the command line arguments.
	 * @param pArgs The command line arguments.
	 */
	public void main(String[] pArgs) {
		final O options = parse(pArgs);
		try {
			run(options);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected Args.Listener getListener(O pOptionBean){
		final Map<String,Option<O>> options = new HashMap<>();
		final OptionRegistry<O> registry = new OptionRegistry<O>() {
			@Override
			public void register(@Nonnull OptionHandler<O> pHandler, @Nonnull String... pOptionNames) {
				final OptionHandler<O> handler = Objects.requireNonNull(pHandler, "Handler");
				final String[] optionNames = com.github.jochenw.afw.core.util.Objects.requireAllNonNull(pOptionNames, "Option Names");
				if (optionNames.length == 0) {
					throw new IllegalArgumentException("At least one option name is required.");
				}
				final String mainOptionName = optionNames[0];
				final Option<O> option = new Option<O>() {
					@Override
					public String getMainOptionName() { return mainOptionName; }
					@Override
					public void accept(Context pCtx, O pOptionBean, String pName, String pMainOptionName) throws Exception {
						handler.accept(pCtx, pOptionBean, pName, pMainOptionName);
					}
				};
				for (String optionName : optionNames) {
					options.put(optionName, option);
				}
			}
		};
		registerOptions(registry);
		return new Listener() {
			@Override
			public RuntimeException error(String pMsg) {
				return error(pMsg);
			}

			@Override
			public void option(Context pCtx, String pName) {
				final Option<O> option = options.get(pName);
				if (option == null) {
					throw error("Invalid option: " + pName);
				}
				try {
					option.accept(pCtx, pOptionBean, pName, option.getMainOptionName());
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
		};
	}

	protected abstract O newOptionBean();
	protected abstract RuntimeException error(String pMsg);
	protected void registerOptions(OptionRegistry<O> pRegistry) {
		pRegistry.register((ctx, o, name, mainOptionName) -> { throw error(null); }, "help", "h", "?");
	}
	protected abstract void run(O pOptions) throws Exception;
}
