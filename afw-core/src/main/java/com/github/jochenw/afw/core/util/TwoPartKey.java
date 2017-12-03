package com.github.jochenw.afw.core.util;

import java.util.Objects;


/** A combined key for use in maps. It overwrites {@link Object#hashCode()},
 * and {@link Object#equals(Object)} in a way, that two instances of TwoPartKey
 * are equal, exactly, if the two parts are equal.
 */
public class TwoPartKey {
	private final Object part1;
	private final Object part2;

	TwoPartKey(Object pPart1, Object pPart2) {
		Objects.requireNonNull(pPart1, "Part1");
		Objects.requireNonNull(pPart2, "Part2");
		part1 = pPart1;
		part2 = pPart2;
	}

	public Object getPart1() { return part1; }
	public Object getPart2() { return part2; }

	@Override
	public int hashCode() {
		final int result2 = 31 * (31 * 1 + part1.hashCode()) + part2.hashCode();
		return result2;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TwoPartKey other = (TwoPartKey) obj;
		return part1.equals(other.part1)  &&  part2.equals(other.part2);
	}
}
