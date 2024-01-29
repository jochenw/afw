package com.github.jochenw.afw.core.function;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.IListable;

/** Interface of an object, that is both an {@link Iterable}, and
 * a {@link IStreamable}.
 * @param <O> The item type.
 */
public interface IStreamableIterable<O> extends Iterable<O>, IStreamable<O> {
	@Override
	default Stream<O> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	@Override
	default @NonNull List<O> list() {
		final List<O> list = new ArrayList<>();
		for (O o : this) {
			list.add(o);
		}
		return list;
	}

	@Override
	default IListable<O> listable() {
		return IListable.of(list());
	}
}
