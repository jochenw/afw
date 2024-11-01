package com.github.jochenw.afw.core.cli;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.cli.Cli.UsageException;

/** Implementation of {@link Option} for integer values.
 * @param <B> The options bean type.
 */
public class StringOption<B> extends Option<B,String> {
	/** Creates a new instance.
	 * @param pCli The {@link Cli}, that creates this option.
	 * @param pPrimaryName The options primary name. Always non-null.
	 * @param pSecondaryNames The options secondary names, if any.
	 */
	@SuppressWarnings("null")
	protected StringOption(@NonNull Cli<B> pCli, @NonNull String pPrimaryName, @NonNull String[] pSecondaryNames) {
		super(pCli, String.class, pPrimaryName, pSecondaryNames);
	}

	@Override
	public String getValue(String pOptValue) throws UsageException {
		if (pOptValue == null) {
			return getDefaultValue();
		}
		return pOptValue;
	}
}
