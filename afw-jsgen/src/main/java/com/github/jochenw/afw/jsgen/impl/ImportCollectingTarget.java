package com.github.jochenw.afw.jsgen.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.api.JSGQName;

public class ImportCollectingTarget implements JSGSourceTarget {
	public static class CountedName {
		private int count;
		private final JSGQName name;
		private final CountedName next;

		public CountedName(JSGQName pName, CountedName pNext) {
			name = pName;
			next = pNext;
		}
	}

	private final Map<String,CountedName> countedNames = new HashMap<>();
	private Set<JSGQName> importedNames;
	private Function<Object,Object> filter;

	public Function<Object, Object> getFilter() {
		return filter;
	}

	public void setFilter(Function<Object, Object> filter) {
		this.filter = filter;
	}

	@Override
	public void write(@Nonnull Object pObject) throws IOException {
		final Object o;
		if (filter == null) {
			o = pObject;
		} else {
			o = filter.apply(pObject);
		}
		if (o instanceof JSGQName) {
			if (importedNames != null) {
				throw new IllegalStateException("Object is already closed.");
			}
			@Nonnull final JSGQName name = (JSGQName) o;
			if (!name.isPrimitive()) {
				CountedName counter = findCountedName(name);
				counter.count++;
			}
		} else {
			// Do nothing
		}
	}

	@Nonnull protected CountedName findCountedName(@Nonnull JSGQName pName) {
		final CountedName firstCn = countedNames.get(pName.getClassName());
		if (firstCn != null) {
			for (CountedName cn = firstCn;  cn != null; cn = cn.next) {
				if (cn.name.equals(pName)) {
					return cn;
				}
			}
		}
		final CountedName newCn = new CountedName(pName, firstCn);
		countedNames.put(pName.getClassName(), newCn);
		return newCn;
	}
	
	@Override
	public void newLine() {
		// Do nothing
	}

	@Override
	public void close() {
		if (importedNames == null) {
			importedNames = new HashSet<>();
			for(CountedName cn : countedNames.values()) {
				final JSGQName name = findMaxCount(cn);
				importedNames.add(name);
			}
		}
	}

	protected JSGQName findMaxCount(@Nonnull CountedName pCn) {
		JSGQName name = null;
		int max = 0;
		for (CountedName cn = pCn;  cn != null;  cn = cn.next) {
			if (name == null  ||  cn.count > max) {
				max = cn.count;
				name = cn.name;
			}
		}
		return name;
	}

	public List<JSGQName> getImportedNames() {
		close();
		return new ArrayList<>(importedNames);
	}
}
