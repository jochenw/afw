package com.github.jochenw.afw.core.cli;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.cli.Cli.UsageException;
import com.github.jochenw.afw.core.util.NotImplementedException;
import com.github.jochenw.afw.core.util.Reflection;

/** Implementation of an {@link Option} for list values.
 * @param <B> The options bean type.
 * @param <E> Element type of the list value.
 */
public class RepeatableOption<B,E> extends Option<B,List<E>> {
	private final @NonNull Option<B,E> option;
	private final List<E> values = new ArrayList<>();

	private static <E> @NonNull Class<List<E>> listType() {
		final @NonNull Class<List<E>> cl = Reflection.cast(List.class);
		return cl;
	}

	/** Creates a new instance.
	 * @param pCli The {@link Cli}, that creates this option.
	 * @param pOption An existing option object, which is being
	 * used to handle single value strings, and elements.
	 * @param pPrimaryName The options primary name. Always non-null.
	 * @param pSecondaryNames The options secondary names, if any.
	 */
	protected RepeatableOption(@NonNull Cli<B> pCli, @NonNull Option<B,E> pOption,
			                   @NonNull String pPrimaryName, @NonNull String[] pSecondaryNames) {
		super(pCli, listType(), pPrimaryName, pSecondaryNames);
		option = pOption;
	}

	@Override
	public List<E> getValue(@NonNull String optValue) throws UsageException {
		throw new NotImplementedException();
	}

	/** Adds a new value to the list of collected value objects.
	 * @param pValue The value, which is being added.
	 */
	protected void addValue(E pValue) {
		values.add(pValue);
	}

	/** Converts the given string value into an actual value
	 * element. This is basically the equivalent of
	 * {@link Option#getValue(String)} for repeatable
	 * options.
	 * @param pOptValue The string value, which is being converted.
	 * @return The converted value, of any.
	 * @throws UsageException The given string value is invalid,
	 *   and cannot be converted.
	 */
	public E getElementValue(@NonNull String pOptValue) {
		return option.getValue(pOptValue);
	}

	/** Returns the list of collected values.
	 * @return The list of collected values.
	 */
	public List<E> getValues() { return values; }

	@Override public boolean isRepeatable() { return true; }
}
