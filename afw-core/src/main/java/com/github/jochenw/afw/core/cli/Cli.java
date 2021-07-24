package com.github.jochenw.afw.core.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.util.Functions.FailableFunction;


/** Abstract base class for deriving Cli applications.
 * Suggested usage:
 * <pre>
 *   public class MyCli {
 *       public static void main(String[] pArgs) {
 *          final OptionsBean bean = new Cli()
 *              .stringOption("--inputFile", "-if").default("inputFile.xml")
 *                 .handler((c,s) -> { \* Process the value *\ }).end()
 *              .build(new OptionsBean());
 *          run(bean);
 *       }
 *  }
 * </pre>
 * @param <B> Type of the option bean.
 */
public class Cli<B> {
	/** This exception is thrown, if a violation of the usage guidelines is
	 * detected, and a usage message should be written.
	 */
	public static class UsageException extends RuntimeException {
		private static final long serialVersionUID = 1721768041601280483L;

		/** Creates a new instance with the given message, and cause.
		 * @param pMessage The exception message.
		 * @param pCause The exception cause.
		 */
		public UsageException(String pMessage, Throwable pCause) {
			super(pMessage, pCause);
		}

		/** Creates a new instance with the given message, and no cause.
		 * @param pMessage The exception message.
		 */
		public UsageException(String pMessage) {
			super(pMessage);
		}
	}
	/**
	 * Interface of the handler context, that is being passed to
	 * {@link Option#handler(FailableBiConsumer)}.
	 * @param <O> Type of the options bean.
	 */
	public interface Context<O> {
		/**
		 * Returns the option bean, that is being configured.
		 * @return The option bean, that is being configured.
		 */
		public O getBean();
		/** Returns the current options primary name.
		 * @return The current options primary name.
		 */
		public String getOptionName();
		/** Called to indicate a usage problem.
		 * @param pMsg The error message.
		 * @return The exception, that is being thrown.
		 */
		public RuntimeException error(String pMsg);
	}
	/** Builder for an option value.
	 * @param <T> The option value's type.
	 * @param <O> The option beans type.
	 */
	public abstract static class Option<T,O> {
		private final Cli<O> cli;
		private final String primaryName;
		private final String[] secondaryNames;
		private String defaultValue;
		private FailableFunction<T,String,?> validator;
		private FailableBiConsumer<Context<O>,T,?> handler;
		private boolean required, repeatable, present;

		/** Creates a new instance with the given {@link Cli cli}.
		 * @param pCli The {@link Cli cli}, that will be returned
		 * when invoking {@link #end()}.
		 * @param pPrimaryName The options primary name.
		 * @param pSecondaryNames The options secondary names.
		 */
		public Option(Cli<O> pCli, String pPrimaryName, String[] pSecondaryNames) {
			cli = pCli;
			primaryName = pPrimaryName;
			secondaryNames = pSecondaryNames;
		}

		/** Returns the {@link Cli cli}, that created this option.
		 * @return The {@link Cli cli}, that created this option.
		 */
		public Cli<O> end() { return cli; }

		protected abstract T asValue(String pValue);

		/** Sets the validator.
		 * @param pValidator The validator.
		 * @return This option.
		 */
		public Option<T,O> validator(FailableFunction<T,String,?> pValidator) {
			validator = pValidator;
			return this;
		}
		/** Sets the handler.
		 * @param pHandler The handler.
		 * @return This option.
		 */
		public Option<T,O> handler(FailableBiConsumer<Context<O>,T,?> pHandler) {
			handler = pHandler;
			return this;
		}
		/** Sets the default value.
		 * @param pDefaultValue The default value.
		 * @return This option.
		 */
		public Option<T,O> defaultValue(String pDefaultValue) {
			defaultValue = pDefaultValue;
			return this;
		}
		/** Sets, that this option is required. This is equivalent to
		 * <pre>required(true)</pre>.
		 * @return This option.
		 */
		public Option<T,O> required() {
			return required(true);
		}
		/** Sets, whether this option is required.
		 * @param pRequired True, if this option is required, otherwise false.
		 * @return This option.
		 */
		public Option<T,O> required(boolean pRequired) {
			required = pRequired;
			return this;
		}
		/** Sets, that this option is repeatable. This is equivalent to
		 * <pre>repeatable(true)</pre>.
		 * @return This option.
		 */
		public Option<T,O> repeatable() {
			return repeatable(true);
		}
		/** Sets, whether this option is repeatable.
		 * @param pRepeatable True, if this option is repeatable, otherwise false.
		 * @return This option.
		 */
		public Option<T,O> repeatable(boolean pRepeatable) {
			repeatable = pRepeatable;
			return this;
		}
		/** Returns the validator.
		 * @return The validator.
		 */
		public FailableFunction<T,String,?> getValidator() {
			return validator;
		}
		/** Returns the handler.
		 * @return The handler.
		 */
		public FailableBiConsumer<Context<O>,T,?> getHandler() {
			return handler;
		}
		/** Returns, whether this option is required.
		 * @return True, if this option is required.
		 */
		public boolean isRequired() { return required; }
		/** Returns, whether this option is repeatable.
		 * @return True, if this option is repeatable.
		 */
		public boolean isRepeatable() { return repeatable; }
		/** Sets, whether this option has been present.
		 * @param pPresent True, if this option has been present.
		 */
		void setPresent(boolean pPresent) {
			present = pPresent;
		}
		/** Returns, whether this option has been present.
		 * @return True, if this option has been present.
		 */
		boolean isPresent() {
			return present;
		}
		/** Returns this options default value.
		 * @return The default value.
		 */
		public String getDefaultValue() {
			return defaultValue;
		}

		/** Returns the options primary name.
		 * @return The options primary name.
		 */
		public String getPrimaryName() {
			return primaryName;
		}

		/** Returns the options secondary names.
		 * @return The options secndary names.
		 */
		public List<String> getSecondaryNames() {
			if (secondaryNames == null) {
				return Collections.emptyList();
			} else {
				return Arrays.asList(secondaryNames);
			}
		}
	}

	/** Implementation of {@link Option} for booolean values.
	 * @param <O> Type of the option bean.
	 */
	public static class BooleanOption<O> extends Option<Boolean,O> {
		/** Creates a new instance with the given cli.
		 * @param pCli The builder, that creates this option.
		 * @param pPrimaryName The options primary name.
		 * @param pSecondaryNames The options secondary names, if any, or null.
		 */
		public BooleanOption(Cli<O> pCli, String pPrimaryName, String[] pSecondaryNames) {
			super(pCli, pPrimaryName, pSecondaryNames);
		}

		@Override
		protected Boolean asValue(String pValue) {
			return Boolean.valueOf(pValue);
		}
	}

	/** Implementation of {@link Option} for string values.
	 * @param <O> Type of the option bean.
	 */
	public static class StringOption<O> extends Option<String,O> {
		/** Creates a new instance with the given cli.
		 * @param pCli The builder, that creates this option.
		 * @param pPrimaryName The options primary name.
		 * @param pSecondaryNames The options secondary names, if any, or null.
		 */
		public StringOption(Cli<O> pCli, String pPrimaryName, String[] pSecondaryNames) {
			super(pCli, pPrimaryName, pSecondaryNames);
		}

		@Override
		protected String asValue(String pValue) {
			return pValue;
		}
	}

	/** Implementation of {@link Option} for path values.
	 * @param <O> Type of the option bean.
	 */
	public static class PathOption<O> extends Option<Path,O> {
		private boolean existsRequired, fileRequired, directoryRequired;

		/** Creates a new instance with the given cli.
		 * @param pCli The builder, that creates this option.
		 * @param pPrimaryName The options primary name.
		 * @param pSecondaryNames The options secondary names, if any, or null.
		 */
		public PathOption(Cli<O> pCli, String pPrimaryName, String[] pSecondaryNames) {
			super(pCli, pPrimaryName, pSecondaryNames);
		}

		/**
		 * Returns the given value as a Path.
		 */
		@Override
		protected Path asValue(String pValue) {
			return Paths.get(pValue);
		}

		/**
		 * Sets, that the configured path value must be existing.
		 * Equivalent to
		 * <pre>
		 *   existsRequired(true)
		 * </pre>
		 * @return This option builder.
		 */
		public PathOption<O> existsRequired() {
			return existsRequired(true);
		}

		/**
		 * Sets, whether the configured path value must be existing.
		 * @param pExistsRequired True, if the configured path value must be an
		 *   existing file, or directory, otherwise false (default).
		 * @return This option builder.
		 */
		public PathOption<O> existsRequired(boolean pExistsRequired) {
			existsRequired = pExistsRequired;
			return this;
		}

		/** Returns, whether the configured path value must be existing.
		 * @return True, if the configured path value must be an existing file,
		 * or directory, otherwise false (default).
		 */
		public boolean isExistsRequired() {
			return existsRequired;
		}

		/**
		 * Sets, that the configured path value must be a file.
		 * Equivalent to
		 * <pre>
		 *   fileRequired(true)
		 * </pre>
		 * @return This option builder.
		 */
		public PathOption<O> fileRequired() {
			return fileRequired(true);
		}

		/**
		 * Sets, whether the configured path value must be a file.
		 * @param pFileRequired True, if the configured path value must be a
		 * file, or directory, otherwise false (default).
		 * @return This option builder.
		 */
		public PathOption<O> fileRequired(boolean pFileRequired) {
			fileRequired = pFileRequired;
			return this;
		}

		/** Returns, whether the configured path value must be a file.
		 * @return True, if the configured path value must be an file,
		 * or directory, otherwise false (default).
		 */
		public boolean isFileRequired() {
			return fileRequired;
		}

		/**
		 * Sets, whether the configured path value must be a directory.
		 * @param pDirRequired True, if the configured path value must be a
		 * file, or directory, otherwise false (default).
		 * @return This option builder.
		 */
		public PathOption<O> dirRequired(boolean pDirRequired) {
			directoryRequired = pDirRequired;
			return this;
		}

		/** Returns, whether the configured path value must be a file.
		 * @return True, if the configured path value must be an file,
		 * or directory, otherwise false (default).
		 */
		public boolean isDirRequired() {
			return directoryRequired;
		}

		/**
		 * Sets, that the configured path value must be a directory.
		 * Equivalent to
		 * <pre>
		 *   dirRequired(true)
		 * </pre>
		 * @return This option builder.
		 */
		public PathOption<O> dirRequired() {
			return dirRequired(true);
		}

		@Override
		public FailableFunction<Path,String,?> getValidator() {
			final FailableFunction<Path,String,?> validator = super.getValidator();
			if (validator == null  &&  !existsRequired  &&  !fileRequired  &&  !directoryRequired) {
				return null;
			}
			return (p) -> {
				if (existsRequired  &&  !Files.exists(p)) {
					return "Invalid value for option " + getPrimaryName()
						+ ": Expected existing file, or directory, got " + p;
				}
				if (existsRequired  &&  !Files.isRegularFile(p)) {
					return "Invalid value for option " + getPrimaryName()
						+ ": Expected file, got " + p;
				}
				if (directoryRequired  &&  !Files.isDirectory(p)) {
					return "Invalid value for option " + getPrimaryName()
						+ ": Expected directory, got " + p;
				}
				if (validator != null) {
					final String s = validator.apply(p);
					if (s != null) {
						return s;
					}
				}
				return null;
			};
		}
	}

	private final B bean;
	private final Map<String,Option<?,B>> options = new HashMap<>();
	private Function<String,RuntimeException> errorHandler;
	private FailableFunction<B,String,?> validator;

	/** Creates  new instance, which configures the given bean.
	 * @param pBean The options bean, that is being configured.
	 */
	public Cli(B pBean) {
		bean = pBean;
	}

	protected RuntimeException error(String pMsg) {
		if (errorHandler == null) {
			return new UsageException(pMsg);
		} else {
			return errorHandler.apply(pMsg);
		}
	}

	/** Sets the error handler.
	 * @param pErrorHandler The error handler.
	 * @return This {@link Cli cli}.
	 */
	public Cli<B> errorHandler(Function<String,RuntimeException> pErrorHandler) {
		errorHandler = pErrorHandler;
		return this;
	}

	/** Returns the error handler, if any, or null.
	 * @return The error handler.
	 */
	public Function<String,RuntimeException> getErrorHandler() {
		return errorHandler;
	}

	protected <O extends Option<?,B>> O option(O pOption) {
		final Predicate<String> existingOptionTest = (s) -> {
			for (Option<?,B> opt : options.values()) {
				if (s.equals(opt.getPrimaryName())) {
					return true;
				}
				final List<String> secondaryNames = opt.getSecondaryNames();
				for (String sn : secondaryNames) {
					if (s.equals(sn)) {
						return true;
					}
				}
			}
			return false;
		};
		String duplicateName = null;
		if (existingOptionTest.test(pOption.getPrimaryName())) {
			duplicateName = pOption.getPrimaryName();
		}
		if (duplicateName == null) {
			final List<String> secondaryNames = pOption.getSecondaryNames();
			for (String sn : secondaryNames) {
				if (existingOptionTest.test(sn)) {
					duplicateName = sn;
					break;
				}
			}
		}
		if (duplicateName != null) {
			throw new IllegalArgumentException("Duplicate option name: " + duplicateName);
		}
		options.put(pOption.getPrimaryName(), pOption);
		return pOption;
	}

	/** Creates an option builder, that takes a {@link Path} value.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names.
	 * @return The created option builder.
	 */
	public PathOption<B> pathOption(String pPrimaryName, String... pSecondaryNames) {
		return option(new PathOption<>(this, pPrimaryName, pSecondaryNames));
	}

	/** Creates an option builder, that takes a boolean value.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names.
	 * @return The created option builder.
	 */
	public BooleanOption<B> booleanOption(String pPrimaryName, String... pSecondaryNames) {
		return option(new BooleanOption<>(this, pPrimaryName, pSecondaryNames));
	}


	/** Creates an option builder, that takes a {@link String} value.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names.
	 * @return The created option builder.
	 */
	public StringOption<B> stringOption(String pPrimaryName, String... pSecondaryNames) {
		return option(new StringOption<>(this, pPrimaryName, pSecondaryNames));
	}

	
	/** Sets the bean validator.
	 * @param pBeanValidator The bean validator.
	 * @return This Cli.
	 */
	public Cli<B> beanValidator(FailableFunction<B,String,?> pBeanValidator) {
		validator = pBeanValidator;
		return this;
	}

	/** Returns the bean validator.
	 * @return The bean validator, if any, or null.
	 */
	public FailableFunction<B,String,?> beanValidator() {
		return validator;
	}

	/**
	 * Configures the option bean by passing the given option values to
	 * the respective handlers.
	 * @param pArgs The command line arguments, specifying the option values.
	 * @param pBean The bean, which is being configured.
	 * @return The successfully configured bean.
	 */
	public B parse(String[] pArgs) {
		final List<String> args = new ArrayList<String>(Arrays.asList(Objects.requireNonNull(pArgs, "Args")));
		final Map<String,String> optionValues = new HashMap<>();
		while (!args.isEmpty()) {
			final String arg = args.remove(0);
			final String opt;
			if (arg.startsWith("--")) {
				opt = arg.substring(2);
			} else if (arg.startsWith("-")  ||  arg.startsWith("/")) {
				opt = arg.substring(1);
			} else {
				throw new UsageException("Invalid argument: Excepted option reference, got " + arg);
			}
			final String optName, optValue;
			final int offset = opt.indexOf('=');
			if (offset == -1) {
				optName = opt;
				optValue = null;
			} else {
				optName = opt.substring(0, offset);
				optValue = opt.substring(offset+1);
			}
			Option<?,B> option = null;
			for (Option<?,B> o : options.values()) {
				if (optName.equals(o.getPrimaryName())) {
					option = o;
					break;
				}
				if (o.getSecondaryNames() != null) {
					for (String s : o.getSecondaryNames()) {
						if (s.equals(optName)) {
							option = o;
							break;
						}
					}
				}
				if (option != null) {
					break;
				}
			}
			final @Nonnull Option<?,B> optn = Objects.requireNonNull(option);
			if (optionValues.containsKey(optn.getPrimaryName())) {
				throw error("Option " + option.getPrimaryName() + " may be repeated only once.");
			}
			String value = optValue;
			if (value == null) {
				if (args.isEmpty()) {
					if (option instanceof BooleanOption) {
						value = "true";
					}
				} else {
					final String v = args.get(0);
					if (v.startsWith("--")  ||  v.startsWith("-")  ||  v.startsWith("/")) {
						if (option instanceof BooleanOption) {
							value = "true";
						}
					} else {
						value = args.remove(0);
					}
				}
			}
			option.setPresent(true);
			optionValues.put(option.getPrimaryName(), value);
		}
		for (Map.Entry<String,String> en : optionValues.entrySet()) {
			final String optionName = en.getKey();
			final String optionValue = en.getValue();
			@SuppressWarnings("unchecked")
			final Option<Object,B> option = (Option<Object,B>) options.get(optionName);
			final Object value;
			try {
				value = option.asValue(optionValue);
			} catch (Throwable t) {
				throw error("Invalid value for option"
						+ optionName + " (" + optionValue + "): " + t.getMessage());
			}
			final FailableFunction<Object,String,?> validator = option.getValidator();
			if (validator != null) {
				final String errorMsg;
				try {
					errorMsg = validator.apply(value);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
				if (errorMsg != null) {
					throw error(errorMsg);
				}
			}
			final FailableBiConsumer<Context<B>,Object,?> handler = option.getHandler();
			if (handler != null) {
				final Context<B> context = new Context<B>() {
					@Override
					public B getBean() {
						return bean;
					}

					@Override
					public String getOptionName() {
						return optionName;
					}

					@Override
					public RuntimeException error(String pMsg) {
						return Cli.this.error(pMsg);
					}
				};
				try {
					handler.accept(context, option.asValue(optionValue));
				} catch (Throwable e) {
					throw Exceptions.show(e);
				}
			}
		}
		validate(bean);
		return bean;
	}

	protected void validate(B pBean) {
		for (Option<?,B> opt : options.values()) {
			if (opt.isRequired()  &&  !opt.isPresent()) {
				throw error("Required option missing: " + opt.getPrimaryName());
			}
		}
		if (validator != null) {
			final String msg = Functions.apply(validator, pBean);
			if (msg != null) {
				throw error(msg);
			}
		}
	}
}
