package com.github.jochenw.afw.jsgen.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.util.AbstractBuilder;
import com.github.jochenw.afw.jsgen.util.Objects;


public abstract class JSGSubroutineBuilder<T extends JSGSubroutineBuilder<T>> extends AbstractBuilder<T> implements JSGSubroutine {
	private final AnnotationSet annotations = new AnnotationSet();
	private final List<JSGSubroutine.Parameter> parameters = new ArrayList<>();
	private final List<JSGQName> exceptions = new ArrayList<>();
	private Protection protection;
	private JSGBlock body;
	private JSGClass sourceClass;

	@Override
	public AnnotationSet getAnnotations() {
		return annotations;
	}

	@Nonnull public T protection(@Nonnull IProtectable.Protection pProtection) {
		assertMutable();
		protection = pProtection;
		return self();
	}

	@Nonnull public T makePublic() {
		return protection(Protection.PUBLIC);
	}

	@Nonnull public T makeProtected() {
		return protection(Protection.PROTECTED);
	}

	@Nonnull public T makePackageProtected() {
		return protection(Protection.PACKAGE);
	}

	@Nonnull public T makePrivate() {
		return protection(Protection.PRIVATE);
	}

	@Override
	public Protection getProtection() {
		return protection;
	}

	@Override
	public JSGBlock body() {
		if (body == null) {
			body = new JSGBlock();
		}
		return body;
	}

	@Override
	public List<Parameter> getParameters() {
		return parameters;
	}

	@Nonnull T exception(@Nonnull String pType) {
		return exception(JSGQName.valueOf(pType));
	}

	@Nonnull T exception(@Nonnull Class<?> pType) {
		return exception(JSGQName.valueOf(pType));
	}

	@Nonnull T exception(@Nonnull JSGQName pType) {
		assertMutable();
		exceptions.add(Objects.requireNonNull(pType, "Exception"));
		return self();
	}

	@Override
	public List<JSGQName> getExceptions() {
		return exceptions;
	}

	public JSGSubroutine.ParameterBuilder parameter(@Nonnull JSGQName pType, @Nonnull String pName) {
		assertMutable();
		final JSGSubroutine.ParameterBuilder pb = new JSGSubroutine.ParameterBuilder().type(pType).name(pName);
		parameters.add(pb);
		return pb;
	}

	@Nonnull T suppressWarning(String pValue) {
		return suppressWarning(pValue, true);
	}

	private static final JSGQName SUPPRESSWARNINGS = JSGQName.valueOf(SuppressWarnings.class);
	
	@Nonnull T suppressWarning(String pValue, boolean pSuppressing) {
		final Annotation annotation = getAnnotation(SUPPRESSWARNINGS);
		if (annotation == null) {
			if (pSuppressing) {
				annotation(SUPPRESSWARNINGS).attribute("unchecked", pValue);
			}
		} else {
			List<String> values = new ArrayList<>();
			final Object uncheckedValue = annotation.getAttributeValue("unchecked");
			if (uncheckedValue != null) {
				if (uncheckedValue instanceof String) {
					values.add((String) uncheckedValue);
				} else if (uncheckedValue instanceof String[]) {
					final String[] array = (String[]) uncheckedValue;
					values.addAll(Arrays.asList(array));
				}
			}
			final boolean modified;
			if (values.contains(pValue)) {
				if (!pSuppressing) {
					values.remove(pValue);
					modified = true;
				} else {
					modified = false;
				}
			} else {
				if (pSuppressing) {
					values.add(pValue);
					modified = true;
				} else {
					modified = false;
				}
			}
			if (modified) {
				annotation.attribute("unchecked", values.toArray(new String[values.size()]));
			}
		}
		return self();
	}

	@Override
	public boolean isSuppressing(String pValue) {
		final Annotation annotation = getAnnotation(SUPPRESSWARNINGS);
		if (annotation == null) {
			return false;
		} else {
			final Object uncheckedValue = annotation.getAttributeValue("unchecked");
			if (uncheckedValue == null) {
				return false;
			} else if (uncheckedValue instanceof String) {
				return pValue.equals(uncheckedValue);
			} else if (uncheckedValue instanceof String[]) {
				final String[] values = (String[]) uncheckedValue;
				for (String v : values) {
					if (pValue.equals(v)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	@Override
	@Nonnull
	public JSGClass getSourceClass() {
		return sourceClass;
	}

	@Nonnull public T sourceClass(JSGClass pClass) {
		assertMutable();
		sourceClass = pClass;
		return self();
	}
}
