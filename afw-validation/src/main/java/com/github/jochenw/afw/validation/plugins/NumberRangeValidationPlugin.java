package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.github.jochenw.afw.validation.api.NumberRange;
import com.github.jochenw.afw.validation.plugins.ValidatorFactory.TypedProvider;


public class NumberRangeValidationPlugin extends AbstractNumberValidationPlugin<NumberRange> {
	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return NumberRange.class;
	}

	@Override
	protected String getCode(NumberRange pAnnotation) {
		return pAnnotation.code();
	}
	
	
	@Override
	protected SimpleValidator<Number> newSimpleValidator(final TypedProvider<Number> pProvider, final String pProperty, final NumberRange pAnnotation) {
		final Class<?> type = pProvider.getType();
		final Number leNumber = asNumber(pAnnotation.le(), type);
		final Number ltNumber = asNumber(pAnnotation.lt(), type);
		final Number geNumber = asNumber(pAnnotation.ge(), type);
		final Number gtNumber = asNumber(pAnnotation.gt(), type);
		return new SimpleValidator<Number>() {
			@Override
			public String isValid(String pContext, Number pValue) {
				if (pValue == null) {
					if (!pAnnotation.nullable()) {
						return getCode(pAnnotation) + ": The value for property " + pProperty
								+ " is null at " + pContext;
					}
				} else {
					if (geNumber != null) {
						final boolean isValid = compare(pValue, geNumber) >= 0;
						if (!isValid) {
							return getCode(pAnnotation) + ": The value for property " + pProperty
									+ " is lower than " + pAnnotation.ge();
						}
					}
					if (gtNumber != null) {
						final boolean isValid = compare(pValue, gtNumber) > 0;
						if (!isValid) {
							return getCode(pAnnotation) + ": The value for property " + pProperty
									+ " is lower than, or equal to " + pAnnotation.gt();
						}
					}
					if (leNumber != null) {
						final boolean isValid = compare(pValue, leNumber) <= 0;
						if (!isValid) {
							return getCode(pAnnotation) + ": The value for property " + pProperty
									+ " is larger than " + pAnnotation.le();
						}
					}
					if (ltNumber != null) {
						final boolean isValid = compare(pValue, ltNumber) < 0;
						if (!isValid) {
							return getCode(pAnnotation) + ": The value for property " + pProperty
									+ " is larger than, or equal to " + pAnnotation.lt();
						}
					}
				}
				return null;
			}
		};
	}

	@Override
	protected String isValid(String pContext, String pProperty, NumberRange pAnnotation, Number pValue) {
		throw new IllegalStateException("Not implemented");
	}

	private int compare(Number pN1, Number pN2) {
		if (pN1 instanceof BigDecimal) {
			final BigDecimal bd1 = (BigDecimal) pN1;
			final BigDecimal bd2 = (BigDecimal) pN2;
			return bd1.compareTo(bd2);
		}
		if (pN1 instanceof BigInteger) {
			final BigInteger bd1 = (BigInteger) pN1;
			final BigInteger bd2 = (BigInteger) pN2;
			return bd1.compareTo(bd2);
		}
		if (pN1 instanceof Long  ||  pN1 instanceof Integer  ||  pN1 instanceof Short  ||  pN1 instanceof Byte) {
			final long l1 = pN1.longValue();
			final long l2 = pN2.longValue();
			if (l1 == l2) {
				return 0;
			} else if (l1 > l2) {
				return 1;
			} else {
				return -1;
			}
		} else {
			final double d1 = pN1.doubleValue();
			final double d2 = pN2.doubleValue();
			if (d1 == d2) {
				return 0;
			} else if (d1 > d2) {
				return 1;
			} else {
				return -1;
			}
		}
	}
	
	private Number asNumber(String pValueStr, Class<?> pType) {
		if (pValueStr == null  ||  pValueStr.length() == 0) {
			return null;
		}
		if (BigDecimal.class.isAssignableFrom(pType)) {
			try {
				return new BigDecimal(pValueStr);
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("Invalid BigDecimal value for NumberRange annotation: " + pValueStr);
			}
		}
		if (BigInteger.class.isAssignableFrom(pType)) {
			try {
				return new BigInteger(pValueStr);
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("Invalid BigInteger value for NumberRange annotation: " + pValueStr);
			}
		}
		if (Long.TYPE == pType  ||  Long.class.isAssignableFrom(pType)) {
			try {
				return Long.valueOf(pValueStr);
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("Invalid Long value for NumberRange annotation: " + pValueStr);
			}
		}
		if (Integer.TYPE == pType  ||  Integer.class.isAssignableFrom(pType)) {
			try {
				return Integer.valueOf(pValueStr);
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("Invalid Integer value for NumberRange annotation: " + pValueStr);
			}
		}
		if (Short.TYPE == pType  ||  Short.class.isAssignableFrom(pType)) {
			try {
				return Short.valueOf(pValueStr);
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("Invalid Short value for NumberRange annotation: " + pValueStr);
			}
		}
		if (Byte.TYPE == pType  ||  Byte.class.isAssignableFrom(pType)) {
			try {
				return Byte.valueOf(pValueStr);
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("Invalid Byte value for NumberRange annotation: " + pValueStr);
			}
		}
		if (Double.TYPE == pType  ||  Double.class.isAssignableFrom(pType)) {
			try {
				return Double.valueOf(pValueStr);
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("Invalid Double value for NumberRange annotation: " + pValueStr);
			}
		}
		if (Float.TYPE == pType  ||  Float.class.isAssignableFrom(pType)) {
			try {
				return Float.valueOf(pValueStr);
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("Invalid Double value for NumberRange annotation: " + pValueStr);
			}
		}
		throw new IllegalStateException("Invalid number type: " + pType.getName());
	}
}
