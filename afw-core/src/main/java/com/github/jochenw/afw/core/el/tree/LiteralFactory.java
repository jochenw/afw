package com.github.jochenw.afw.core.el.tree;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * A factory for literal values.
 */
public class LiteralFactory {
    private static final Integer[] lowIntegers = new Integer[128];
    static {
        for (int i = 0;  i < lowIntegers.length;  i++) {
            lowIntegers[i] = new Integer(i);
        }
    }

    /**
     * Creates a new integer value.
     */
    public static Number asInteger(int pValue) {
        if (pValue >= 0  &&  pValue < lowIntegers.length) {
            return lowIntegers[pValue];
        }
        return new Integer(pValue);
    }

    /**
     * Creates a new integer value.
     */
    public static Number asInteger(long pValue) {
        return new Long(pValue);
    }

    /**
     * Creates a new integer value.
     */
    public static Number asInteger(String pValue) {
        try {
            int value = Integer.parseInt(pValue);
            return asInteger(value);
        } catch (Exception e1) {
            try {
                return new Long(pValue);
            } catch (Exception e2) {
                try {
                    return new BigInteger(pValue);
                } catch (Exception e3) {
                    throw new IllegalArgumentException("Invalid integer value: " + pValue);
                }
            }
        }
    }

    /**
     * Creates a new floating point value.
     */
    public static Number asFloatingPoint(float pValue) {
        return new Float(pValue);
    }

    /**
     * Creates a new floating point value.
     */
    public static Number asFloatingPoint(double pValue) {
        return new Double(pValue);
    }

    /**
     * Creates a new floating point value.
     */
    public static Number asFloatingPoint(String pValue) {
        try {
            return new Float(pValue);
        } catch (Exception e1) {
            try {
                return new Double(pValue);
            } catch (Exception e2) {
                try {
                    return new BigDecimal(pValue);
                } catch (Exception e3) {
                    throw new IllegalArgumentException("Invalid floating point value: " + pValue);
                }
            }
        }
    }
}
