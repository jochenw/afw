package com.github.jochenw.afw.di.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

/** A bindings key identifies the binding. Basically, you can
 * think of the {@link IComponentFactory} as a set of key/binding
 * pairs.
 * @param <T> Type of the binding (Type of the instance, which is
 *   being returned by the supplier.
 */
public class Key<T> {
	private final Type type;
	private final String name;
	private final Class<? extends Annotation> annotationType;
	private final Annotation annotation;

	/** Creates a new key.
	 * @param pType Type of the created key.
	 * @param pName Name of the created key.
	 * @param pAnnotationType Annotation type of the created key, if any, or null.
	 * @param pAnnotation Annotation of the created key, if any, or null.
	 */
	public Key(Type pType, String pName, Class<? extends Annotation> pAnnotationType, Annotation pAnnotation) {
		type = pType;
		name = pName;
		annotationType = pAnnotationType;
		annotation = pAnnotation;
	}

	/** Returns the keys type, never null.
	 * @return The keys type, never null.
	 */
	public Type getType() { return type; }
	/** Returns the keys name, never null.
	 * @return The keys name, never null.
	 */
	public String getName() { return name; }
	/** Returns the keys annotation type, if any, or null.
	 * @return The keys annotation type, if any, or null.
	 */
	public Class<? extends Annotation> getAnnotationType() { return annotationType; }
	/** Returns the keys annotation, if any, or null.
	 * @return The keys annotation, if any, or null.
	 */
	public Annotation getAnnotation() { return annotation; }

	/** Creates a new key.
	 * @param pType Type of the created key. Must not be null.
	 * @param pName Name of the created key. Must not be null.
	 * @param pAnnotationType Annotation type of the created key, if any, or null.
	 * @param pAnnotation Annotation of the created key, if any, or null.
	 * @return The created key.
	 * @throws NullPointerException Type, or name, are null.
	 * @param <T> Type of the created key.
	 */
	public static <T> Key<T> of(Type pType, String pName, Class<? extends Annotation> pAnnotationType,
                                Annotation pAnnotation) {
		final Type type = Objects.requireNonNull(pType, "Type");
		final String name = Objects.requireNonNull(pName, "Name");
		return new Key<T>(type, name, pAnnotationType, pAnnotation);
	}
	/** Creates a new key with annotation type null, and annotation null.
	 * @param pType Type of the created key. Must not be null.
	 * @param pName Name of the created key. Must not be null.
	 * @return The created key.
	 * @throws NullPointerException Type, or name, are null.
	 * @param <T> Type of the created key.
	 */
	public static <T> Key<T> of(Type pType, String pName) {
		return of(pType, pName, null, null);
	}

	@Override
	public int hashCode() {
		return Objects.hash(annotation, annotationType, name, type);
	}

	@Override
	public boolean equals(Object pOther) {
		if (this == pOther)
			return true;
		if (pOther == null)
			return false;
		if (getClass() != pOther.getClass())
			return false;
		Key<?> other = (Key<?>) pOther;
		return Objects.equals(type, other.type)
				&&  Objects.equals(name, other.name)
				&& Objects.equals(annotationType, other.annotationType)
				&&  Objects.equals(annotation, other.annotation);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Key");
		sb.append(super.toString());
		sb.append("[type=");
		sb.append(type);
		if (name.length() > 0) {
			sb.append(", name=");
			sb.append(name);
		}
		if (annotationType != null) {
			sb.append(", annotationType=");
			sb.append(annotationType.getName());
		}
		if (annotation != null) {
			sb.append(", annotation=");
			sb.append(annotation);
			sb.append("(");
			sb.append(annotation.getClass().getName());
			sb.append(")");
		}
		sb.append("]");
		return sb.toString();
	}
}
