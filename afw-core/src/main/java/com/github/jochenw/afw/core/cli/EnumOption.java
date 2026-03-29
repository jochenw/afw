package com.github.jochenw.afw.core.cli;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.cli.Cli.UsageException;
import com.github.jochenw.afw.core.util.Objects;


/** Implementation of {@link Option} for enum values.
 * @param <B> The options bean type.
 * @param <E> The enum type.
 */
public class EnumOption<B,E extends Enum<E>> extends Option<B,E> {
	private boolean caseInsensitive;

	/** Creates a new instance.
	 * @param pCli The {@link Cli}, that creates this option.
	 * @param pType The enum type.
	 * @param pPrimaryName The options primary name. Always non-null.
	 * @param pSecondaryNames The options secondary names, if any.
	 */
	public EnumOption(@NonNull Cli<B> pCli, @NonNull Class<E> pType, @NonNull String pPrimaryName,
			@NonNull String[] pSecondaryNames) {
		super(pCli, pType, pPrimaryName, pSecondaryNames);
	}

	@Override
	public E getValue(@NonNull String pOptValue) throws UsageException {
		@SuppressWarnings("null")
		final @NonNull Class<E> type = getType();
		if (isCaseInsensitive()) {
			final @NonNull E[] valuesArray = Objects.enumValues(type);
			for (int i = 0;  i < valuesArray.length;  i++) {
				final E e = valuesArray[i];
				if (e.name().equalsIgnoreCase(pOptValue)) {
					return e;
				}
			}
			throw new UsageException("Invalid value for option " + getPrimaryName() + ": Expected "
					+ Objects.enumNamesAsString(type, "|") + " (case insensitive), got " + pOptValue);
		} else {
			try {
				return Enum.valueOf(type, pOptValue);
			} catch (IllegalArgumentException e) {
				throw new UsageException("Invalid value for option " + getPrimaryName() + ": Expected "
						+ Objects.enumNamesAsString(type, "|") + ", got " + pOptValue);
			}
		}
	}

	/** Returns, whether option values should be treated case insensitive.
	 * By default, option values are case sensitive.
	 * @return True, if option values are treated case insensitive.
	 * @see #caseInsensitive(boolean)
	 * @see #caseInsensitive()
	 */
	public boolean isCaseInsensitive() { return caseInsensitive; }
	/** Sets, that option values should be treated case insensitive.
	 * By default, option values are case sensitive. Equivalent to
	 * <pre>
	 *   caseInsensitive(true)
	 * </pre>
	 * @see #isCaseInsensitive()
	 * @see #caseInsensitive(boolean)
	 * @return This option.
	 */
	public EnumOption<B,E> caseInsensitive() { return caseInsensitive(true); }
	/** Sets, whether option values should be treated case insensitive.
	 * By default, option values are case sensitive.
	 * @param pCaseInsensitive True, if option values are treated case insensitive.
	 * @see #isCaseInsensitive()
	 * @see #caseInsensitive()
	 * @return This option.
	 */
	public EnumOption<B,E> caseInsensitive(boolean pCaseInsensitive) { caseInsensitive = pCaseInsensitive; return this; }
}
