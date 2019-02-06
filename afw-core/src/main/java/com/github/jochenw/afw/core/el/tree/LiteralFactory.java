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
package com.github.jochenw.afw.core.el.tree;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * A factory for literal values.
 */
public class LiteralFactory {
    /**
     * Creates a new integer value.
     * @param pValue Returns a number with the given value.
     * @return A number with the given value.
     */
    public static Number asInteger(int pValue) {
        return Integer.valueOf(pValue);
    }

    /**
     * Creates a new integer value.
     * @param pValue Returns a number with the given value.
     * @return A number with the given value.
     */
    public static Number asInteger(long pValue) {
        return Long.valueOf(pValue);
    }

    /**
     * Creates a new integer value.
     * @param pValue Returns a number with the given value.
     * @return A number with the given value.
     */
    public static Number asInteger(String pValue) {
        try {
            int value = Integer.parseInt(pValue);
            return asInteger(value);
        } catch (Exception e1) {
            try {
                return Long.valueOf(pValue);
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
     * @param pValue Returns a number with the given value.
     * @return A number with the given value.
     */
    public static Number asFloatingPoint(float pValue) {
        return Float.valueOf(pValue);
    }

    /**
     * Creates a new floating point value.
     * @param pValue Returns a number with the given value.
     * @return A number with the given value.
     */
    public static Number asFloatingPoint(double pValue) {
        return Double.valueOf(pValue);
    }

    /**
     * Creates a new floating point value.
     * @param pValue Returns a number with the given value.
     * @return A number with the given value.
     */
    public static Number asFloatingPoint(String pValue) {
        try {
            return Float.valueOf(pValue);
        } catch (Exception e1) {
            try {
                return Double.valueOf(pValue);
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
