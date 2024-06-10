package com.github.jochenw.afw.core.cli;

import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.util.Objects;

/** Implementation of {@link Option} for integer values.
 * @param <O> Type of the option bean.
 */
public class IntOption<O> extends Option<Integer,O> {
	/** Creates a new instance with the given {@link Cli},
	 * and {@code end handler}.
	 * @param pCli The {@link Cli}, which is creating this option.
	 * @param pEndHandler The {@code end handler}, which is being
	 *   invoked upon invocation of {@link Option#end()}.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names.
	 */
	protected IntOption(@NonNull Cli<O> pCli, @NonNull Consumer<Option<?,O>> pEndHandler,
                        @NonNull String pPrimaryName,
                        @NonNull String @Nullable [] pSecondaryNames) {
		super(pCli, pEndHandler, pPrimaryName, pSecondaryNames);
	}

	@Override
	public @NonNull Integer getValue(@NonNull String pStrValue) {
		return Objects.requireNonNull(Integer.valueOf(pStrValue));
	}
}