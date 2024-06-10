package com.github.jochenw.afw.core.cli;

import java.util.List;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.NotImplementedException;


/** Implementation of an option, which is repeatable. (The
 * option value is a list.)
 * @param <T> Element type of the list, which is the option value.
 * @param <O> Type of the option bean.
 */
public class ListOption<T,O> extends Option<List<T>,O> {
	private final Option<T,O> option;

	/** Creates a new instance with the given {@link Cli},
	 * and {@code end handler}.
	 * @param pOption The option, that created this list option.
	 */
	public ListOption(Option<T,O> pOption) {
		super(pOption.getCli(), pOption.getEndHandler(), pOption.getPrimaryName(),
			  pOption.getSecondaryNames());
		option = pOption;
	}

	@Override
	public @NonNull List<T> getValue(@NonNull String pStrValue) {
		throw new NotImplementedException();
	}

	/** Returns, whether this option is repeatable.
	 */
	public boolean isRepeatable() { return true; }

	@Override
	public @NonNull Option<List<T>, O> required() {
		option.required();
		return this;
	}

	@Override
	public boolean isRequired() {
		return option.isRequired();
	}
}
