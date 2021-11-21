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

import java.io.Serializable;

import com.github.jochenw.afw.core.util.Functions.FailableRunnable;

/**
 * Utility class, providing the ability to store a mutable boolean value.
 */
public class MutableBoolean implements Serializable, Comparable<MutableBoolean> {
	private static final long serialVersionUID = 3402519841525500747L;
	private boolean value;

	/** Returns the current boolean value.
	 * @return The current boolean value.
	 */
    public boolean getValue() {
        return value;
    }

	/** Sets the current boolean value.
	 * @param pValue The current boolean value.
	 */
    public void setValue(boolean pValue) {
        value = pValue;
    }

    @Override
	public int compareTo(MutableBoolean pOtherMutableBoolean) {
		return Boolean.compare(value, pOtherMutableBoolean.getValue());
	}

    @Override
	public boolean equals(Object pObj) {
    	// This Boolean is mutable, so we can't simply compare the values.
		return super.equals(pObj);
	}

	@Override
	public int hashCode() {
    	// This Boolean is mutable, so we can't simply use the values hash code.
		return super.hashCode();
	}

	/**
     * Shortcut for {@link #setValue(boolean) setValue(true)}.
     */
	public void set() {
		setValue(true);
	}

    /**
     * Shortcut for {@link #getValue()}.
	 * @return The current boolean value.
     */
	public boolean isSet() {
		return value;
	}

	/**
	 * Asserts the mutable booleans current state by invoking the given {@link FailableRunnable},
	 * if the mutable boolean isn't set.
	 * @param pRunnable The runnable to invoke, if the mutable boolean isn't set.
	 */
	public void assertTrue(FailableRunnable<?> pRunnable) {
		assertValue(true, pRunnable);
	}

	/**
	 * Asserts the mutable booleans current state by invoking the given {@link FailableRunnable},
	 * if the mutable boolean is set.
	 * @param pRunnable The runnable to invoke, if the mutable boolean is set.
	 */
	public void assertFalse(FailableRunnable<?> pRunnable) {
		assertValue(false, pRunnable);
	}

	/**
	 * Asserts the mutable booleans current state by invoking the given {@link FailableRunnable},
	 * if the mutable boolean isn't as given by the parameter {@link pExpect}.
	 * @param pExpect The expected state of the mutable boolean.
	 * @param pRunnable The runnable to invoke, if the mutable boolean is set.
	 */
	public void assertValue(boolean pExpect, FailableRunnable<?> pRunnable) {
		if (value != pExpect) {
			Functions.run(pRunnable);
		}
	}

}
