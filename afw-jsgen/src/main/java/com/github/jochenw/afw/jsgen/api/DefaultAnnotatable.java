package com.github.jochenw.afw.jsgen.api;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

public class DefaultAnnotatable implements IAnnotatable {
	public static class DefaultAnnotation implements IAnnotation {
		private final JSGQName type;
		private Map<String,Object> values;

		DefaultAnnotation(JSGQName pType) {
			type = pType;
		}
		
		@Override
		public JSGQName getType() {
			return type;
		}

		@Override
		public Map<String, Object> getValues() {
			if (values == null) {
				return Collections.emptyMap();
			}
			return values;
		}
		
	}
	private Map<JSGQName,IAnnotation> annotations;

	@Override
	public IAnnotation getAnnotation(JSGQName pName) {
		if (annotations == null) {
			return null;
		}
		return annotations.get(pName);
	}

	@Override
	public IAnnotation getAnnotation(Class<? extends Annotation> pName) {
		final JSGQName name = JSGQName.newInstance(pName);
		return getAnnotation(name);
	}

	@Override
	public boolean isAnnotatedWith(JSGQName pName) {
		if (annotations == null) {
			return false;
		}
		return annotations.containsKey(pName);
	}

	@Override
	public boolean isAnnotatedWith(Class<? extends Annotation> pName) {
		final JSGQName name = JSGQName.newInstance(pName);
		return isAnnotatedWith(name);
	}

}
