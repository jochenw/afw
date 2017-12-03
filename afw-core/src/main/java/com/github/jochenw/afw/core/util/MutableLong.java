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
public class MutableLong extends Number implements Comparable<MutableLong> {
	private static final long serialVersionUID = 3459405672114578607L;
	private long value;

    public long getValue() {
        return value;
    }
    public void setValue(long pValue) {
        value = pValue;
    }

    @Override
	public int compareTo(MutableLong pOtherMutableLong) {
    	return Long.compare(value, pOtherMutableLong.getValue());
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
}
