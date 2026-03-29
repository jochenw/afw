package com.github.jochenw.afw.core.cli;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.cli.Cli.UsageException;

/** Implementation of {@link Option} for boolean values.
 * @param <B> The options bean type.
 */
public class BooleanOption<B> extends Option<B,Boolean> {
	/** Creates a new instance.
	 * @param pCli The {@link Cli}, that creates this option.
	 * @param pPrimaryName The options primary name. Always non-null.
	 * @param pSecondaryNames The options secondary names, if any.
	 */
	@SuppressWarnings("null")
	protected BooleanOption(@NonNull Cli<B> pCli, @NonNull String pPrimaryName, @NonNull String[] pSecondaryNames) {
		super(pCli, Boolean.class, pPrimaryName, pSecondaryNames);
	}
	public boolean isNullValueValid() { return true; }
	@Override
	public Boolean getValue(@NonNull String pOptValue) throws UsageException {
		return Boolean.valueOf(pOptValue);
	}

	/** Returns the options builtin default value, if any.
	 * @return The options builtin default value, or null,
	 *   if there is no builtin default value.
	 */
	@Override
	public @Nullable String getBuiltinDefaultValue() {
		return "true";
	}
}
