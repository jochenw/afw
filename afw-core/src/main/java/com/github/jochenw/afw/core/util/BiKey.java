/**
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

import java.util.Objects;


/** A combined key for use in maps. It overwrites {@link Object#hashCode()},
 * and {@link Object#equals(Object)} in a way, that two instances of TwoPartKey
 * are equal, exactly, if the two parts are equal.
 */
public class BiKey<O1,O2> {
	private final O1 part1;
	private final O2 part2;

	BiKey(O1 pPart1, O2 pPart2) {
		Objects.requireNonNull(pPart1, "Part1");
		Objects.requireNonNull(pPart2, "Part2");
		part1 = pPart1;
		part2 = pPart2;
	}

	public O1 getPart1() { return part1; }
	public O2 getPart2() { return part2; }

	@Override
	public int hashCode() {
		final int hc1 = part1 == null ? 0 : part1.hashCode();
		final int hc2 = part2 == null ? 0 : part2.hashCode();
		return 31 * (31 * 1 + hc1) + hc2;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		BiKey<O1,O2> other = (BiKey<O1,O2>) obj;
		if (part1 == null) {
			if (part2 == null) {
				return other.part1 == null  &&  other.part2 == null;
			} else {
				return other.part1 == null  &&  part2.equals(other.part2);
			}
		} else {
			if (part2 == null) {
				return part1.equals(other.part1)  &&  other.part2 == null;
			} else {
				return part1.equals(other.part1)  &&  part2.equals(other.part2);
			}
		}
	}
}
