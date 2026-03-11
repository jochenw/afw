package com.github.jochenw.afw.di.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.DiUtils;

/** Simple, leightweight, but fast implementation of {@IComponentFactory}.
 */
public class SimpleComponentFactory extends AbstractComponentFactory {
	/** Creates a new instance with the given configuration.
	 * @param pConfiguration The created instances configuration.
	 */
	public SimpleComponentFactory(Configuration pConfiguration) {
		super(pConfiguration);
		bindings = Collections.unmodifiableMap(pConfiguration.getBindings());
		annotationProvider = pConfiguration.getAnnotationProvider();
		staticInjectionClasses = pConfiguration.getStaticInjectionClasses();
		pConfiguration.getBindings().keySet().forEach((k) -> {
			final Class<? extends Annotation> annotationType = k.getAnnotationType();
			if (annotationType != null) {
				annotationClasses.add(annotationType);
			}
			final Annotation annotation = k.getAnnotation();
			if (annotation != null) {
				annotationClasses.add(annotation.annotationType());
			}
		});
	}

	private final Map<Key<Object>,IBinding<Object>> bindings;
	private final ConcurrentMap<Class<Object>,ClassMetaData> metaDatas = new ConcurrentHashMap<>();
	private final Set<Class<? extends Annotation>> annotationClasses = new HashSet<>();
	private final Set<Class<?>> staticInjectionClasses;

	/** An object, which holds the information, that the component factory needs to
	 * create, or initialize instances of one particular class.
	 */
	public static class ClassMetaData {
		private final Consumer<Object> initializer;
		private Supplier<Object> instantiator;
		private final Supplier<Supplier<Object>> instantiatorProvider;

		/** Creates a new instance with the given initializer, and the
		 * given provider of an instantiator.
		 * @param pInitializer The initializer is internally used by
		 *   {@link SimpleComponentFactory#init(Object)} to initialize
		 *   an object.
		 * @param pInstantiatorProvider The instantiator provider is
		 *   invoked, on demand, to create an instantiator, which will
		 *   then be used internally, to create an instance of a
		 *   particular class.
		 */
		public ClassMetaData(Consumer<Object> pInitializer, Supplier<Supplier<Object>> pInstantiatorProvider) {
			initializer = pInitializer;
			instantiatorProvider = pInstantiatorProvider;
		}

		/** Returns an instantiator by invoking the
		 * {@link #instantiatorProvider}, if necessary.
		 * @return The created instantiator.
		 */
		public synchronized Supplier<Object> getInstantiator() {
			if (instantiator == null) {
				instantiator = Objects.requireNonNull(instantiatorProvider.get(), "Instantiator");
			}
			return instantiator;
		}
		/** Returns the initializer.
		 * @return The initializer.
		 */
		public Consumer<Object> getInitializer() {
			return initializer;
		}
	}
	private final IAnnotationProvider annotationProvider;

	@Override
	public <T> IBinding<T> getBinding(Key<T> pKey) {
		@SuppressWarnings("unchecked")
		final IBinding<T> binding = (IBinding<T>) bindings.get(pKey);
		return binding;
	}

	protected ClassMetaData getMetaData(Class<?> pType) {
		@SuppressWarnings("unchecked")
		final Class<Object> type = (Class<Object>) Objects.requireNonNull(pType, "Type");
		return metaDatas.computeIfAbsent(type, (tp) -> {
			final Consumer<Object> initializer = newInitializer(tp);
			final Supplier<Supplier<Object>> instantiatorProvider = () -> newInstantiator(tp);
			return new ClassMetaData(initializer, instantiatorProvider);
		});
	}

	protected Supplier<Object> newInstantiator(Class<Object> pType) {
		for (Constructor<?> constructor : pType.getDeclaredConstructors()) {
			if (!annotationProvider.isInjectable(constructor)) {
				continue;
			}
			return newInstantiator(constructor);
		}
		final Constructor<Object> defaultConstructor;
		try {
			defaultConstructor = pType.getConstructor();
		} catch (Exception e) {
			throw new IllegalStateException("No suitable constructor (annotated with @Inject, or public default)" 
					+ " found to create an instance of class " + pType, e);
		}
		return () -> {
			try {
				return defaultConstructor.newInstance();
			} catch (Exception e) {
				throw DiUtils.show(e);
			}
			
		};
	}

	protected Supplier<Object> newInstantiator(Constructor<?> pConstructor) {
		@SuppressWarnings("unchecked")
		final Constructor<Object> constructor = (Constructor<Object>) Objects.requireNonNull(pConstructor, "Constructor");
		final AnnotatedType[] parameters = constructor.getAnnotatedParameterTypes();
		@SuppressWarnings("unchecked")
		final IBinding<Object>[] bindings = (IBinding<Object>[]) Array.newInstance(IBinding.class, parameters.length);
		for (int i = 0;  i < parameters.length;  i++) {
			final int index = i;
			final Supplier<String> descriptor = () -> {
				final StringBuilder sb = new StringBuilder();
				sb.append("constructor ");
				sb.append(constructor.getDeclaringClass().getSimpleName());
				sb.append("(");
				for (int j = 0;  j < parameters.length;  j++) {
					if (j > 0) {
						sb.append(", ");
					}
					final String parameterClassName = parameters[index].getType().getTypeName();
					final int packageNameEnd = parameterClassName.lastIndexOf('.');
					final String appendedClassName;
					if (packageNameEnd == -1) {
						appendedClassName = parameterClassName;
					} else {
						final String packageName = parameterClassName.substring(0, packageNameEnd);
						if ("java.lang".equals(packageName)) {
							appendedClassName = parameterClassName.substring(packageNameEnd+1);
						} else {
							appendedClassName = parameterClassName;
						}
					}
					sb.append(appendedClassName);
				}
				sb.append(") in class " + constructor.getDeclaringClass().getName());
				return sb.toString();
			};
			bindings[i] = requireBinding(parameters[i], descriptor);
		}
		return () -> {
			final Object[] values = new Object[bindings.length];
			DiUtils.assertAccessible(constructor);
			try {
				return constructor.newInstance(values);
			} catch (Exception e) {
				throw DiUtils.show(e);
			}
		};
	}

	protected Consumer<Object> newInitializer(Class<Object> pType) {
		final List<Consumer<Object>> initializerList = new ArrayList<>();
		findMethods(pType, initializerList::add);
		findFields(pType, initializerList::add);
		return (o) -> {
			initializerList.forEach((i) -> i.accept(o));
			if (o instanceof IComponentFactoryAware) {
				((IComponentFactoryAware) o).init(SimpleComponentFactory.this);
			}
		};
	}

	protected void findMethods(Class<Object> pType, Consumer<Consumer<Object>> pInitializerConsumer) {
		Class<Object> cl = pType;
		do {
			for (final Method method : cl.getDeclaredMethods()) {
				// Ignore abstract methods.
				if (Modifier.isAbstract(method.getModifiers())) {
					continue;
				}
				// Ignore methods without @Inject
				if (!annotationProvider.isInjectable(method)) {
					continue;
				}
				// Ignore static methods, unless static injection has been requested for the class.
				if (Modifier.isStatic(method.getModifiers())  &&  !staticInjectionClasses.contains(method.getDeclaringClass())) {
					continue;
				}
				final AnnotatedType[] parameters = method.getAnnotatedParameterTypes();
				@SuppressWarnings("unchecked")
				final IBinding<Object>[] bindings = (IBinding<Object>[]) Array.newInstance(IBinding.class, parameters.length);
				for (int i = 0;  i < parameters.length;  i++) {
					final int index = i;
					final Supplier<String> descriptor = () -> "parameter " + index + " in method " + method.getName()
						+ " of class " + method.getDeclaringClass().getName();
					bindings[i] = requireBinding(parameters[i], descriptor);
				}
				final Consumer<Object> initializer = (o) -> {
					final Object[] values = new Object[bindings.length];
					for (int j = 0;  j < bindings.length;  j++) {
						values[j] = bindings[j].getSupplier().apply(SimpleComponentFactory.this);
					}
					DiUtils.assertAccessible(method);
					try {
						method.invoke(o, values);
					} catch (Exception e) {
						throw DiUtils.show(e);
					}
				};
				pInitializerConsumer.accept(initializer);
			}
			cl = cl.getSuperclass();
		} while (cl != null  &&  cl != Object.class);
	}

	protected void findFields(Class<Object> pType, Consumer<Consumer<Object>> pInitializerConsumer) {
		Class<Object> cl = pType;
		do {
			for (final Field field : cl.getDeclaredFields()) {
				// Ignore fields without @Inject
				if (!annotationProvider.isInjectable(field)) {
					continue;
				}
				// Ignore static fields, unless static injection has been requested for the class.
				if (Modifier.isStatic(field.getModifiers())  &&  !staticInjectionClasses.contains(field.getDeclaringClass())) {
					continue;
				}
				final AnnotatedType type = field.getAnnotatedType();
				final Supplier<String> descriptor = () -> "field " + field.getName() + " in class " + field.getDeclaringClass().getName();
				final IBinding<Object> binding = requireBinding(type, descriptor);
				final Consumer<Object> initializer = (o) -> {
					final Object value = binding.getSupplier().apply(SimpleComponentFactory.this);
					DiUtils.assertAccessible(field);
					try {
						field.set(o, value);
					} catch (Exception e) {
						throw DiUtils.show(e);
					}
				};
				pInitializerConsumer.accept(initializer);
			}
			cl = cl.getSuperclass();
		} while (cl != null  &&  cl != Object.class);
	}
	
	/** Performs a lookup for a binding, which is suitable for injecting a value into the given type.
	 * @param pType The type of the method parameter, or field, which needs a binding.
	 * @param pDescriptor Description of the method parameter, or field, for use in
	 *   error messages.
	 * @return The required binding. Never null.
	 * @throws IllegalStateException No suitable binding has been found.
	 */
	protected IBinding<Object> requireBinding(AnnotatedType pType, Supplier<String> pDescriptor) {
		final Type type = pType.getType();
		String name = annotationProvider.getNamedValue(pType);
		if (name == null) {
			name = "";
		}
		IBinding<Object> binding = null;
		for (Annotation annotation : pType.getAnnotations()) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			if (annotationClasses.contains(annotationType)) {
				final Key<Object> key1 = Key.of(type, name, null, annotation);
				final IBinding<Object> bndng1 = bindings.get(key1);
				if (bndng1 != null) {
					return bndng1;
				}
				if (binding == null) {
					final Key<Object> key2 = Key.of(type, name, annotationType, null);
					final IBinding<Object> bndng2 = bindings.get(key2);
					binding = bndng2;
				}
			}
		}
		if (binding == null) {
			final Key<Object> key = Key.of(type, name);
			binding = bindings.get(key);
			if (binding == null) {
				throw new IllegalStateException("No suitable binding found for " + pDescriptor.get());
			}
		}
		return binding;
	}
	
	@Override
	public void init(Object pObject) {
		final ClassMetaData cmd = getMetaData(Objects.requireNonNull(pObject, "Object").getClass());
		cmd.getInitializer().accept(pObject);
	}

	@Override
	public <T> Supplier<T> getInstantiator(Class<? extends T> pImplType) {
		final ClassMetaData cmd = getMetaData(Objects.requireNonNull(pImplType, "ImplType"));
		final Supplier<Object> supplier = cmd.getInstantiator();
		@SuppressWarnings("unchecked")
		final Supplier<T> result = (Supplier<T>) supplier;
		return result;
	}

	@Override
	public <T> Supplier<T> getInstantiator(Constructor<? extends T> pConstructor) {
		final Supplier<Object> supplier = newInstantiator(pConstructor);
		@SuppressWarnings("unchecked")
		final Supplier<T> result = (Supplier<T>) supplier;
		return result;
	}


}
