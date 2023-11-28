package com.github.jochenw.afw.di.api;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.di.impl.BindingBuilder;



/** Interface of a {@link BindingBuilder} without scope, and supplier, but with
 * the ability of restriction, based on annotations.
 * @param <T> Type of the binding, that is being created.
 */
public interface AnnotatableBindingBuilder<T> extends LinkableBindingBuilder<T> {
	/** Requests, that the binding is only applicable, if the injection point
	 * (field, or method) is annotated with an instance of the given annotation class.
	 * @param pAnnotationType The annotation type, that injection points must have.
	 * @return A {@link BindingBuilder} without scope, and supplier, which is no
	 *   longer restrictable, based on annotations.
	 */
	LinkableBindingBuilder<T> annotatedWith(Class<? extends Annotation> pAnnotationType);
	/** Requests, that the binding is only applicable, if the injection point
	 * (field, or method) is annotated with an annotation object, that is equal to
	 * the given annotation.
	 * @param pAnnotation The annotation, that injection points must have.
	 * @return A {@link BindingBuilder} without scope, and supplier, which is no
	 *   longer restrictable, based on annotations.
	 */
	LinkableBindingBuilder<T> annotatedWith(Annotation pAnnotation);
	/** Requests, that the binding is only applicable, if the injection point
	 * (field, or method) is annotated with an \@Named annotation, that has
	 * the given value.
	 * @param pValue Value, that the \@Named annotation must have.
	 * @return A {@link BindingBuilder} without scope, and supplier, which is no
	 *   longer restrictable, based on annotations.
	 */
	LinkableBindingBuilder<T> named(String pValue);
}
