package com.github.jochenw.afw.jsgen.api;

import java.util.HashMap;
import java.util.Map;

public class JSGSourceFactory {
	public static class SourceExistsException extends RuntimeException {
		private static final long serialVersionUID = -1988888513322357177L;
		private final JSGQName name;

		public SourceExistsException(JSGQName pName, String pMessage) {
			super(pMessage);
			name = pName;
		}

		public JSGQName getName() {
			return name;
		}
	}

	private final Map<String, Object> map = new HashMap<>();
	JSGSourceBuilder newSourceBuilder(JSGQName pName) throws SourceExistsException {
		final String id = getId(pName);
		if (map.containsKey(id)) {
			throw new SourceExistsException(pName, "Object already exists: " + id + ", " + map.get(id));
		}
		final JSGSourceBuilder builder = new JSGSourceBuilder(this, pName);
		map.put(id, builder);
		return builder;
	}
	JSGSourceBuilder getSourceBuilder(JSGQName pName) {
		final String id = getId(pName);
		final Object value = map.get(id);
		if (value == null) {
			return null;
		} else {
			if (value instanceof JSGSourceBuilder) {
				return (JSGSourceBuilder) value;
			}
			throw new IllegalStateException("Expected JSGSourceBuilder, got " + value.getClass().getName() + " for id: " + id);
		}
	}

	private String getId(JSGQName pName) {
		return pName.getQName().replace('.', '/') + ".class";
	}
}
