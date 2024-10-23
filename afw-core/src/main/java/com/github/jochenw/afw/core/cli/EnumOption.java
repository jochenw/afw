package com.github.jochenw.afw.core.cli;

import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;

/** Implementation of {@link Option} for {@code enum} values.
 * @param <O> Type of the options bean.
 * @param <E> Type of the {@code enum} value.
 */
public class EnumOption<O, E extends Enum<E>>  extends Option<E,O> {
	private final Class<E> enumType;

	/** Creates a new instance with the given {@link Cli},
	 * and {@code end handler}.
	 * @param pCli The {@link Cli}, which is creating this option.
	 * @param pEnumType Type of the {@code enum} value.
	 * @param pEndHandler The {@code end handler}, which is being
	 *   invoked upon invocation of {@link Option#end()}.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names.
	 */
	protected EnumOption(@NonNull Cli<O> pCli, @NonNull Class<E> pEnumType, @NonNull Consumer<Option<?, O>> pEndHandler,
			             @NonNull String pPrimaryName, @NonNull String[] pSecondaryNames) {
		super(pCli, pEndHandler, pPrimaryName, pSecondaryNames);
		enumType = pEnumType;
	}

	@Override
	public @NonNull E getValue(@NonNull String pStrValue) {
		@SuppressWarnings("null")
		final @NonNull E e = (E) Enum.valueOf(enumType, pStrValue);
		return e;
	}
}
