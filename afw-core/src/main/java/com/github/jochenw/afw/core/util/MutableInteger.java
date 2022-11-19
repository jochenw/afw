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
package com.github.jochenw.afw.core.util;

/** A mutable {@link Number} with the properties of an {@link Integer}.
 */
public class MutableInteger extends Number implements Comparable<MutableInteger> {
	private static final long serialVersionUID = 5486916587259114022L;
	private int value;


	/**
	 * Returns the primitive number.
	 * @return The primitive number.
	 */
	public int getValue() {
        return value;
    }
	/**
	 * Sets the primitive number.
	 * @param pValue The primitive number.
	 */
    public void setValue(int pValue) {
        value = pValue;
    }

    @Override
	public int compareTo(MutableInteger pOtherMutableInteger) {
    	return Integer.compare(value, pOtherMutableInteger.getValue());
	}

    @Override
	public boolean equals(Object pObj) {
    	// This Integer is mutable, so we can't simply compare the values.
		return super.equals(pObj);
	}

	@Override
	public int hashCode() {
    	// This Integer is mutable, so we can't simply use the values hash code.
		return super.hashCode();
	}

    @Override
	public int intValue() {
    	return value;
	}

    @Override
	public long longValue() {
		return (long) value;
	}

    @Override
	public float floatValue() {
    	return (float) value;
	}

    @Override
	public double doubleValue() {
    	return (double) value;
	}

    /**
     * Increments the primitive number by one, and returns the result.
     * This is basically a pre-increment.
     * @return The incremented (new) value.
     */
    public int inc() {
    	setValue(getValue()+1);
    	return getValue();
    }

    /**
     * Decrements the primitive number by one, and returns the old value.
     * This is basically a post-decrement.
     * @return The previous (non-decremented) value.
     */
    public int dec() {
    	int value = getValue();
    	setValue(value-1);
    	return value;
    }
}
