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

/** A mutable {@link Number} with the properties of a {@link Long}.
 */
public class MutableLong extends Number implements Comparable<MutableLong> {
	private static final long serialVersionUID = 3459405672114578607L;
	/** The current value.
	 */
	private long value;

	/**
	 * Returns the primitive number.
	 * @return The primitive number.
	 */
    public long getValue() {
        return value;
    }
	/**
	 * Sets the primitive number.
	 * @param pValue The primitive number.
	 */
    public void setValue(long pValue) {
        value = pValue;
    }

    @Override
	public int compareTo(MutableLong pOtherMutableLong) {
    	return Long.compare(value, pOtherMutableLong.getValue());
	}

    @Override
	public boolean equals(Object pObj) {
    	// This Long is mutable, so we can't simply compare the values.
		return super.equals(pObj);
	}

	@Override
	public int hashCode() {
    	// This Long is mutable, so we can't simply use the values hash code.
		return super.hashCode();
	}

    @Override
	public int intValue() {
    	return (int) value;
	}

    @Override
	public long longValue() {
    	return value;
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
    public long inc() {
    	setValue(getValue()+1);
    	return getValue();
    }

    /**
     * Decrements the primitive number by one, and returns the old value.
     * This is basically a post-decrement.
     * @return The previous (non-decremented) value.
     */
    public long dec() {
    	long value = getValue();
    	setValue(value-1);
    	return value;
    }
}
