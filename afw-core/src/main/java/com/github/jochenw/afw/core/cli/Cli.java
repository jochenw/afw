package com.github.jochenw.afw.core.cli;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.core.util.Exceptions;


/** An instance of this class can be used in a command line appplication
 * (command line interface, or cli) to implement a processor in a quick,
 * and precise manner.
 * 
 * The class is based on the assumption, that command line options are
 * transformed into properties of a so-called {@code options bean}. For
 * example, if the cli user specifies an option
 * <pre>-inputFile file.txt</pre>, then there should be a field
 * {@code inputFile} in the options bean. The fields type should be
 * {@link Path}, and the value a {@link Path} object, that points to
 * <pre>file.txt</pre>.
 *
 * For every option, there must be a so-called
 * {@link Option#handler(Functions.FailableBiConsumer) argument handler}.
 * An argument handler is a callback, which is invoked, if an option
 * has been identified, and the option value has been parsed
 * successfully.
 *
 * The argument handler takes two arguments: A context object, which
 * allows the handler a limited level of interaction with the Cli
 * object, and the actual value object (<em>not</em> the value string).
 * Typically, the argument handler would just take the value, and store
 * it into the options bean. The argument handler allows to perform
 * additional tasks, like validation, transformation, or combination.
 *
 * Things you should know about the context object:
 * <ul>
 *  <li>The option name, which is actually used, can be obtained via
 *    {@link Context#getOptName()}. This can be used, for example,
 *    to obtain a slightly different behaviour for a long, and a
 *    short option name.</li>
 *  <li>The context object provides access to the {@link Cli}
 *    object itself via {@link Context#getCli()}.
 *    This can be used, for example, to add additional options,
 *    based on the use of an option.</li>
 *  <li>The context object also provides access to the options bean
 *    via {@link Context#getBean()}.</li>
 *  <li>If the argument handler detects an invalid value, it
 *    can invoke either {@link Context#usage(String)}, or
 *    {@link Context#error(String)}, and throw the returned
 *    exception.
 *    
 *    The difference between these methods is, that the latter
 *    exception will be trapped, and mapped to an invocation of
 *    the <a href="#cli-example-usage-handler">usage handler</a>.</li>
 * </ul>
 * 
 * As a short introduction, we'll provide a small example for
 * 
 * 
 * @param <B> Type of the options bean.
 */
public class Cli<B> {
	/** A context object, which is passed to the
	 * {@link Option#handler(Functions.FailableBiConsumer) argument handler}.
     * @param <B> Type of the option bean.
	 */
	public interface Context<B> {
		/** Returns the {@link Cli}. that's calling the argument handler.
		 * @return The {@link Cli}. that's calling the argument handler.
		 */
		@NonNull Cli<B> getCli();
		/** Returns the options bean, that's being configured by the
		 * {@link Cli}.
		 * @return The options bean, that's being configured by the
		 * {@link Cli}.
		 */
		default @NonNull B getBean() { return getCli().getBean(); }
		/** Returns the option name, which was used to specify
		 * the option value.
		 * @return the option name, which was used to specify
		 * the option value.
		 */
		String getOptName();
		/** Called to create an exception, which triggers a usage message.
		 * @param pMsg The created exceptions message.
		 * @return The created exception.
		 * @see #error(String)
		 * @see #error(String, Throwable)
		 */
		default UsageException usage(String pMsg) { return new UsageException(pMsg); }
		/** Called to create an exception, which is being thrown.
		 * @param pMsg The created exceptions message.
		 * @return The created exception.
		 * @see #error(String, Throwable)
		 * @see #usage(String)
		 */
		default IllegalStateException error(String pMsg) { return new IllegalStateException(pMsg); }
		/** Called to create an exception, which is being thrown.
		 * @param pMsg The created exceptions message.
		 * @param pCause The created exceptions cause, if any, or null.
		 * @return The created exception.
		 * @see #error(String)
		 * @see #usage(String)
		 */
		default IllegalStateException error(String pMsg, Throwable pCause) { return new IllegalStateException(pMsg, pCause); }
	}
	/** An Exception, which may be thrown to trigger a usage message.
	 */
	public static class UsageException extends RuntimeException {
		private static final long serialVersionUID = 1721768041601280483L;

		/** Creates a new instance with the given message, and cause.
		 * @param pMessage The created exceptions message.
		 * @param pCause The created exceptions cause.
		 */
		public UsageException(String pMessage, Throwable pCause) {
			super(pMessage, pCause);
		}

		/** Creates a new instance with the given message, and no cause.
		 * @param pMessage The created exceptions message.
		 */
		public UsageException(String pMessage) {
			super(pMessage);
		}
	}

	private final @NonNull B bean;
	private final Map<@NonNull String,Option<B,?>> optionsByName = new HashMap<>();
	private @Nullable FailableBiConsumer<@NonNull Cli<B>,@NonNull String,?> extraArgsHandler;
	private @Nullable FailableFunction<@Nullable UsageException,@NonNull RuntimeException,?> usageHandler;
	private @Nullable FailableFunction<@NonNull B,@Nullable String,?> validator;

	/** Creates a new instance with the given options bean.
	 * @param pBean The options bean, which will be configured by the
	 *   created instance. The same bean will be returned by
	 *   {@link #parse(String[])}.
	 */
	protected Cli(@NonNull B pBean) {
		bean = pBean;
	}

	/** Returns the bean, which will be configured by the
	 *   created instance.
	 * @return The bean, which will be configured by the
	 *   created instance.
	 */
	public @NonNull B getBean() { return bean; }
	/** Called to register a new option, which has just been created.
	 * @param <O> Type of the options value.
	 * @param <OP> Type of the option, that is being registered.
	 * @param pOption The option, that is being registered.
	 * @return The option, which has been registered
	 *   successfully.
	 */
	protected <O,OP extends Option<B,O>> OP register(@NonNull OP pOption) {
		final Consumer<@NonNull String> registration = (n) -> {
			if (pOption.isRepeatable()) {
				optionsByName.put(n, pOption); // Replace the original, non-repeatable option.
			} else {
				if (optionsByName.put(n, pOption) != null) {
					throw new IllegalArgumentException("Duplicate option name: " + n);
				}
			}
		};
		registration.accept(pOption.getPrimaryName());
		final @NonNull String[] secondaryNames = pOption.getSecondaryNames();
		if (secondaryNames != null) {
			for (@NonNull String name : secondaryNames) {
				registration.accept(name);
			}
		}
		return pOption;
	}

	/** Creates a new instance with the given options bean.
	 * @param pBean The options bean, which will be configured by the
	 *   created instance. The same bean will be returned by
	 *   {@link #parse(String[])}.
	 * @param <B> Type of the options bean.
	 * @return The created instance.
	 */
	public static <B> @NonNull Cli<B> of(@NonNull B pBean) {
		return new Cli<>(pBean);
	}

	/** Creates a new option, which configures a string value.
	 *
	 *
	 * Our {@link Cli} object needs to know about the properties, that are
	 * configurable in the options bean. Let's begin with a property
	 * {@code inputFile}. The command line applications user must
	 * configure it by using a term like "-inputFile file.txt". As a
	 * shorter version, we will allow "-if file.txt" instead.
	 * So, we'll begin with writing
	 * <pre>
     *   cli.stringOption("inputFile", "if").required()
     *      .handler((c,s) -&gt; c.getBean().inputFile = s)
	 * </pre>
	 * 
     * This snippet should be fairly obvious:
     * <ul>
     *   <li>We create a PathOption object by invoking the
     *     {@link Cli#stringOption(String, String...)} method,
     *     supplying the accepted option names "inputFile", and
     *     "if".</li>
     *   <li>We make this a required option by invoking the
     *     {@link Option#required()} on the Option object.
     *     (By doing that, we are making the null value invalid
     *     on the option beans {@code inputFile} property.</li>
     *  <li>We connect the value of the command line option
     *     "inputFile" to the options bean property of the
     *     same name by specifying a argument handler on the
     *     option object. This piece of code is less obvious,
     *     so we'll cover argument handlers in more detail below-</li>
     * </ul>
     * 
     * When using the option bean, then we'll notice a minor
     * inconvenience: The "inputFile" is a plain string. We'd
     * rather have a path object. This can be achieved by changing
     * the above snippet as follows:
     * 
	 * <pre>
     *   cli.pathOption("inputFile", "if").required()
     *      .handler((c,p) -&gt; c.getBean().inputFile = p)
	 * </pre>
     * 
     * That is almost identical, apart from replacing "stringOption"
     * with "pathOption", and changing the type of the "inputField"
     * property in the options bean.
     * 
     * In summary, the most important takeaway here is, that you create a
     * {@link String string} valued option using the method
     * {@link Cli#stringOption(String, String...)}. In other words: How
     * you create the option object, depends on the option values type.
     * For other types, there are also
     * <ol>
     *   <li>{@link Cli#boolOption(String, String...)} (boolean values)</li>
     *   <li>{@link Cli#intOption(String, String...)} (integer values)</li>
     *   <li>{@link Cli#pathOption(String, String...)} (path values)</li>
     *   <li>{@link Cli#urlOption(String, String...)} (URL values)</li>
     *   <li>{@link Cli#enumOption(Class, String, String...)} (Enum values)</li>
     * </ol>
     *
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names, if any.
	 * @return The created option.
	 */
	public StringOption<B> stringOption(@NonNull String pPrimaryName, @NonNull String... pSecondaryNames) {
		return register(new StringOption<>(this, pPrimaryName, pSecondaryNames));
	}

	/** Creates a new option, which configures a boolean value.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names, if any.
	 * @return The created option.
	 */
	public BooleanOption<B> boolOption(@NonNull String pPrimaryName, @NonNull String... pSecondaryNames) {
		return register(new BooleanOption<>(this, pPrimaryName, pSecondaryNames));
	}

	/** Creates a new option, which configures an integer value.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names, if any.
	 * @return The created option.
	 */
	public IntOption<B> intOption(@NonNull String pPrimaryName, @NonNull String... pSecondaryNames) {
		return register(new IntOption<>(this, pPrimaryName, pSecondaryNames));
	}

	/** Creates a new option, which configures a path value.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names, if any.
	 * @return The created option.
	 */
	public PathOption<B> pathOption(@NonNull String pPrimaryName, @NonNull String... pSecondaryNames) {
		return register(new PathOption<>(this, pPrimaryName, pSecondaryNames));
	}

	/** Creates a new option, which configures an URL value.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names, if any.
	 * @return The created option.
	 */
	public UrlOption<B> urlOption(@NonNull String pPrimaryName, @NonNull String... pSecondaryNames) {
		return register(new UrlOption<>(this, pPrimaryName, pSecondaryNames));
	}

	/** Creates a new option, which configures an enum value.
	 * @param pEnumType Type of the enum value.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names, if any.
	 * @return The created option.
	 * @param <E> Type of the enum value.
	 */
	public <E extends Enum<E>> EnumOption<B,E> enumOption(@NonNull Class<E> pEnumType, @NonNull String pPrimaryName,
			                                              @NonNull String... pSecondaryNames) {
		return register(new EnumOption<B,E>(this, pEnumType, pPrimaryName, pSecondaryNames));
	}

	/** An option reference is the specification of an option with the associated
	 * option value.
	 * @param <B> Type of the options bean.
	 * @param <O> Type of the option value.
	 */
	protected static class OptRef<B,O> {
		private final @NonNull String optName;
		private final @NonNull Option<B,O> option;
		private final String optValue;
		/** Creates a new instance with the given option name, option, and option value.
		 * @param pOptName The option name, which has been used. (Either of the
		 *   options primary name, or secondary names.)
		 * @param pOption The option object, which has been referenced by the
		 *   option name.
		 * @param pOptValue The specified option value.
		 */
		public OptRef(@NonNull String pOptName, @NonNull Option<B,O> pOption, String pOptValue) {
			optName = pOptName;
			option = pOption;
			optValue = pOptValue;
		}
		/** Returns the option name, which has been used. (Either of the
		 *   options primary name, or secondary names.)
		 * @return The option name, which has been used. (Either of the
		 *   options primary name, or secondary names.)
		 */
		public @NonNull String getOptName() { return optName; }
		/** Returns the option object, which has been referenced by the
		 *   option name.
		 * @return The option object, which has been referenced by the
		 *   option name.
		 */
		public Option<B, O> getOption() { return option; }
		/** Returns The specified option value.
		 * @return The specified option value.
		 */
		public String getOptValue() { return optValue; }
	}
	/** Called internally to split the given command line into a series of
	 * option references.
	 * @param pArgs The list of command line arguments.
	 * @param pOptConsumer A consumer, which will be invoked for every
	 *   detected option reference.
	 * @throws UsageException A usage error has been detected. The caller
	 * is supposed to invoke the {@link #usageHandler(Functions.FailableFunction)}.
	 */
	protected void parse(@NonNull String[] pArgs, @NonNull Consumer<OptRef<B,?>> pOptConsumer) throws UsageException {
		final List<@NonNull String> args = new ArrayList<>(Arrays.asList(pArgs));
		while (!args.isEmpty()) {
			@SuppressWarnings("null")
			final @NonNull String arg = args.remove(0);
			final @NonNull String opt;
			if (arg.startsWith("--")) {
				@SuppressWarnings("null")
				final @NonNull String op = arg.substring(2);
				opt = op;
			} else if (arg.startsWith("-")) {
				@SuppressWarnings("null")
				final @NonNull String op = arg.substring(1);
				opt = op;
			} else {
				if (extraArgsHandler != null) {
					final @NonNull FailableBiConsumer<@NonNull Cli<B>,@NonNull String,?> handler =
							Reflection.cast(extraArgsHandler);
					Functions.accept(handler, this, arg);
					continue;
				} else {
					throw new UsageException("Unexpected non-option argument: " + arg);
				}
			}
			final @NonNull String optName;
			String optValue;
			final int offset = opt.indexOf('=');
			if (offset == -1) {
				optName = opt;
				optValue = null;
			} else {
				optName = Objects.requireNonNull(opt.substring(0, offset));
				optValue = opt.substring(offset+1);
			}
			final Option<B,?> option = optionsByName.get(optName);
			if (option == null) {
				if ("help".equals(optName) || "h".equals(optName)) {
					throw new UsageException(null);
				}
				throw new UsageException("Invalid option name: " + optName);
			} else {
				if (optValue == null  &&  !option.isNullValueValid()) {
					if (args.isEmpty()) {
						throw new UsageException("Option requires an argument: " + optName);
					} else {
						optValue = (@NonNull String) args.remove(0);
					}
				}
			}
			final OptRef<B,?> optRef = new OptRef<>(opt, option, optValue);
			pOptConsumer.accept(optRef);
		}
	}

	/** Implementation of {@link Context}.
	 * @param <B> Type of the options bean.
	 */
	protected static class ContextImpl<B> implements Context<B> {
		private final @NonNull Cli<B> cli;
		private String optName;

		/** Creates a new instance with the given {@link Cli}.
		 * @param pCli The {@link Cli}, which creates, and
		 * uses this context object.
		 */
		protected ContextImpl(@NonNull Cli<B> pCli) {
			cli = pCli;
		}
		/** Returns the {@link Cli}, which created, and
		 * uses this context object.
		 * @return The {@link Cli}, which created, and
		 * uses this context object.
		 */
		@Override
		public @NonNull Cli<B> getCli() { return cli; }

		@Override
		public @NonNull String getOptName() { return Objects.requireNonNull(optName); }
		/** Sets the current options name.
		 * @param pOptName The current options name.
		 */
		public void setOptName(@NonNull String pOptName) { optName = pOptName; }
	}
	/** Called to split the given command line into a series of
	 * option references, and invoke the argument handlers.
	 * @param pArgs The list of command line arguments.
	 * @throws UsageException A usage error has been detected. The caller
	 * is supposed to invoke the {@link #usageHandler(Functions.FailableFunction)}.
	 * @return The configured options bean.
	 */
	public B parse(@NonNull String... pArgs) {
		final Map<String,Integer> usage = new HashMap<>();
		final ContextImpl<B> ctx = new ContextImpl<>(this);
		final Consumer<OptRef<B,?>> optConsumer = new Consumer<OptRef<B,?>>(){
			@Override
			public void accept(OptRef<B,?> pOptRef) {
				@SuppressWarnings("unchecked")
				final OptRef<B,Object> optRef = (OptRef<B,Object>) pOptRef;
				final String optPrimaryName = optRef.getOption().getPrimaryName();
				final Integer useCount = usage.compute(optPrimaryName, (pn,cnt) -> {
					if (cnt == null) {
						return Integer.valueOf(1);
					} else {
						return Integer.valueOf(cnt.intValue()+1);
					}
				});
				final Option<B,Object> option = optRef.getOption();
				final FailableBiConsumer<Context<B>,Object,?> argHandler = option.getArgHandler();
				if (option.isRepeatable()) {
					final RepeatableOption<B,Object> rptOpt = Reflection.cast(option);
					final Object value = rptOpt.getElementValue(optRef.getOptValue());
					rptOpt.addValue(value);
				} else {
					if (useCount.intValue() > 1) {
						throw ctx.usage("The option " + optRef.getOptName() + " may be used only once.");
					}
					final Object value = option.getValue(optRef.getOptValue());
					ctx.setOptName(optRef.getOptName());
					if (argHandler != null) {
						try {
							argHandler.accept(ctx, value);
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
					}
				}
			}
		};
		try {
			parse(pArgs, optConsumer);
			final FailableFunction<@NonNull B,String,?> validator = getValidator();
			if (validator != null) {
				final String msg = Functions.apply(validator, getBean());
				if (msg != null) {
					throw usage(msg);
				}
			}
		} catch (UsageException ue) {
			if (usageHandler != null) {
				@SuppressWarnings("null")
				final @Nullable FailableFunction<UsageException,RuntimeException,?> handler = usageHandler;
				final RuntimeException rte = Functions.apply(handler, ue);
				if (rte != null) {
					throw rte;
				} else {
					System.exit(1);
					return Objects.fakeNonNull();
				}
			}
			throw ue;
		}
		for (Map.Entry<@NonNull String, Option<B,?>> en : optionsByName.entrySet()) {
			@SuppressWarnings("null")
			final @NonNull String optName = en.getKey();
			@SuppressWarnings("unchecked")
			final @NonNull Option<B,Object> opt = (@NonNull Option<B,Object>) en.getValue();
			if (optName.equals(opt.getPrimaryName())) {
				if (opt.isRequired()) {
					final Integer usageCount = usage.get(optName);
					if (usageCount == null  ||  usageCount.intValue() == 0) {
						final Object value = opt.getValue(null);
						if (value != null) {
							ctx.setOptName(opt.getPrimaryName());
							final FailableBiConsumer<Context<B>,Object,?> argHandler = opt.getArgHandler();
							if (argHandler != null) {
								try {
									argHandler.accept(ctx, value);
								} catch (Throwable t) {
									throw Exceptions.show(t);
								}
							}
						}
						throw ctx.usage("Required option missing: " + optName);
					}
				}
				if (opt.isRepeatable()) {
					final RepeatableOption<B,Object> rptOpt = Reflection.cast(opt);
					final FailableBiConsumer<Context<B>,List<Object>,?> argHandler = rptOpt.getArgHandler();
					if (argHandler != null) {
						ctx.setOptName(optName);
						Functions.accept(argHandler, ctx, rptOpt.getValues());
					}
				}
			}
		}
		return bean;
	}

	/** Returns the handler for additional arguments (other than option references).
	 * @return The handler for additional arguments (other than option references).
	 * @see #extraArgsHandler(Functions.FailableBiConsumer)
	 */
	public @Nullable FailableBiConsumer<@NonNull Cli<B>,@NonNull String,?> getExtraArgsHandler() { return extraArgsHandler; }
	/** Sets the handler for additional arguments (other than option references).
	 * @param pHandler The handler for additional arguments (other than option references).
	 * @return This {@link Cli}.
	 * @see #getExtraArgsHandler()
	 */
	public Cli<B> extraArgsHandler(@NonNull FailableBiConsumer<@NonNull Cli<B>,@NonNull String,?> pHandler) {
		extraArgsHandler = pHandler;
		return this;
	}
	/** Returns the handler for producing a usage message.
	 * @return The handler for producing a usage message.
	 * @see #usageHandler(Functions.FailableFunction)
	 */
	public @Nullable FailableFunction<@Nullable UsageException,@NonNull RuntimeException,?> getUsageHandler() { return usageHandler; }
	/** Sets the handler for reporting a usage message.
	 * 
	 * If the cli users enters invalid input (like an invalid argument value,
	 * an invalid option name, or the like), then the CLI application is
	 * supposed to exit with an error message, printing out a usage text,
	 * that explains the problem.
	 *
     * The {@link Cli cli object} supports this by using a so-called usage
     * handler. The usage handler works as follows:
     *
     * <ul>
     *  <li>The piece of code, that detects the invalid input
     *    (the {@link Cli cli object} itself, the argument
     *    handler, a validator, or whatever) throws a
     *    {@link Cli.UsageException}.</li>
     *  <li>The {@link Cli cli object} catches that
     *    exception, and invokes the
     *    {@link Cli#usageHandler(Functions.FailableFunction)}, passing the
     *    exceptions message as an error message.</li>
     *  <li>The usage handler writes out the usage message,
     *    including the error message (if any), and
     *    returns null, or an exception.</li>
     *  <li> If the usage handler returned an exception,
     *    then that exception is being thrown, terminating
     *    the operation. In the case of the return value
     *    null, the {@link Cli cli} will invoke
     *    {@link System#exit(int)} with a status code of
     *    1.</li>
     * </ul>
     *
     * A typical usage handler would look like this:
     * <pre>
     *   public void usage(UsageException pException) {
     *       final String msg = pException.getMessage();
     *       if (msg != null) {
     *           System.err.println(msg);
     *           System.err.println();
     *       }
     *       System.err.println("Usage: java MyApp &lt;Options&gt;");
     *       System.err.println();
     *       System.err.println("Required options are:");
     *                          ...
     *       System.err.println("Other options are:");
     *                          ...
     *       return null;
     *   }
     * </pre>
     * 
	 * @param pHandler The handler for producing a usage message.
	 * @see #getUsageHandler()
	 * @return This {@link Cli}.
	 */
	public Cli<B> usageHandler(@NonNull FailableFunction<@Nullable UsageException,@NonNull RuntimeException,?> pHandler) {
		usageHandler = pHandler;
		return this;
	}

	/** Sets the bean validator.
	 * 
	 * Sometimes, validating single option values (which is, what the
	 * argument handlers, can do), is not sufficient.For example,
	 * option values may only be valid in a particular combination. In
     * that case, you might specify {@link Cli#validator(Functions.FailableFunction)}.
     * The validator is called after the argument handlers. In other words,
     * what it receives is the fully configured option bean. If it detects,
     * that the option beans configuration is invalid, then it returns a
     * non-null error message, which will then be passed on to the
     * {@link #usageHandler(Functions.FailableFunction)}. The return value null, on the
     * other hand, indicates, that the options bean is valid, and may be
     * returned to the caller, who will use the bean as a configuration
     * for the actual operation.
     *
	 * @param pValidator The bean validator.
	 * @return This {@link Cli}.
	 * @see #getValidator()
	 */
	public Cli<B> validator(FailableFunction<@NonNull B,@Nullable String,?> pValidator) {
		validator = pValidator;
		return this;
	}

	/** Returns the bean validator.
	 * @return The bean validator.
	 * @see #validator(Functions.FailableFunction)
	 */
	public @Nullable FailableFunction<@NonNull B,@Nullable String,?> getValidator() { return validator; }

	/** Called to create an exception, which triggers a usage message.
	 * @param pMsg The created exceptions message.
	 * @return The created exception.
	 * @see #error(String)
	 * @see #error(String, Throwable)
	 */
	public UsageException usage(String pMsg) { return new UsageException(pMsg); }
	/** Called to create an exception, which is being thrown.
	 * @param pMsg The created exceptions message.
	 * @return The created exception.
	 * @see #error(String, Throwable)
	 * @see #usage(String)
	 */
	public IllegalStateException error(String pMsg) { return new IllegalStateException(pMsg); }
	/** Called to create an exception, which is being thrown.
	 * @param pMsg The created exceptions message.
	 * @param pCause The created exceptions cause, if any, or null.
	 * @return The created exception.
	 * @see #error(String)
	 * @see #usage(String)
	 */
	public IllegalStateException error(String pMsg, Throwable pCause) { return new IllegalStateException(pMsg, pCause); }
}
