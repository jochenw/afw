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

/**
 *
 * @author jwi
 */
public class MutableInteger extends Number implements Comparable<MutableInteger> {
	private static final long serialVersionUID = 5486916587259114022L;
	private int value;

    public int getValue() {
        return value;
    }
    public void setValue(int pValue) {
        value = pValue;
    }

    @Override
	public int compareTo(MutableInteger pOthetMutableInteger) {
    	return Integer.compare(value, pOthetMutableInteger.getValue());
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
}
