package com.github.jochenw.afw.core.components;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An instantiator is an object, which creates new instances.
 */
public interface IInstantiator {
	/**
	 * Creates a new instance, using the given {@link ClassLoader}.
	 * @param <O> The created objects formal type. The objects must be
	 * castable to the formal type.
	 * @param pClassLoader The class loader to use when creating the object.
	 * @param pClassName The created objects class name.
	 * @param pBeanProperties An optional set of bean properties, which
	 * should be set on the created object.
	 * @return A new instance, configured with the requested set of
	 * bean properties.
	 */
	public <O> O newInstance(@Nonnull ClassLoader pClassLoader, @Nonnull String pClassName, @Nullable Map<String,String> pBeanProperties); 

	/**
	 * Creates a new instance, using the given {@link ClassLoader}.
	 * @param <O> The created objects formal type. The objects must be
	 * castable to the formal type.
	 * @param pClassLoader The class loader to use when creating the object.
	 * @param pClassName The created objects class name.
	 * @param pBeanProperties An optional set of bean properties, which
	 * should be set on the created object. The properties are being supplied
	 * as a key/value pairs.
	 * @return A new instance, configured with the requested set of
	 * bean properties.
	 */
	public <O> O newInstance(@Nonnull ClassLoader pClassLoader, @Nonnull String pClassName, @Nullable String... pBeanProperties); 
}
