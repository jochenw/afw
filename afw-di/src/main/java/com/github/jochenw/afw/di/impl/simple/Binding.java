package com.github.jochenw.afw.di.impl.simple;

import java.util.function.Function;

/** A binding provides a value, that provides a value for a particular
 * field, or method parameter.
 */
public interface Binding extends Function<SimpleComponentFactory,Object> {
}
