package com.github.jochenw.afw.core.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Lists;
import com.github.jochenw.afw.core.util.Objects;


/** Abstract base class for deriving Cli applications.
 * Suggested usage:
 * <pre>
 *   public class MyCli {
 *       public static void main(String[] pArgs) {
 *          final OptionsBean bean = new Cli()
 *              .stringOption("--inputFile", "-if").default("inputFile.xml")
 *                 .handler((c,s) -&gt;{ \* Process the value *\ }).end()
 *              .build(new OptionsBean());
 *          run(bean);
 *       }
 *  }
 * </pre>
 * @param <B> Type of the option bean.
 */
public class Cli<B> extends OptionBuilder<B> {
	/** This exception indicates, that the submitted command line is invalid.
	 * Typically, this will result in a usage message being written.
	 */
	public static class UsageException extends RuntimeException {
		private static final long serialVersionUID = 5606503011947221498L;

		/** Creates a new instance with the given message, and cause.
		 * @param pMessage The error message
		 * @param pCause The error cause, if any, or null.
		 */
		public UsageException(@NonNull String pMessage, @Nullable Throwable pCause) {
			super(pMessage, pCause);
		}

		/** Creates a new instance with the given message, and no cause.
		 * @param pMessage The error message
		 */
		public UsageException(@NonNull String pMessage) {
			super(pMessage);
		}
	}

	/** This context object is submitted to the option value handler.
	 * @param <O> The option bean type.
	 */
	public static abstract class Context<O> extends OptionBuilder<O> {
		private final O bean;
		private final @NonNull Cli<O> cli;
		private String optionName, primaryOptionName;
		private final @NonNull Consumer<Option<?,O>> optionAdder;
		private final Predicate<String> optionNameChecker;

		/** Creates a new instance with the given bean.
		 * @param pBean The bean, which is being configured by the {@link Cli}.
		 * @param pCli The {@link Cli}, which is currently configuring the option bean.
		 * @param pOptionAdder A handler for options, that are being created by this context.
		 * @param pOptionNameChecker The implementation of
		 * {@link OptionBuilder#isOptionNamePresent(String)}.
		 */
		public Context(O pBean, @NonNull Cli<O> pCli,
				       @NonNull Consumer<Option<?,O>> pOptionAdder,
				       Predicate<String> pOptionNameChecker) {
			optionAdder = pOptionAdder;
			optionNameChecker = pOptionNameChecker;
			bean = pBean;
			cli = pCli;
		}

		/** Creates a new {@link UsageException} with the given error message,
		 * and no cause.
		 * @param pMsg The error message.
		 * @return The created exception.
		 */
		public RuntimeException error(@NonNull String pMsg) {
			return new UsageException(pMsg);
		}
		/** Creates a new {@link UsageException} with the given error message,
		 * and cause.
		 * @param pMsg The error message.
		 * @param pCause The error cause, if any, or null.
		 * @return The created exception.
		 */
		public RuntimeException error(@NonNull String pMsg, @Nullable Throwable pCause) {
			return new UsageException(pMsg, pCause);
		}

		/** Returns the option bean, that is being configured.
		 * @return The option bean, which is being configured.
		 */
		public O getBean() { return bean; }

		/** Returns the option bean, that is being configured.
		 * @return The option bean, which is being configured.
		 */
		public O bean() { return bean; }

		/** Sets the name (primary, or secondary) of the option,
		 * which is currently being configured.
		 * @param pOptionName Name (primary, or secondary) of the option,
		 *   which is currently being configured.
		 */
		void setOptionNames(@NonNull String pOptionName, @NonNull String pPrimaryOptionName) {
			optionName = pOptionName;
			primaryOptionName = pPrimaryOptionName;
		}

		/** Returns the name (primary, or secondary) of the option,
		 * which is currently being configured.
		 * @return Name (primary, or secondary) of the option,
		 *   which is currently being configured.
		 */
		public @NonNull String getOptionName() { return Objects.requireNonNull(optionName); }

		/** Returns the primary name of the option,
		 * which is currently being configured.
		 * @return Name (primary, or secondary) of the option,
		 *   which is currently being configured.
		 */
		public @NonNull String getPrimaryOptionName() {
			return Objects.requireNonNull(primaryOptionName);
		}

		@Override
		protected @NonNull Cli<O> getCli() {
			return cli;
		}

		@Override
		protected void optionAdded(Option<?, O> pOption) {
			optionAdder.accept(pOption);
		}

		@Override
		protected boolean isOptionNamePresent(String pOptionName) {
			return optionNameChecker.test(pOptionName);
		}

		/** Sets the handler for extra arguments without context.
		 * @param pExtraArgsHandler The handler for extra arguments without context.
		 * @return This {@link Cli}.
		 * @see #extraArgsHandler(Functions.FailableConsumer)
		 */
		public Context<O> extraArgsHandler(@NonNull FailableConsumer<@NonNull String,?> pExtraArgsHandler) {
			getCli().extraArgsHandler(pExtraArgsHandler);
			return this;
		}

		/** Sets the handler for extra arguments with context.
		 * @param pExtraArgsHandler The handler for extra arguments with context.
		 * @return This {@link Cli}.
		 * @see #extraArgsHandler(Functions.FailableConsumer)
		 */
		public Context<O> extraArgsHandler(@NonNull FailableBiConsumer<@NonNull Context<O>,@NonNull String,?> pExtraArgsHandler) {
			getCli().extraArgsHandler(pExtraArgsHandler);
			return this;
		}
	}
	private final B bean;
	private final List<@NonNull Option<?,B>> options = new ArrayList<>();
	private FailableConsumer<@NonNull String,?> extraArgsHandler;
	private FailableBiConsumer<@NonNull Context<B>,@NonNull String,?> extraArgsHandlerCtx;
	private Function<B,String> validator;
	private Function<String,RuntimeException> usageHandler;

	/** Creates a new instance, which configures the given bean.
	 * @param pBean The instances bean, that is being configured.
	 */
	public Cli(B pBean) {
		bean = pBean;
	}

	/** Creates a new instance, which configures the given bean.
	 * @param <T> The instances bean type.
	 * @param pBean The instances bean, that is being configured.
	 * @return The created instance.
	 */
	public static <T> @NonNull Cli<T> of(@NonNull T pBean) {
		return new Cli<T>(pBean);
	}

	/** Sets the handler for extra arguments without context.
	 * @param pExtraArgsHandler The handler for extra arguments without context.
	 * @return This {@link Cli}.
	 * @see #extraArgsHandler(Functions.FailableBiConsumer)
	 */
	public Cli<B> extraArgsHandler(@NonNull FailableConsumer<@NonNull String,?> pExtraArgsHandler) {
		extraArgsHandler = pExtraArgsHandler;
		extraArgsHandlerCtx = null;
		return this;
	}

	/** Sets the handler for extra arguments with context.
	 * @param pExtraArgsHandler The handler for extra arguments with context.
	 * @return This {@link Cli}.
	 * @see #extraArgsHandler(Functions.FailableConsumer)
	 */
	public Cli<B> extraArgsHandler(@NonNull FailableBiConsumer<@NonNull Context<B>,@NonNull String,?> pExtraArgsHandler) {
		extraArgsHandlerCtx = pExtraArgsHandler;
		extraArgsHandler = null;
		return this;
	}
	
	/** Returns the handler for extra arguments without context.
	 * @return The handler for extra arguments without context.
	 */
	public FailableConsumer<@NonNull String,?> getExtraArgsHandler() {
		return extraArgsHandler;
	}

	/** Returns the handler for extra arguments without context.
	 * @return The handler for extra arguments without context.
	 */
	public FailableBiConsumer<@NonNull Context<B>, @NonNull String, ?>
	        getExtraArgsHandlerCtx() {
		return extraArgsHandlerCtx;
	}

	/** Parses the given command line, thereby configuring the bean.
	 * @param pArgs the command line, which is being parsed.
	 * @return The configured bean.
	 */
	public B parse(String [] pArgs) {
		try {
			return doParse(pArgs);
		} catch (UsageException e) {
			if (usageHandler != null) {
				throw usageHandler.apply(e.getMessage());
			} else {
				throw e;
			}
		}
	}

	/** Actual implementation of {@link #parse(String[])}.
	 * @param pArgs the command line, which is being parsed.
	 * @return The configured bean.
	 * @throws UsageException A usage error has been detected.
	 */
	protected B doParse(String [] pArgs) throws UsageException {
		final Map<@NonNull String,OptionReference<B,?>> optionsByPrimaryName = new HashMap<>();
		final Map<@NonNull String,String> primaryNamesByName = new HashMap<>();
		final Function<Option<?,B>,@Nullable String> optionAdder = opt -> {
			final OptionReference<B,?> ref = new OptionReference<>(opt);
			if (optionsByPrimaryName.put(opt.getPrimaryName(), ref) != null) {
				return opt.getPrimaryName();
			}
			if (primaryNamesByName.put(opt.getPrimaryName(), opt.getPrimaryName()) != null) {
				return opt.getPrimaryName();
			}
			final @NonNull String @Nullable [] secondaryNames = opt.getSecondaryNames();
			if (secondaryNames != null) {
				for (@NonNull String secondaryName : secondaryNames) {
					if (primaryNamesByName.put(secondaryName, opt.getPrimaryName()) != null) {
						return secondaryName;
					}
				}
			}
			return null;
		};
		for (Option<?,B> opt : options) {
			optionAdder.apply(opt);
		}
		final @NonNull List<String> args = Objects.cast(Lists.asList(pArgs));
		final Consumer<Option<?,B>> optionHandler = opt -> {
			final String existingOptionName = optionAdder.apply(opt);
			if (existingOptionName != null) {
				throw new IllegalStateException("Duplicate option name: " + existingOptionName);
			}
		};
		final Predicate<String> optionNameChecker = on -> {
			return primaryNamesByName.containsKey(on);
		};
		final @NonNull Context<B> ctx = new Context<B>(bean, this, optionHandler, optionNameChecker) {
			@Override
			protected boolean isOptionNamePresent(String pOptionName) {
				return primaryNamesByName.containsKey(pOptionName);
			}
		};
		while (!args.isEmpty()) {
			final String arg = args.remove(0);
			if (arg == null) {
				throw new NullPointerException("Unexpected null argument");
			}
			final String opt;
			if ("--".equals(arg)) {
				// Terminate args processing.
				args.forEach((s) -> Cli.this.handleExtraArg(ctx, Objects.requireNonNull(s)));
				break;
			}
			if (arg.startsWith("--")) {
				opt = Objects.requireNonNull(arg.substring(2));
			} else if (arg.startsWith("-")) {
				opt = Objects.requireNonNull(arg.substring(1));
			} else {
				handleExtraArg(ctx, arg);
				continue;
			}
			if (usageHandler != null) {
				if (("help".equals(opt)  ||  "h".equals(opt))  &&  !primaryNamesByName.containsKey(opt)) {
					throw usageHandler.apply(null);
				}
			}
			final int offsetAssignment = opt.indexOf('=');
			final String optName;
			String optValue;
			if (offsetAssignment == -1) {
				optName = opt;
				if (args.isEmpty()) {
					optValue = null;
				} else {
					optValue = args.remove(0);
				}
			} else {
				optName = opt.substring(0, offsetAssignment);
				optValue = opt.substring(offsetAssignment+1);
			}
			final String optPrimaryName = primaryNamesByName.get(optName);
			if (optPrimaryName == null) {
				throw new UsageException("Unknown option: " + optName);
			}
			final OptionReference<B,?> ref = optionsByPrimaryName.get(optPrimaryName);
			if (ref == null) {
				throw new UsageException("Invalid option " + optName);
			}
			final Option<?,B> option = ref.option;
			if (optValue == null) {
				optValue = ref.option.getDefaultValue();
				if (optValue == null) {
					throw new UsageException("Option " + optName + " requires an argument.");
				}
			}
			if (option.isRepeatable()) {
				if (ref.values == null) {
					ref.values = new ArrayList<>();
				}
				ref.values.add(optValue);
			} else {
				if (ref.value != null) {
					throw new UsageException("Option " + optName + " may be used only once.");
				}
				ref.value = optValue;
			}
			ref.hasBeenUsed = true;
		}
		final List<Runnable> handlerCalls = new ArrayList<>();
		for (OptionReference<B,?> ref : optionsByPrimaryName.values()) {
			final Object value = validate(ref);
			final FailableBiConsumer<Context<B>, Object, ?> handler = Objects.cast(ref.option.getHandler());
			if (handler != null  &&  value != null) {
				handlerCalls.add(() -> Functions.accept(handler, ctx, value));
			}
		}
		if (validator != null) {
			final String errorMessage = validator.apply(bean);
			if (errorMessage != null) {
				throw new UsageException(errorMessage);
			}
		}
		handlerCalls.forEach(r -> r.run());
		return bean;
	}

	/** Handles extra arguments.
	 * @param pCtx The handler context.
	 * @param pArg The extra argument.
	 */
	protected void handleExtraArg(@NonNull Context<B> pCtx, @NonNull String pArg) {
		if (extraArgsHandler == null) {
			if (extraArgsHandlerCtx == null) {
				throw new UsageException("Expected option, got extra argument " + pArg);
			} else {
				Functions.accept(extraArgsHandlerCtx, pCtx, pArg);
			}
		} else {
			Functions.accept(extraArgsHandler, pArg);
		}
	}

	/** Converts the option reference into the actual option value.
	 * @param pOptionRef The option reference, which is being specified.
	 * @return The converted option value, or null, if no value has
	 *   been specified.
	 */
	protected Object validate(OptionReference<B,?> pOptionRef) {
		final Option<?,B> opt = pOptionRef.option;
		if (opt.isRequired()  &&  !pOptionRef.hasBeenUsed) {
			throw new UsageException("Required option missing: " + opt.getPrimaryName());
		}
		if (opt.isRepeatable()) {
			if (pOptionRef.values != null) {
				final List<Object> values = new ArrayList<>(pOptionRef.values.size());
				for (String v : pOptionRef.values) {
					try {
						values.add(opt.getValue(v));
					} catch (Throwable t) {
						throw new UsageException("Invalid value for option " + opt.getPrimaryName() + ": " + v);
					}
				}
				return values;
			}
			return null;
		} else {
			String value = pOptionRef.value;
			if (pOptionRef.hasBeenUsed) {
				if (value == null) {
					value = opt.getDefaultValue();
				}
			} else {
				return null;
			}
			if (value == null) {
				return null;
			} else {
				try {
					return opt.getValue(value);
				} catch (Throwable t) {
					throw new UsageException("Invalid value for option " + opt.getPrimaryName() + ": " + value);
				}
			}
		}
	}

	/** An object, which provides information about an option,
	 * that has been used.
	 * @param <O> Type of the options bean.
	 * @param <T> Type of the options value.
	 */
	public static class OptionReference<O,T> {
		final Option<T,O> option;
		String value;
		List<@NonNull String> values;
		boolean hasBeenUsed;

		/** Creates a new instance as a reference to the given option.
		 * @param pOption The referenced option.
		 */
		public OptionReference(Option<T,O> pOption) {
			option = pOption;
		}
	}

	@Override
	protected @NonNull Cli<B> getCli() {
		return this;
	}

	@Override
	protected void optionAdded(Option<?, B> pOption) {
		options.add(Objects.requireNonNull(pOption));
	}

	/** Specifies a bean validator. The bean validator is a function, which takes
	 * the configured bean as input, and returns either null (The bean is valid),
	 * or an error message (The bean is invalid.)
	 * @param pValidator The bean validator, if any, or null.
	 * @return This {@link Cli}.
	 */
	public @NonNull Cli<B> validator(Function<B,String> pValidator) {
		validator = pValidator;
		return this;
	}

	/** Returns the bean validator. The bean validator is a function, which takes
	 * the configured bean as input, and returns either null (The bean is valid),
	 * or an error message (The bean is invalid.)
	 * @return The bean validator, if any, or null.
	 */
	public Function<B,String> getValidator() {
		return validator;
	}

	/** Specifies a usage handler. The usage handler is a function, which
	 * takes a usage error message as input, displays a usage description,
	 * and returns a {@link RuntimeException}, which is finally thrown to
	 * terminate command line processing.
	 * @param pUsageHandler The usage handler, if any, or null.
	 * @return This {@link Cli}.
	 */
	public @NonNull Cli<B> usageHandler(Function<String,RuntimeException> pUsageHandler) {
		usageHandler = pUsageHandler;
		return this;
	}

	/** Returns the usage handler. The usage handler is a function, which
	 * takes a usage error message as input, displays a usage description,
	 * and returns a {@link RuntimeException}, which is finally thrown to
	 * terminate command line processing.
	 * @return The usage handler, if any, or null.
	 */
	public Function<String,RuntimeException> getUsageHandler() {
		return usageHandler;
	}

	@Override
	protected boolean isOptionNamePresent(String pOptionName) {
		for (Option<?,B> opt : options) {
			if (opt.getPrimaryName().equals(pOptionName)) {
				return true;
			}
			final String[] secondaryNames = opt.getSecondaryNames();
			if (secondaryNames != null) {
				@SuppressWarnings("null")
				final @NonNull String @NonNull [] secNames = (String @NonNull []) secondaryNames;
				for (@NonNull String on : secNames) {
					if (on.equals(pOptionName)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
