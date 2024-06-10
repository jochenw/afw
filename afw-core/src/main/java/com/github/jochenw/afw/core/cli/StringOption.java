package com.github.jochenw.afw.core.cli;

import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Implementation of {@link Option} for string values.
 * @param <O> Type of the option bean.
 */
public class StringOption<O> extends Option<String,O> {
	/** Creates a new instance with the given {@link Cli},
	 * and {@code end handler}.
	 * @param pCli The {@link Cli}, which is creating this option.
	 * @param pEndHandler The {@code end handler}, which is being
	 *   invoked upon invocation of {@link Option#end()}.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names.
	 */
	protected StringOption(@NonNull Cli<O> pCli, @NonNull Consumer<Option<?,O>> pEndHandler,
	                       @NonNull String pPrimaryName,
	                       @NonNull String @Nullable [] pSecondaryNames) {
		super(pCli, pEndHandler, pPrimaryName, pSecondaryNames);
	}

	@Override
	public @NonNull String getValue(@NonNull String pStrValue) {
		return pStrValue;
	}
}