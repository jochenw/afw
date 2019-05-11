package com.github.jochenw.afw.cli;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Options {
	private final List<Option<?>> options = new ArrayList<>();

	public static class StringOption extends Option<String> {
		public StringOption() {
			super(String.class);
		}

		@Override
		protected String asValue(String pStrValue) {
			return pStrValue;
		}
	}

	public static class IntOption extends Option<Integer> {
		public IntOption(int pDefaultValue) {
			super(Integer.class);
			setDefaultValue(String.valueOf(pDefaultValue));
		}

		@Override
		protected Integer asValue(String pStrValue) {
			try {
				return Integer.valueOf(pStrValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(pStrValue);
			}
		}

		public int getIntValue() {
			final Integer value = getValue();
			if (value == null) {
				return asValue(getDefaultValue()).intValue();
			} else {
				return value.intValue();
			}
		}
	}

	public static abstract class DiskArtifactOption<O> extends Option<O> {
		private boolean requiredToExist, requiredToBeFile, requiredToBeDirectory;
		
		public boolean isRequiredToExist() {
			return requiredToExist;
		}

		protected void setRequiredToExist(boolean pRequiredToExist) {
			requiredToExist = pRequiredToExist;
		}

		public boolean isRequiredToBeFile() {
			return requiredToBeFile;
		}

		protected void setRequiredToBeFile(boolean pRequiredToBeFile) {
			requiredToBeFile = pRequiredToBeFile;
		}

		public boolean isRequiredToBeDirectory() {
			return requiredToBeDirectory;
		}

		protected void setRequiredToBeDirectory(boolean pRequiredToBeDirectory) {
			requiredToBeDirectory = pRequiredToBeDirectory;
		}

		protected DiskArtifactOption(Class<O> pType) {
			super(pType);
		}
	}

	public static class PathOption extends DiskArtifactOption<Path> {
		public PathOption() {
			super(Path.class);
		}

		@Override
		protected Path asValue(String pStrValue) {
			return Paths.get(pStrValue);
		}
	}

	public static class FileOption extends DiskArtifactOption<File> {
		public FileOption() {
			super(File.class);
		}

		@Override
		protected File asValue(String pStrValue) {
			return new File(pStrValue);
		}
	}

	public static class BooleanOption extends Option<Boolean> {
		public BooleanOption(boolean pDefaultValue) {
			super(Boolean.class);
			setDefaultValue(Boolean.toString(pDefaultValue));
		}

		@Override
		protected Boolean asValue(String pStrValue) {
			return Boolean.valueOf(pStrValue);
		}

		public boolean getBoolValue() {
			final Boolean v = getValue();
			return (v == null ? asValue(getDefaultValue()) : v).booleanValue();
		}
	}

	public OptionBuilder<String> stringOption() {
		return new OptionBuilder<String>(String.class){
			@Override
			public StringOption build() {
				final StringOption option = new StringOption();
				configure(option);
				options.add(option);
				return option;
			}

			@Override
			public Options end() {
				return Options.this;
			}
		};
	}

	public OptionBuilder<Path> pathOption() {
		return new OptionBuilder<Path>(Path.class) {
			@Override
			public PathOption build() {
				final PathOption option = new PathOption();
				configure(option);
				options.add(option);
				return option;
			}

			@Override
			public Options end() {
				return Options.this;
			}
		};
	}

	public OptionBuilder<File> fileOption() {
		return new OptionBuilder<File>(File.class) {
			@Override
			public FileOption build() {
				final FileOption option = new FileOption();
				configure(option);
				options.add(option);
				return option;
			}

			@Override
			public Options end() {
				return Options.this;
			}
		};
	}

	public OptionBuilder<Integer> intOption(int pDefaultValue) {
		return new OptionBuilder<Integer>(Integer.class) {
			@Override
			public Option<Integer> build() {
				final IntOption option = new IntOption(pDefaultValue);
				configure(option);
				options.add(option);
				return option;
			}

			@Override
			public Options end() {
				return Options.this;
			}
		};
	}
	public OptionBuilder<Boolean> booleanOption(boolean pDefaultValue) {
		final OptionBuilder<Boolean> ob = new OptionBuilder<Boolean>(Boolean.class) {
			@Override
			public Option<Boolean> build() {
				final BooleanOption option = new BooleanOption(pDefaultValue);
				configure(option);
				options.add(option);
				return option;
			}

			@Override
			public Options end() {
				return Options.this;
			}
		};
		ob.defaultValue(String.valueOf(pDefaultValue));
		return ob;
	}
	protected <O> Option<O> getOption(String pName) throws NoSuchElementException {
		for (Option<?> opt : options) {
			for (String n : opt.getNames()) {
				if (n.equals(pName)) {
					@SuppressWarnings("unchecked")
					final Option<O> o = (Option<O>) opt;
					return o;
				}
			}
		}
		return null;
	}
	protected <O> Option<O> requireOption(String pName) throws NoSuchElementException {
		final Option<O> o = getOption(pName);
		if (o == null) {
			throw new NoSuchElementException("No such option: " + pName);
		}
		return null;
	}
	
	public interface Result {
		public @Nullable String getStrValue(@Nonnull Option<?> pOption);
		public @Nullable String getStrValue(@Nonnull String pOptionName) throws NoSuchElementException;
		public @Nullable <O> O getValue(@Nonnull Option<O> pOption);
		public @Nullable <O> O getValue(@Nonnull String pOptionName) throws NoSuchElementException;
		public int getIntValue(@Nonnull Option<? extends Number> pOption);
		public int getIntValue(@Nonnull String pOptionName) throws ClassCastException, NoSuchElementException;
		public boolean getBoolValue(@Nonnull Option<Boolean> pOption);
		public boolean getBoolValue(@Nonnull String pOptionName) throws ClassCastException, NoSuchElementException;
		public @Nullable String[] getRemainingArgs();
	}
	public static class DefaultResult implements Result {
		private final Options options;
		private final String[] remainingArgs;
		protected DefaultResult(Options pOptions, List<String> pRemainingArgs) {
			options = pOptions;
			if (pRemainingArgs == null  ||  pRemainingArgs.isEmpty()) {
				remainingArgs = null;
			} else {
				remainingArgs = pRemainingArgs.toArray(new String[pRemainingArgs.size()]);
			}
		}
		@Override
		public String getStrValue(Option<?> pOption) {
			return pOption.getStrValue();
		}
		@Override
		public String getStrValue(String pOptionName) throws NoSuchElementException {
			return getStrValue(options.requireOption(pOptionName));
		}
		@Override
		public <O> O getValue(Option<O> pOption) {
			if (pOption.isGiven()) {
				final O o = pOption.getValue();
				return o;
			} else {
				final String defaultValue = pOption.getDefaultValue();
				if (defaultValue == null) {
					return null;
				} else {
					return pOption.asValue(defaultValue);
				}
			}
		}
		@Override
		public <O> O getValue(String pOptionName) throws NoSuchElementException {
			return getValue(options.requireOption(pOptionName));
		}
		@Override
		public int getIntValue(@Nonnull Option<? extends Number> pOption) {
			if (pOption instanceof IntOption) {
				return ((IntOption) pOption).getIntValue();
			}
			final Number value = getValue(pOption);
			if (value == null) {
				final String defaultValue = pOption.getDefaultValue();
				if (defaultValue == null) {
					throw new NullPointerException("Option value has not been given, and no default value provided: " + pOption.getNames()[0]);
				} else {
					return pOption.asValue(defaultValue).intValue();
				}
			} else {
				return value.intValue();
			}
		}
		@Override
		public int getIntValue(String pOptionName) throws ClassCastException, NoSuchElementException {
			final Option<?> opt = options.requireOption(pOptionName);
			if (!Number.class.isAssignableFrom(opt.getType())) {
				throw new IllegalArgumentException("Invalid type for option " + pOptionName + ": Expected Number, got "
						+ opt.getType().getName());
			}
			@SuppressWarnings("unchecked")
			final Option<Number> numOpt = (Option<Number>) opt;
			return getIntValue(numOpt);
		}
		@Override
		public boolean getBoolValue(@Nonnull Option<Boolean> pOption) {
			if (pOption instanceof BooleanOption) {
				return ((BooleanOption) pOption).getBoolValue();
			}
			final Boolean value = getValue(pOption);
			if (value == null) {
				final String defaultValue = pOption.getDefaultValue();
				if (defaultValue == null) {
					throw new NullPointerException("Option value has not been given, and no default value provided: " + pOption.getNames()[0]);
				} else {
					return pOption.asValue(defaultValue).booleanValue();
				}
			} else {
				return value.booleanValue();
			}
		}
		@Override
		public boolean getBoolValue(String pOptionName) throws ClassCastException, NoSuchElementException {
			final Option<?> opt = options.requireOption(pOptionName);
			if (!Boolean.class.isAssignableFrom(opt.getType())) {
				throw new IllegalArgumentException("Invalid type for option " + pOptionName + ": Expected Boolean, got "
						+ opt.getType().getName());
			}
			@SuppressWarnings("unchecked")
			final Option<Boolean> boolOpt = (Option<Boolean>) opt;
			return getBoolValue(boolOpt);
		}
		@Override
		public String[] getRemainingArgs() {
			return remainingArgs;
		}
	}
	public Result process(String[] pArgs, Function<String,RuntimeException> pErrorHandler) {
		final List<String> args = new ArrayList<>(Arrays.asList(pArgs));
		final List<String> remainingArgs = new ArrayList<>();
		while (!args.isEmpty()) {
			final String arg = args.remove(0);
			final int argOffset;
			if (arg.equals("--")) {
				remainingArgs.addAll(args);
				return new DefaultResult(this, remainingArgs);
			} if (arg.startsWith("--")) {
				argOffset = 2;
			} else if (arg.startsWith("-")  ||  arg.startsWith("/")) {
				argOffset = 1;
			} else {
				throw pErrorHandler.apply("Invalid option: " + arg);
			}
			String opt = arg.substring(argOffset);
			final int valueOffset = opt.indexOf('=');
			final String value;
			if (valueOffset >= 0) {
				value = opt.substring(valueOffset+1);
				opt = opt.substring(0, valueOffset);
			} else {
				value = null;
			}
			
		}
		return new DefaultResult(this, Collections.emptyList());
	}
}
