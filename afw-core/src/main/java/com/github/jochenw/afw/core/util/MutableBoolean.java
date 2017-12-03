/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jochenw.afw.core.util;

import java.io.Serializable;

/**
 *
 * @author jwi
 */
public class MutableBoolean implements Serializable, Comparable<MutableBoolean> {
	private static final long serialVersionUID = 3402519841525500747L;
	private boolean value;

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean pValue) {
        value = pValue;
    }

    @Override
	public int compareTo(MutableBoolean pOtherMutableBoolean) {
		return Boolean.compare(value, pOtherMutableBoolean.getValue());
	}
}
