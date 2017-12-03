/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
