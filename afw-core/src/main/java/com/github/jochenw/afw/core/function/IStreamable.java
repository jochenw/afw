package com.github.jochenw.afw.core.function;

import java.util.List;
import java.util.stream.Stream;

import com.github.jochenw.afw.core.util.IListable;

/** Interface of an object, that can be converted into a Java {@link Stream}.
 * @param <O> The item type.
 */
public interface IStreamable<O> {
	/** Converts this object into an item stream.
	 * @return The converted item stream.
	 */
	Stream<O> stream();
	/** Converts this object into an item list.
	 * @return The converted item stream.
	 */
	List<O> list();
	/** Converts this object into an item list.
	 * @return The converted item stream.
	 */
	IListable<O> listable();
}
