package com.github.jochenw.afw.bootstrap.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;


public class Versions {
	public static class Version {
		private final String versionStr;
		private final int[] numbers;

		public Version(String pVersionStr, int[] pNumbers) {
			versionStr = Objects.requireNonNull(pVersionStr, "Version String");
			numbers = Objects.requireNonNull(pNumbers, "Numbers");
		}

		public boolean isGreaterOrEqual(Version pOther) {
			final int[] n1 = numbers;
			final int[] n2 = pOther.numbers;
			final int length = Math.max(n1.length, n2.length);
			for (int i = 0;  i < length;  i++) {
				final int i1 = i >= n1.length ? 0 : n1[i];
				final int i2 = i >= n2.length ? 0 : n2[i];
				if (i1 < i2) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() { return versionStr; }

		public int[] getNumbers() {
			return numbers;
		}
	}

	public static Version valueOf(String pVersionStr) {
		return valueOf(pVersionStr, () -> "Invalid version number: " + pVersionStr);
	}

	public static Version valueOf(String pVersionStr, Supplier<String> pMsg) {
		final String vs = Objects.requireNonNull(pVersionStr, "Version String");
		if (vs.length() == 0) {
			throw new IllegalArgumentException(pMsg.get());
		}
		final List<Integer> integers = new ArrayList<Integer>();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < vs.length();  i++) {
			final char c = vs.charAt(i);
			if (Character.isDigit(c)) {
				sb.append(c);
			} else if ('.' == c) {
				final String s = sb.toString();
				if (s.length() == 0  &&  i < vs.length())  {
					if (integers.isEmpty()) {
					    integers.add(Integer.valueOf(0));	
					} else {
						throw new IllegalArgumentException(pMsg.get());
					}
				} else {
					final Integer intNumber;
					try {
						intNumber = Integer.parseInt(s);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException(pMsg.get());
					}
					integers.add(intNumber);
					sb.setLength(0);
				}
			} else {
				throw new IllegalArgumentException(pMsg.get());
			}
		}
		if (sb.length() > 0) {
			final String s = sb.toString();
			final Integer intNumber;
			try {
				intNumber = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(pMsg.get());
			}
			integers.add(intNumber);
		}
		final int[] numbers = new int[integers.size()];
		for (int i = 0;  i < integers.size();  i++) {
			numbers[i] = integers.get(i);
		}
		return new Version(vs, numbers);
	}
}
