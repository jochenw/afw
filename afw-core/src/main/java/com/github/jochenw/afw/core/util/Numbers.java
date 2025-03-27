package com.github.jochenw.afw.core.util;

import java.math.BigDecimal;

import org.jspecify.annotations.Nullable;

/** Utility class for working with numbers.
 */
public class Numbers {
	/** Creates a new instance. (Private, because this class contains only
	 * static methods.
	 */
	private Numbers() {}

	/** Safe conversion of an int number to a short number.
	 * @param pValue The int value, which is being converted.
	 * @return The converted short value.
	 * @throws IllegalArgumentException The given int value is not
	 *   in the permitted range for short values.
	 */
	public static short toShort(int pValue) {
		if (pValue < Short.MIN_VALUE  ||  pValue > Short.MAX_VALUE) {
			throw new NumberFormatException("The given integer value is not in the permitted range for a short number.");
		}
		return (short) pValue;
	}

	/** Safe conversion of an int number to a byte number.
	 * @param pValue The int value, which is being converted.
	 * @return The converted short value.
	 * @throws IllegalArgumentException The given int value is not
	 *   in the permitted range for short values.
	 */
	public static byte toByte(int pValue) {
		if (pValue < Byte.MIN_VALUE  ||  pValue > Byte.MAX_VALUE) {
			throw new NumberFormatException("The given integer value is not in the permitted range for a byte number.");
		}
		return (byte) pValue;
	}

	/** Safe conversion of a short number to a byte number.
	 * @param pValue The short value, which is being converted.
	 * @return The converted short value.
	 * @throws IllegalArgumentException The given int value is not
	 *   in the permitted range for short values.
	 */
	public static byte toByte(short pValue) {
		if (pValue < Byte.MIN_VALUE  ||  pValue > Byte.MAX_VALUE) {
			throw new NumberFormatException("The given short value is not in the permitted range for a byte number.");
		}
		return (byte) pValue;
	}

	/** Safe conversion of a double number to a float number.
	 * @param pValue The double value, which is being converted.
	 * @return The converted float value.
	 * @throws IllegalArgumentException The given int value is not
	 *   in the permitted range for short values.
	 */
	public static float toFloat(double pValue) {
		if (pValue < Float.MIN_VALUE  ||  pValue > Float.MAX_VALUE) {
			throw new NumberFormatException("The given double value is not in the permitted range for a float number.");
		}
		return (float) pValue;
	}

	/** Attempts to convert the given {@link BigDecimal} into a
	 * number object with smaller precision. If possible,
	 * the conversion will be without loss of information.
	 * @param pBd The number, which is being converted.
	 * @return The converted number.
	 */
	public static @Nullable Object toNumber(BigDecimal pBd) {
		try {
			return pBd.byteValueExact();
		} catch (ArithmeticException ae) {
			try {
				return pBd.shortValueExact();
			} catch (ArithmeticException ae2) {
				return toNumberPreferringInteger(pBd);
			}
		}
	}

	/** Attempts to convert the given {@link BigDecimal} into a
	 * number object with smaller precision. If possible,
	 * the conversion will be without loss of information.
	 * Basically, this is the same as {@link #toNumber(BigDecimal)},
	 * except that no attempt will be made to return a
	 * {@link Byte}, or {@link Short}. Instead, such numbers
	 * will be represented as {@link Integer}.
	 * @param pBd The number, which is being converted.
	 * @return The converted number.
	 */
	public static @Nullable Object toNumberPreferringInteger(BigDecimal pBd) {
		try {
			return Integer.valueOf(pBd.intValueExact());
		} catch (ArithmeticException ae3) {
			try {
				return Long.valueOf(pBd.longValueExact());
			} catch (ArithmeticException ae4) {
				try {
					return pBd.toBigIntegerExact();
				} catch (ArithmeticException ae5) {
					final double d = pBd.doubleValue();
					if (d == Double.NEGATIVE_INFINITY  ||  d == Double.POSITIVE_INFINITY) {
						return pBd;
					}
					return Double.valueOf(d);
				}
			}
		}
	}
}
