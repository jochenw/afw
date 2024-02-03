package com.github.jochenw.afw.di.api;

import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.di.api.Types.Type;



/** A {@link Binder} is an object, that can be used to create binding
 * builders.
 */
public interface Binder {
	/** Creates a binding builder, that will be applicable in a context
	 * with the given {@link Key}.
	 * @param <T> The created binding builders type.
	 * @param pKey The key, in which the created binding will be applicable.
	 * @return The created binding builder.
	 */
    <T> LinkableBindingBuilder<T> bind(@NonNull Key<T> pKey);
	/** Creates a binding builder, that will be applicable in a context
	 * with the given {@link Type type}.
	 * @param <T> The created binding builders type.
	 * @param pType The type, in which the created binding will be applicable.
	 * @return The created binding builder.
	 */
    <T> AnnotatableBindingBuilder<T> bind(Types. @NonNull Type<T> pType);
	/** Creates a binding builder, that will be applicable in a context
	 * with the given {@link Type type}, and the given name.
	 * @param <T> The created binding builders type.
	 * @param pType The type, in which the created binding will be applicable.
	 * @param pName The expected value of the {@code Named} annotation.
	 * @return The created binding builder.
	 */
    <T> LinkableBindingBuilder<T> bind(Types. @NonNull Type<T> pType, @NonNull String pName);
	/** Creates a binding builder, that will be applicable in a context
	 * with the given {@link Class type}.
	 * @param <T> The created binding builders type.
	 * @param pType The type, in which the created binding will be applicable.
	 * @return The created binding builder.
	 */
    <T> AnnotatableBindingBuilder<T> bind(@NonNull Class<T> pType);
	/** Creates a binding builder, that will be applicable in a context
	 * with the given {@link Class type}, and the given name.
	 * @param <T> The created binding builders type.
	 * @param pType The type, in which the created binding will be applicable.
	 * @param pName The expected value of the {@code Named} annotation.
	 * @return The created binding builder.
	 */
    <T> LinkableBindingBuilder<T> bind(@NonNull Class<T> pType, @NonNull String pName);
    /** Requests, that the binder should configure static fields of the given classes.
     * @param pTypes The classes, on which static fields are being configured.
     */
    void requestStaticInjection(@NonNull Class<?>... pTypes);
    /** Configures a consumer, that will be invoked after creation of the
     * {@link IComponentFactory component factory}.
     * @param pComponentFactory The component factory, that has been created.
     */
    void addFinalizer(@NonNull Consumer<@NonNull IComponentFactory> pComponentFactory);
}
