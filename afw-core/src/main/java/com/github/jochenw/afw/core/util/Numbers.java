package com.github.jochenw.afw.core.util;


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
}
