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
}
