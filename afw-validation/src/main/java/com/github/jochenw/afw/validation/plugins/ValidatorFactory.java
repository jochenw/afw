package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.validation.api.IValidationError;
import com.github.jochenw.afw.validation.api.IValidator;
import com.github.jochenw.afw.validation.plugins.IValidationPlugin.Provider;

public class ValidatorFactory {
	public interface TypedProvider<T> extends Provider<T> {
		Class<?> getType();
	}

	private static class ClassMetaData {
		private List<IPropertyValidator<Object>> atomicValidators;
		private IValidator validator;
		public List<IPropertyValidator<Object>> getAtomicValidators() {
			return atomicValidators;
		}
		public void setAtomicValidators(List<IPropertyValidator<Object>> pAtomicValidators) {
			atomicValidators = pAtomicValidators;
		}
		public void setValidator(IValidator pValidator) {
			validator = pValidator;
		}
		public IValidator getValidator() {
			return validator;
		}
	}
	private static final List<IValidationPlugin<? extends Annotation>> DEFAULT_PLUGINS = newDefaultPluginList();
	private final List<IValidationPlugin<? extends Annotation>> plugins = newPluginList();
	private final Map<String,ClassMetaData> map = new HashMap<>();

	private static List<IValidationPlugin<? extends Annotation>> newDefaultPluginList() {
		final List<IValidationPlugin<? extends Annotation>> list = new ArrayList<>();
		list.add(new NotNullValidationPlugin());
		list.add(new NotEmptyValidationPlugin());
		list.add(new AssertFalseValidationPlugin());
		list.add(new AssertTrueValidationPlugin());
		list.add(new LengthValidationPlugin());
		list.add(new NumberRangeValidationPlugin());
		list.add(new PatternValidationPlugin());
		return Collections.unmodifiableList(list);
	}

	protected List<IValidationPlugin<? extends Annotation>> newPluginList() {
		return DEFAULT_PLUGINS;
	}

	public IValidator getValidator(Object pObject) {
		final String id = pObject.getClass().getName();
		ClassMetaData classMetaData;
		synchronized(map) {
			classMetaData = map.get(id);
			if (classMetaData == null) {
				classMetaData = new ClassMetaData();
				map.put(id, classMetaData);
			}
		}
		synchronized(classMetaData) {
			List<IPropertyValidator<Object>> atomicValidators = classMetaData.getAtomicValidators();
			if (atomicValidators == null) {
				atomicValidators = newAtomicValidatorList(pObject.getClass());
				classMetaData.setAtomicValidators(atomicValidators);
				classMetaData.setValidator(newValidator(classMetaData));
			}
		}
		return classMetaData.getValidator();
	}

	protected List<IPropertyValidator<Object>> newAtomicValidatorList(Class<?> pClass) {
		final List<IPropertyValidator<Object>> list = new ArrayList<>();
		for (Method method : pClass.getDeclaredMethods()) {
			final Class<?> type = method.getReturnType();
			final int modifiers = method.getModifiers();
			if ((type != null  &&  type != Void.TYPE)  &&  method.getParameterCount() == 0
					&&  !Modifier.isStatic(modifiers)) {
				findAtomicValidators(list, method);
			}
		}
		for (Field field : pClass.getDeclaredFields()) {
			findAtomicValidators(list, field);
		}
		return list;
	}

	protected TypedProvider<Object> findAtomicValidators(final List<IPropertyValidator<Object>> list, AccessibleObject pAccessibleObject) {
		TypedProvider<Object> provider = null;
		for (Annotation annotation : pAccessibleObject.getAnnotations()) {
			for (IValidationPlugin<? extends Annotation> plugin : plugins) {
				if (plugin.isApplicable(annotation)) {
					if (provider == null) {
						provider = newProvider(pAccessibleObject);
					}
					@SuppressWarnings("unchecked")
					final IValidationPlugin<Annotation> pl = (IValidationPlugin<Annotation>) plugin;
					list.add(pl.getAtomicValidator(getProperty(pAccessibleObject), provider, annotation));
				}
			}
		}
		return provider;
	}

	protected String getProperty(AccessibleObject pAccessibleObject) {
		if (pAccessibleObject instanceof Field) {
			return ((Field) pAccessibleObject).getName();
		} else if (pAccessibleObject instanceof Method) {
			return ((Method) pAccessibleObject).getName();
		} else {
			throw new IllegalStateException("Invalid type of AccessibleObject: " + pAccessibleObject.getClass().getName());
		}
	}

	protected TypedProvider<Object> newProvider(AccessibleObject pAccessibleObject) {
		if (pAccessibleObject instanceof Field) {
			final Field field = (Field) pAccessibleObject;
			return new TypedProvider<Object>() {
				@Override
				public Object get(Object pObject) {
					synchronized(field) {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						try {
							return field.get(pObject);
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
					}
				}

				@Override
				public Class<?> getType() {
					return field.getType();
				}
			};
		} else if (pAccessibleObject instanceof Method) {
			final Method method = (Method) pAccessibleObject;
			return new TypedProvider<Object>() {
				@Override
				public Object get(Object pObject) {
					synchronized(method) {
						if (!method.isAccessible()) {
							method.setAccessible(true);
						}
						try {
							return method.invoke(pObject);
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
					}
				}

				@Override
				public Class<?> getType() {
					return method.getReturnType();
				}
				
			};
		} else {
			throw new IllegalStateException("Invalid type of AccessibleObject: " + pAccessibleObject.getClass().getName());
		}
	}

	protected IValidator newValidator(final ClassMetaData pMetaData) {
		return new IValidator() {
			@Override
			public void validate(String pContext, Object pObject, Handler pHandler) {
				for (IPropertyValidator<Object> validator : pMetaData.getAtomicValidators()) {
					final IValidationError error = validator.isValid(pContext, pObject);
					if (error != null) {
						pHandler.note(error, pObject);
					}
				}
			}
		};
	}
}
