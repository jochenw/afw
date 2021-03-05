/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javax.inject.Named;

import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactory;

/**
 * A {@link IComponentFactory} is basically a map: In the case of
 * the {@link SimpleComponentFactory}, the map's keys are instances
 * of {@link Key}, and the values are the respective bindings.
 * @param <O> Type of the binding, that is associated with the key.
 */
public class Key<O extends Object> {
	private final @Nonnull Type type;
	private final @Nullable Annotation annotation;
	private final @Nullable Class<? extends Annotation> annotationClass;

	/** Creates a new instance with the given type, no annotation,
	 * and no annotation class.
	 * @param pType The type, to which the keys binding is applicable.
	 */
	public Key(@Nonnull Type pType) {
		type = pType;
		annotation = null;
		annotationClass = null;
	}

	/** Creates a new instance with the given type, the given annotation,
	 * and no annotation class.
	 * @param pType The type, to which the keys binding is applicable.
	 * @param pAnnotation The annotation, which must be present to make
	 * the keys binding applicable.
	 */
	public Key(@Nonnull Type pType, @Nonnull Annotation pAnnotation) {
		type = pType;
		annotation = pAnnotation;
		annotationClass = null;
	}

	/** Creates a new instance with the given type, no annotation,
	 * and the given annotation class.
	 * @param pType The type, to which the keys binding is applicable.
	 * @param pAnnotationClass The annotation class, which must be
	 *   present to make the keys binding applicable.
	 */
	public Key(@Nonnull Type pType, @Nonnull Class<? extends Annotation> pAnnotationClass) {
		type = pType;
		annotation = null;
		annotationClass = pAnnotationClass;
	}

	/** Returns the type, to which the keys binding is applicable.
	 * @return The type, to which the keys binding is applicable.
	 */
	public Type getType() {
		return type;
	}

	/** Returns the annotation, which must be present to make
	 * the keys binding applicable.
	 * @return The annotation, which must be present to make
	 * the keys binding applicable.
	 */
	public Annotation getAnnotation() {
		return annotation;
	}

	/** Returns the annotation class, which must be present to make
	 * the keys binding applicable.
	 * @return The annotation class, which must be present to make
	 * the keys binding applicable.
	 */
	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	@Override
	public int hashCode() {
		/* It is important, to use the getters here, rather than
		 * directly accessing the fields, so that the method works
		 * for subclasses, like MutableKey, as well.
		 */
		return Objects.hash(getType(), getAnnotation(), getAnnotationClass());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Key<?> other = (Key<?>) obj;
		/* It is important, to use the getters here, rather than
		 * directly accessing the fields, so that the method works
		 * for subclasses, like MutableKey, as well.
		 */
		final Annotation anno = getAnnotation();
		final Annotation otherAnno = other.getAnnotation();
		if (anno == null) {
			if (otherAnno != null)
				return false;
		} else if (!anno.equals(otherAnno))
			return false;
		final Class<? extends Annotation> annoClass = getAnnotationClass();
		final Class<? extends Annotation> otherAnnoClass = other.getAnnotationClass();
		if (annoClass == null) {
			if (otherAnnoClass != null)
				return false;
		} else if (!annoClass.equals(otherAnnoClass))
			return false;
		return getType().equals(other.getType());
	}

	/** Returns the keys description, as a string.
	 * Same value as {@link #toString()}.
	 * @return The keys description, as a string.
	 * @see #toString()
	 */
	public @Nonnull String getDescription() {
		final StringBuilder sb = new StringBuilder();
		final Type t = getType();
		if (t instanceof Class) {
			final Class<?> cl = (Class<?>) t;
			sb.append("class=");
			sb.append(cl.getName());
		} else {
			sb.append("type=");
			sb.append(type);
		}
		final Annotation anno = getAnnotation();
		if (anno != null) {
			if (anno instanceof Named) {
				final Named named = (Named) anno;
				sb.append(", name=");
				sb.append(named.value());
			} else {
				sb.append(", annotation=");
				sb.append(anno);
			}
		}
		final Class<? extends Annotation> annoClass = getAnnotationClass();
		if (annoClass != null) {
			sb.append(", annotationClass=");
			sb.append(annoClass);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
