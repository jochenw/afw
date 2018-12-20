package com.github.jochenw.afw.jsgen.api;

import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.jsgen.util.Objects;

public class JSGBlock {
	private List<Object> contents = new ArrayList<>();

	public void addLine(Object... pObjects) {
		contents.add(Objects.requireAllNonNull(pObjects, "Objects"));
	}

	public void addLine(Iterable<Object> pObjects) {
		contents.add(Objects.requireAllNonNull(pObjects, "Objects"));
	}

	public List<Object> getContents() {
		return contents;
	}
}
