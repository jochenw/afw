package com.github.jochenw.afw.core.cli;

import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.cli.Cli.Context;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;

/** Builder for an option value.
 * @param <T> The option value's type.
 * @param <O> The option beans type.
 */
public abstract class Option<T,O> {
	private final @NonNull Consumer<Option<?,O>> endHandler;
	private final @NonNull Cli<O> cli;
	private final @NonNull String primaryName;
	private final @NonNull String @Nullable [] secondaryNames;
	private @Nullable String defaultValue;
	private boolean required;
	private FailableBiConsumer<Context<O>,T,?> handler;

	/** Creates a new instance with the given {@link Cli},
	 * and {@code end handler}.
	 * @param pCli The {@link Cli}, which is creating this option.
	 * @param pEndHandler The {@code end handler}, which is being
	 *   invoked upon invocation of {@link #end()}.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names.
	 */
	protected Option(@NonNull Cli<O> pCli, @NonNull Consumer<Option<?,O>> pEndHandler,
			         @NonNull String pPrimaryName, @NonNull String @Nullable [] pSecondaryNames) {
		cli = pCli;
		endHandler = pEndHandler;
		primaryName = pPrimaryName;
		secondaryNames = pSecondaryNames;
	}

	/** Returns the options primary name.
	 * @return The options primary name.
	 */
	public @NonNull String getPrimaryName() {
		return primaryName;
	}

	/** Returns the options seondary names.
	 * @return The options secondary names.
	 */
	public @NonNull String @Nullable [] getSecondaryNames() {
		return secondaryNames;
	}

	/** Converts the options string value into the actual
	 * option value.
	 * @param pStrValue The string value, which is being converted.
	 * @return The converted value.
	 */
	public abstract @NonNull T getValue(@NonNull String pStrValue);

	/** Terminates configuration of the option, returning the
	 * {@link Cli}, that created this option, permitting
	 * continued configuration of the {@link Cli}.
	 * @return The {@link Cli}, that created this option.
	 */
	public @NonNull Cli<O> end() {
		endHandler.accept(this);
		return cli;
	}

	/** Declares, that this is a required option.
	 * @return This option.
	 */
	public @NonNull Option<T,O> required() {
		this.required = true;
		return this;
	}

	/** Returns, whether this is a required option.
	 * @return True, if this option is required, otherwise false.
	 */
	public boolean isRequired() {
		return required;
	}

	/** Declares, that this is a required option.
	 * @return This option.
	 */
	public @NonNull Option<List<T>,O> repeatable() {
		return new ListOption<T,O>(this);
	}

	/** Returns, whether this option is repeatable.
	 * @return True, iif this option is repeatable, otherwise false.
	 */
	public boolean isRepeatable() { return false; }

	/** Declares this options default value.
	 * @param pDefaultValue This options default value.
	 * @return This option.
	 */
	public @NonNull Option<T,O> defaultValue(@NonNull String pDefaultValue) {
		defaultValue = pDefaultValue;
		return this;
	}

	/** Returns this options default value.
	 * @return The options default value.
	 */
	public @Nullable String getDefaultValue() {
		return defaultValue;
	}

	@NonNull Consumer<Option<?, O>> getEndHandler() {
		return endHandler;
	}

	@NonNull Cli<O> getCli() {
		return cli;
	}

	/** Returns, whether a non-null default value has been specified for this option.
	 * @return True, if a non-null default value has been specified for this option.
	 */
	public boolean hasDefaultValue() {
		return defaultValue != null;
	}

	/** Called to specify the options value handler.
	 * @param pHandler The options value handler.
	 * @return This option.
	 */
	public @NonNull Option<T,O> handler(FailableBiConsumer<Context<O>,T,?> pHandler) {
		handler = pHandler;
		return this;
	}

	/** Returns the options value handler, if any, or null.
	 * @return The options value handler, if any, or null.
	 */
	public FailableBiConsumer<Context<O>,T,?> getHandler() {
		return handler;
	}
}