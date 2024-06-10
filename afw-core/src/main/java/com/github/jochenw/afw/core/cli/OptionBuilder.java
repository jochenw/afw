package com.github.jochenw.afw.core.cli;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;

/** A builder object for creating options.
 * @param <O> The option bean type.
 */
public abstract class OptionBuilder<O> {
	/** Returns the {@link Cli}, which is currently configuring
	 * the options bean.
	 * @return The {@link Cli}, which is currently configuring
	 * the options bean.
	 */
	protected abstract @NonNull Cli<O> getCli();
	/** Called to indicate, that a new option has been created,
	 * and configured.
	 * @param pOption The created, and configured option.
	 */
	protected abstract void optionAdded(Option<?,O> pOption);
	/** Called to check, whether the given option name is already
	 * in use.
	 * @param pOptionName The option name, which is being checked.
	 * @return True, if the given option name is already in use.
	 */
	protected abstract boolean isOptionNamePresent(String pOptionName);

	/** Creates a new instance with the given option adder.
	 */
	protected OptionBuilder() {
	}

	/** Creates a new string option.
	 * @param pPrimaryName The primary option name.
	 * @param pSecondaryNames The secondary option names.
	 * @return The created string option.
	 */
	public StringOption<O> stringOption(@NonNull String pPrimaryName,
			                            @NonNull String... pSecondaryNames) {
		checkOptionNames(pPrimaryName, pSecondaryNames);
		return new StringOption<>(getCli(), this::optionAdded, pPrimaryName, pSecondaryNames);
	}

	/** Creates a new path option.
	 * @param pPrimaryName The primary option name.
	 * @param pSecondaryNames The secondary option names.
	 * @return The created string option.
	 */
	public PathOption<O> pathOption(@NonNull String pPrimaryName,
			                        @NonNull String... pSecondaryNames) {
		checkOptionNames(pPrimaryName, pSecondaryNames);
		return new PathOption<>(getCli(), this::optionAdded, pPrimaryName, pSecondaryNames);
	}

	/** Creates a new boolean option.
	 * @param pPrimaryName The primary option name.
	 * @param pSecondaryNames The secondary option names.
	 * @return The created integer option.
	 */
	public BooleanOption<O> boolOption(@NonNull String pPrimaryName,
			                           @NonNull String... pSecondaryNames) {
		checkOptionNames(pPrimaryName, pSecondaryNames);
		return new BooleanOption<>(getCli(), this::optionAdded, pPrimaryName, pSecondaryNames);
	}

	/** Creates a new integer option.
	 * @param pPrimaryName The primary option name.
	 * @param pSecondaryNames The secondary option names.
	 * @return The created integer option.
	 */
	public IntOption<O> intOption(@NonNull String pPrimaryName,
                                  @NonNull String... pSecondaryNames) {
		checkOptionNames(pPrimaryName, pSecondaryNames);
		return new IntOption<>(getCli(), this::optionAdded, pPrimaryName, pSecondaryNames);
	}

	/** Creates a new long option.
	 * @param pPrimaryName The primary option name.
	 * @param pSecondaryNames The secondary option names.
	 * @return The created integer option.
	 */
	public LongOption<O> longOption(@NonNull String pPrimaryName,
                                    @NonNull String... pSecondaryNames) {
		checkOptionNames(pPrimaryName, pSecondaryNames);
		return new LongOption<>(getCli(), this::optionAdded, pPrimaryName, pSecondaryNames);
	}

	/** Checks, whether either of the given option names is already present.
	 * @param pPrimaryName The primary option name.
	 * @param pSecondaryNames The secondary option names.
	 */
	protected void checkOptionNames(@NonNull String pPrimaryName,
            						@NonNull String... pSecondaryNames) {
		final Set<String> optionNames = new HashSet<>(); 
		final Consumer<String> check = on -> {
			if (optionNames.contains(on)  ||  isOptionNamePresent(on)) {
				throw new IllegalArgumentException("Duplicate option name: " + on);
			}
			optionNames.add(on);
		};
		check.accept(pPrimaryName);
		if (pSecondaryNames != null) {
			for (String on : pSecondaryNames) {
				check.accept(on);
			}
		}
	}
}