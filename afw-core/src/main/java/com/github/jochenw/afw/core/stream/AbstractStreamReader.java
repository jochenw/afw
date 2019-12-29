package com.github.jochenw.afw.core.stream;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.stream.StreamController.MetaData;
import com.github.jochenw.afw.core.util.Exceptions;

public abstract class AbstractStreamReader implements StreamReader {
	private final StreamController streamController = new StreamController();

	public static class Data {
		@Nonnull private final Class<Object> type;
		@Nonnull private final Object bean;
		@Nonnull private final MetaData metaData;
		public Data(@Nonnull Class<Object> pType, @Nonnull Object pBean, @Nonnull MetaData pMetaData) {
			type = pType;
			bean = pBean;
			metaData = pMetaData;
		}
		public @Nonnull Class<Object> getType() {
			return type;
		}
		public @Nonnull Object getBean() {
			return bean;
		}
		public @Nonnull MetaData getMetaData() {
			return metaData;
		}
	}

	protected Data newData(Class<Object> pType) {
		final Object bean = newBean(pType);
		final MetaData metaData = streamController.getMetaData(pType);
		return new Data(pType, bean, metaData);
	}

	protected Object newBean(Class<Object> pType) {
		try {
			final Constructor<Object> cons = pType.getConstructor();
			return cons.newInstance();
		} catch (Throwable t) {
			throw Exceptions.show("Unable to create bean of type "
					              + pType.getClass().getName() + ": ", t);
		}
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

	/** Converts the given string value into an object, which is suitable
	 * for the given value.
	 * @param pValue The string value being converted.
	 * @param pField The field, for which the converted value must be suitable.
	 * @return The converted value.
	 */
	public Object fromString(String pValue, Field pField) {
		final Class<?> type = pField.getType();
		if (pValue == null) {
			return null;
		} else {
			if (String.class == type) {
				return pValue;
			} else if (Boolean.class == type  ||  Boolean.TYPE == type) {
				return Boolean.valueOf(pValue);
			} else if (Long.class == type  ||  Long.TYPE == type) {
				try {
					return Long.valueOf(pValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Invalid long value for field " + pField + ": " + pValue, nfe);
				}
			} else if (Integer.class == type  ||  Integer.TYPE == type) {
				try {
					return Integer.valueOf(pValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Invalid integer value for field " + pField + ": " + pValue, nfe);
				}
			} else if (Short.class == type  ||  Short.TYPE == type) {
				try {
					return Short.valueOf(pValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Invalid short value for field " + pField + ": " + pValue, nfe);
				}
			} else if (Byte.class == type  ||  Byte.TYPE == type) {
				try {
					return Byte.valueOf(pValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Invalid byte value for field " + pField + ": " + pValue, nfe);
				}
			} else if (Double.class == type  ||  Double.TYPE == type) {
				try {
					return Double.valueOf(pValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Invalid double value for field " + pField + ": " + pValue, nfe);
				}
			} else if (Float.class == type  ||  Float.TYPE == type) {
				try {
					return Float.valueOf(pValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Invalid byte value for field " + pField + ": " + pValue, nfe);
				}
			} else if (Character.class == type  ||  Character.TYPE == type) {
				if (pValue.length() == 1) {
					return Character.valueOf(pValue.charAt(0));
				} else {
					throw new IllegalStateException("Invalid char value for field " + pField + ": " + pValue
							                        + " (Expected one character, got " + pValue.length() + ")");
				}
			} else if (BigDecimal.class == type) {
				try {
					return new BigDecimal(pValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Invalid BigDecimal value for field " + pField + ": " + pValue, nfe);
				}
			} else if (BigInteger.class == type) {
				try {
					return new BigInteger(pValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Invalid BigInteger value for field " + pField + ": " + pValue, nfe);
				}
			} else {
				throw new IllegalStateException("Invalid type for field " + pField + ": " + type.getName());
			}
		}
	}

	/**
	 * Sets the given field on the given bean to the given value.
	 * @param pField The field, which is being modified.
	 * @param pBean The bean, which is being updated.
	 * @param pValue The new field value.
	 */
	public void setValue(Field pField, Object pBean, Object pValue) {
		try {
			if (!pField.isAccessible()) {
				pField.setAccessible(true);
			}
			pField.set(pBean, pValue);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
