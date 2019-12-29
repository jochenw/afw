package com.github.jochenw.afw.core.stream;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.stream.StreamController.MetaData;
import com.github.jochenw.afw.core.util.Exceptions;


/** Abstract base class for implementations of {@link StreamWriter}.
 */
public abstract class AbstractStreamWriter implements StreamWriter {
	private final StreamController metaDataController = new StreamController();

	/**
	 * Returns the given objects streamable id. The streamable id is
	 * given by the objects class. This class {@em must} be annotated
	 * with {@link Streamable @Streamable}.
	 * @return The value of the id attribute in the types
	 * {@link Streamable @Streamable} annotation, if that attribute
	 * is present, and non-empty. Otherwise, the types
	 * {@link Class#getSimpleName() simple name}, as a default value.
	 * @throws IllegalArgumentException The objects type is not
	 * annotated with {@link Streamable @Streamable}.
	 */
	protected String requireStreamableId(@Nonnull Object pStreamable) {
		return requireStreamableId(pStreamable.getClass());
	}

	protected String requireStreamableId(@Nonnull Class<?> pType) {
		final Streamable streamableAnnotation = pType.getAnnotation(Streamable.class);
		if (streamableAnnotation == null) {
			throw new IllegalArgumentException("The class "
					 + pType.getName() + " is not annotated with @Streamable");
		} else {
			final String s = streamableAnnotation.id();
			if (s.length() == 0) {
				return pType.getSimpleName();
			} else {
				return s;
			}
		}
	}

	protected MetaData getMetaData(Class<?> pType) {
		return metaDataController.getMetaData(pType);
	}

	protected boolean isTerse(Field pField) {
		final Streamable streamable = pField.getAnnotation(Streamable.class);
		return streamable.terse();
	}

	protected boolean isAtomic(Field pField) {
		final Class<?> type = pField.getType();
		return type == String.class
			|| type == Boolean.class
			|| type == Boolean.TYPE
			|| type == Long.class
			|| type == Long.TYPE
			|| type == Integer.class
			|| type == Integer.TYPE
			|| type == Short.class
			|| type == Short.TYPE
			|| type == Byte.class
			|| type == Byte.TYPE
			|| type == Character.class
			|| type == Character.TYPE
			|| type == Double.class
			|| type == Double.TYPE
			|| type == Float.class
			|| type == Float.TYPE
			|| type == BigDecimal.class
			|| type == BigInteger.class;
	}

	protected String asString(@Nonnull Field pField, @Nonnull Object pObject) {
		final Class<?> type = pField.getType();
		final Object v = getValue(pField, pObject);
		if (v == null) {
			return null;
		}
		if (type == String.class) {
			return (String) v;
		} else if (type == Boolean.class  ||  type == Boolean.TYPE) {
			final Boolean b = (Boolean) v;
			return String.valueOf(b.booleanValue());
		} else if (type == Long.class  ||  type == Long.TYPE) {
			final Long l = (Long) v;
			return String.valueOf(l.longValue());
		} else if (type == Integer.class  ||  type == Integer.TYPE) {
			final Integer i = (Integer) v;
			return String.valueOf(i.intValue());
		} else if (type == Short.class  ||  type == Short.TYPE) {
			final Short s = (Short) v;
			return String.valueOf(s.shortValue());
		} else if (type == Byte.class  ||  type == Byte.TYPE) {
			final Byte b = (Byte) v;
			return String.valueOf(b.byteValue());
		} else if (type == Character.class  ||  type == Character.TYPE) {
			final Character c = (Character) v;
			return new String(new char[] {c});
		} else {
			throw new IllegalStateException("Invalid atomic type: " + type.getName());
		}
	}

	protected Object getValue(Field pField, Object pObject) {
		final Object v;
		try {
			if (!pField.isAccessible()) {
				pField.setAccessible(true);
			}
			v = pField.get(pObject);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		return v;
	}
}
