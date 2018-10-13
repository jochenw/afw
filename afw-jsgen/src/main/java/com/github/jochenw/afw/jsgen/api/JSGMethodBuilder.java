package com.github.jochenw.afw.jsgen.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.util.AbstractBuilder;
import com.github.jochenw.afw.jsgen.util.Objects;


public class JSGMethodBuilder extends AbstractBuilder implements JSGMethod {
	public static class ParameterBuilder extends AbstractBuilder implements Parameter {
		private final AnnotationSet annotations = new AnnotationSet();
		private JSGQName type;
		private String name;

		@Override
		public AnnotationSet getAnnotations() {
			return annotations;
		}

		public ParameterBuilder name(String pName) {
			assertMutable();
			name = pName;
			return this;
		}

		@Override
		public String getName() {
			return name;
		}

		public ParameterBuilder type(JSGQName pType) {
			assertMutable();
			type = pType;
			return this;
		}

		@Override
		public JSGQName getType() {
			return type;
		}

		@Override
		public Parameter build() {
			return (Parameter) super.build();
		}
	}
	private final AnnotationSet annotations = new AnnotationSet();
	private final List<Parameter> parameters = new ArrayList<>();
	private final List<JSGQName> exceptions = new ArrayList<>();
	@Nonnull private JSGQName type;
	@Nonnull private IProtectable.Protection protection;
	@Nonnull private String name;
	private boolean isStatic;
	private boolean isAbstract;
	private boolean isFinal;
	private boolean isSynchronized;
	private JSGBlock body;

	@Override
	public JSGMethod build() {
		return (JSGMethod) super.build();
	}

	@Override
	public AnnotationSet getAnnotations() {
		return annotations;
	}

	@Nonnull public JSGMethodBuilder returnType(@Nonnull JSGQName pType) {
		assertMutable();
		type = pType;
		return this;
	}

	@Nonnull public JSGMethodBuilder returnType(@Nonnull Class<?> pType) {
		return returnType(JSGQName.valueOf(pType));
	}

	@Nonnull public JSGMethodBuilder returnType(@Nonnull String pType) {
		return returnType(JSGQName.valueOf(pType));
	}

	@Override
	@Nonnull public JSGQName getReturnType() {
		return type;
	}

	@Nonnull public JSGMethodBuilder protection(@Nonnull IProtectable.Protection pProtection) {
		assertMutable();
		protection = pProtection;
		return this;
	}

	@Nonnull public JSGMethodBuilder makePublic() {
		return protection(Protection.PUBLIC);
	}

	@Nonnull public JSGMethodBuilder makeProtected() {
		return protection(Protection.PROTECTED);
	}

	@Nonnull public JSGMethodBuilder makePackageProtected() {
		return protection(Protection.PACKAGE);
	}

	@Nonnull public JSGMethodBuilder makePrivate() {
		return protection(Protection.PRIVATE);
	}

	@Override
	@Nonnull public IProtectable.Protection getProtection() {
		return protection;
	}

	@Nonnull JSGMethodBuilder name(@Nonnull String pName) {
		assertMutable();
		name = pName;
		return this;
	}

	@Override
	@Nonnull public String getName() {
		return name;
	}

	public ParameterBuilder parameter(@Nonnull JSGQName pType, @Nonnull String pName) {
		assertMutable();
		final ParameterBuilder pb = new ParameterBuilder().type(pType).name(pName);
		parameters.add(pb);
		return pb;
	}

	@Override
	public List<Parameter> getParameters() {
		return parameters;
	}

	@Nonnull JSGMethodBuilder makeAbstract() {
		return makeAbstract(true);
	}
	
	@Nonnull JSGMethodBuilder makeAbstract(boolean pAbstract) {
		assertMutable();
		isAbstract = pAbstract;
		return this;
	}
	
	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Nonnull JSGMethodBuilder makeStatic() {
		return makeStatic(true);
	}
	
	@Nonnull JSGMethodBuilder makeStatic(boolean pStatic) {
		assertMutable();
		isStatic = pStatic;
		return this;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Nonnull JSGMethodBuilder makeFinal() {
		return makeFinal(true);
	}

	@Nonnull JSGMethodBuilder makeFinal(boolean pFinal) {
		assertMutable();
		isFinal = pFinal;
		return this;
	}

	@Override
	public boolean isFinal() {
		return isFinal;
	}

	@Nonnull JSGMethodBuilder makeSynchronized() {
		return makeSynchronized(true);
	}

	@Nonnull JSGMethodBuilder makeSynchronized(boolean pSynchronized) {
		assertMutable();
		isSynchronized = pSynchronized;
		return this;
	}

	@Override
	public boolean isSynchronized() {
		return isSynchronized;
	}
	
	@Override
	public JSGBlock body() {
		if (body == null) {
			body = new JSGBlock();
		}
		return body;
	}

	@Nonnull JSGMethodBuilder exception(@Nonnull JSGQName pException) {
		assertMutable();
		exceptions.add(Objects.requireNonNull(pException, "Exception"));
		return this;
	}

	@Override
	@Nonnull
	public List<JSGQName> getExceptions() {
		return exceptions;
	}

	private static final JSGQName OVERRIDE = JSGQName.valueOf(Override.class);
	private static final JSGQName SUPPRESSWARNINGS = JSGQName.valueOf(SuppressWarnings.class);

	@Nonnull JSGMethodBuilder overriding() {
		return overriding(true);
	}

	@Nonnull JSGMethodBuilder overriding(boolean pOverriding) {
		assertMutable();
		if (pOverriding) {
			annotation(OVERRIDE);
		}
		return this;
	}
	
	@Override
	public boolean isOverriding() {
		return isAnnotatedWith(OVERRIDE);
	}

	@Nonnull JSGMethodBuilder suppressWarning(String pValue) {
		return suppressWarning(pValue, true);
	}

	@Nonnull JSGMethodBuilder suppressWarning(String pValue, boolean pSuppressing) {
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
		return this;
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
}
