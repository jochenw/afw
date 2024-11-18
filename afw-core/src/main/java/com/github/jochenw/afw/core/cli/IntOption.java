package com.github.jochenw.afw.core.cli;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.cli.Cli.UsageException;


/** Implementation of {@link Option} for integer values.
 * @param <B> The options bean type.
 */
public class IntOption<B> extends Option<B,Integer> {
	/** Creates a new instance.
	 * @param pCli The {@link Cli}, that creates this option.
	 * @param pPrimaryName The options primary name. Always non-null.
	 * @param pSecondaryNames The options secondary names, if any.
	 */
	@SuppressWarnings("null")
	protected IntOption(@NonNull Cli<B> pCli, @NonNull String pPrimaryName, @NonNull String[] pSecondaryNames) {
		super(pCli, Integer.class, pPrimaryName, pSecondaryNames);
	}

	@Override
	public Integer getValue(@NonNull String pOptValue) throws UsageException {
		try {
			return Integer.valueOf(pOptValue);
		} catch (NumberFormatException nfe) {
			throw new UsageException("Invalid value for option " + getPrimaryName() + "; Expected integer number, got " + pOptValue);
		}
	}
}
