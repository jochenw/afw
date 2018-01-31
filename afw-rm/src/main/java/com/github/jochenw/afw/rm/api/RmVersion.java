package com.github.jochenw.afw.rm.api;

import java.util.Arrays;

import com.github.jochenw.afw.rm.util.Strings;

public class RmVersion implements Comparable<RmVersion> {
	private final int[] versionNumbers;

	public RmVersion(int[] pVersionNumbers) {
		versionNumbers = pVersionNumbers;
	}
	
	@Override
	public int compareTo(RmVersion o) {
		final int[] vn1 = versionNumbers;
		final int[] vn2 = o.versionNumbers;
		final int length = Math.min(vn1.length, vn2.length);
		for (int i = 0;  i < length;  i++) {
			final int num = vn1[i] - vn2[i];
			if (num != 0) {
				return num;
			}
		}
		final int n1 = (vn1.length > length) ? vn1[length] : 0;
		final int n2 = (vn2.length > length) ? vn2[length] : 0;
		return n1 - n2;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(versionNumbers);
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther == null  ||  getClass() != pOther.getClass()) {
			return false;
		}
		RmVersion other = (RmVersion) pOther;
		return versionNumbers.length == other.versionNumbers.length  &&  compareTo(other) == 0;
	}

	public static RmVersion of(String pVersionStr) {
		return new RmVersion(Strings.parseVersionNumber(pVersionStr));
	}

	public int[] getNumbers() {
		return versionNumbers;
	}
}
