package com.github.jochenw.afw.di.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.AbstractBindingProvider;
import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.api.IBindingProvider;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactory.IConfiguration;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.AtInjectBindingProvider;
import com.github.jochenw.afw.di.impl.DiUtils;


/** Simple, lightweight, but fast implementation of {@link IComponentFactory}.
 */
public class SimpleComponentFactory extends AbstractComponentFactory {
	private ConcurrentMap<Key<Object>,IBinding<Object>> bindings;
	private IAnnotationProvider annotationProvider;
	private final ConcurrentMap<Class<Object>,ClassMetaData> metaDatas = new ConcurrentHashMap<>();
	private final Set<Class<? extends Annotation>> annotationClasses = new HashSet<>();
	private Set<Class<?>> staticInjectionClasses;
	private List<IBindingProvider> bindingProviders;

	/** Creates a new instance. The created instance needs configuration
	 * by a call to {@link #init(IConfiguration)}, before using it.
	 */
	public SimpleComponentFactory() {
	}

	
	@Override
	public void init(IConfiguration pConfiguration) {
		super.init(pConfiguration);
		bindings = new ConcurrentHashMap<>();
		bindings.putAll(pConfiguration.getBindings());
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
		bindingProviders = new ArrayList<>();
		final AtInjectBindingProvider atInjectBindingProvider = new AtInjectBindingProvider();
		bindingProviders.add(atInjectBindingProvider);
		atInjectBindingProvider.init(pConfiguration, bindings);
		pConfiguration.getBindingProviders().forEach((bp) -> {
			if (bp instanceof AbstractBindingProvider abp) {
				abp.init(this, pConfiguration);
			}
			bindingProviders.add(bp);
		});
	}


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

	@Override
	public <T> IBinding<T> getBinding(Key<T> pKey) {
		@SuppressWarnings("unchecked")
		final IBinding<T> binding = (IBinding<T>) bindings.get(pKey);
		return binding;
	}

	/** Returns a metadata object for the given type. Internally,
	 * this performs a lookup in the {@code metaDatas} map. If
	 * a suitable object is found in the cache, returns it.
	 * Otherwise, creates a new object by invoking {@link #newInitializer(Class)},
	 * and {@link #newInstantiator(Class)}, caches, and returns it.
	 * @param pType The type, for which a metadata object is
	 * requested.
	 * @return The requested object, never null.
	 */
	protected ClassMetaData getMetaData(Class<?> pType) {
		@SuppressWarnings("unchecked")
		final Class<Object> type = (Class<Object>) Objects.requireNonNull(pType, "Type");
		return metaDatas.computeIfAbsent(type, (tp) -> {
			final Consumer<Object> initializer = newInitializer(tp);
			final Supplier<Supplier<Object>> instantiatorProvider = () -> newInstantiator(tp);
			return new ClassMetaData(initializer, instantiatorProvider);
		});
	}

	/** Creates a new instantiator. Internally, this works by finding a
	 * suitable constructor, which is annotated with @Inject. If such a
	 * constructor is found, invokes {@link #newInstantiator(Constructor)}.
	 * Otherwise, uses the default constructor (Public, no-args). 
	 * 
	 * @param pType The result type of the created instantiator.
	 * @return The created instantiator.
	 */
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

	/** Creates a new instantiator, which is implemented by the given
	 * constructor.
	 * @param pConstructor The constructor to use for creating the new
	 *   instance.
	 * @return The created injector.
	 */
	protected Supplier<Object> newInstantiator(Constructor<?> pConstructor) {
		@SuppressWarnings("unchecked")
		final Constructor<Object> constructor = (Constructor<Object>) Objects.requireNonNull(pConstructor, "Constructor");
		final AnnotatedType[] parameterAnnotations = constructor.getAnnotatedParameterTypes();
		final Type[] parameterTypes = constructor.getGenericParameterTypes();
		@SuppressWarnings("unchecked")
		final IBinding<Object>[] bindings = (IBinding<Object>[]) Array.newInstance(IBinding.class, parameterAnnotations.length);
		for (int i = 0;  i < parameterAnnotations.length;  i++) {
			final int index = i;
			final Supplier<String> descriptor = () -> {
				final StringBuilder sb = new StringBuilder();
				sb.append("constructor ");
				sb.append(constructor.getDeclaringClass().getSimpleName());
				sb.append("(");
				for (int j = 0;  j < parameterAnnotations.length;  j++) {
					if (j > 0) {
						sb.append(", ");
					}
					final String parameterClassName = parameterAnnotations[index].getType().getTypeName();
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
			bindings[i] = requireBinding(parameterAnnotations[i], parameterTypes[i], descriptor);
		}
		return () -> {
			final Object[] values = new Object[bindings.length];
			for (int i = 0;  i < bindings.length;  i++) {
				values[i] = bindings[i].apply(SimpleComponentFactory.this);
			}
			IBindingProvider.assertAccessible(constructor);
			try {
				return constructor.newInstance(values);
			} catch (Exception e) {
				throw DiUtils.show(e);
			}
		};
	}

	/** Creates a new initializer for an object of the given type.
	 * Internally, this will invoke {@link #findFields(Class, Consumer)},
	 * and {@link #findMethods(Class, Consumer)} to create initializers
	 * that set fields, and invoke methods.
	 * @param pType The type, on which the created initializer
	 * operates.
	 * @return The created initializer.
	 */
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

	/** Searches for injectable methods (methods, that are annotated with @Inject).
	 * For any such method, an initializer is being created, that invokes it,
	 * with suitable parameters. The created initializer will then be passed
	 * to the given consumer.
	 * @param pType The type, for which an initializer is being created.
	 * @param pInitializerConsumer The consumer, which receives the created
	 *   method initializers, combining them into an initializer for instances
	 *   of the given type.
	 */
	protected void findMethods(Class<Object> pType, Consumer<Consumer<Object>> pInitializerConsumer) {
		Class<Object> cl = pType;
		do {
			for (final Method method : cl.getDeclaredMethods()) {
				// Ignore abstract methods.
				if (Modifier.isAbstract(method.getModifiers())) {
					continue;
				}
				// Ignore static methods, unless static injection has been requested for the class.
				if (Modifier.isStatic(method.getModifiers())  &&  !staticInjectionClasses.contains(method.getDeclaringClass())) {
					continue;
				}
				for (IBindingProvider bp : bindingProviders) {
					if (bp.isInjectable(method)) {
						final BiConsumer<IComponentFactory,Object> consumer = bp.createInjector(this, method);
						final Consumer<Object> initializer = (o) -> consumer.accept(this, o);
						pInitializerConsumer.accept(initializer);
						break;
					}
				}
			}
			cl = cl.getSuperclass();
		} while (cl != null  &&  cl != Object.class);
	}

	/** Searches for injectable fields (fields, that are annotated with @Inject).
	 * For any such field, an initializer is being created, that sets it
	 *   on an instance, which is being initialized. The created initializer will
	 *   then be passed to the given consumer.
	 * @param pType The type, for which an initializer is being created.
	 * @param pInitializerConsumer The consumer, which receives the created
	 *   field initializers, combining them into an initializer for instances
	 *   of the given type.
	 */
	protected void findFields(Class<Object> pType, Consumer<Consumer<Object>> pInitializerConsumer) {
		Class<Object> cl = pType;
		do {
			for (final Field field : cl.getDeclaredFields()) {
				// Ignore static fields, unless static injection has been requested for the class.
				if (Modifier.isStatic(field.getModifiers())  &&  !staticInjectionClasses.contains(field.getDeclaringClass())) {
					continue;
				}
				for (IBindingProvider bp : bindingProviders) {
					if (bp.isInjectable(field)) {
						final BiConsumer<IComponentFactory,Object> consumer = bp.createInjector(this, field);
						final Consumer<Object> initializer = (o) -> consumer.accept(this, o);
						pInitializerConsumer.accept(initializer);
						break;
					}
				}
			}
			cl = cl.getSuperclass();
		} while (cl != null  &&  cl != Object.class);
	}
	
	/** Performs a lookup for a binding, which is suitable for injecting a value into the given type.
	 * @param pAnnotations The method parameter, or fields set of annotations.
	 * @param pType The type of the method parameter, or field, which needs a binding.
	 * @param pDescriptor Description of the method parameter, or field, for use in
	 *   error messages.
	 * @return The required binding. Never null.
	 * @throws IllegalStateException No suitable binding has been found.
	 */
	protected IBinding<Object> requireBinding(AnnotatedElement pAnnotations, Type pType, Supplier<String> pDescriptor) {
		String name = annotationProvider.getNamedValue(pAnnotations);
		if (name == null) {
			name = "";
		}
		final IBinding<Object> binding = findBinding(pType, name, pAnnotations, true);
		if (binding == null) {
			throw new IllegalStateException("No suitable binding found for " + pDescriptor.get());
		}
		return binding;
	}

	/** Searches for a binding, which can be used to configure a single field, or method
	 * parameter.
	 * @param pType The field type, or parameter type.
	 * @param pName The bindings name, if an @Named annotation is present on the field,
	 * or method parameter.
	 * @param pAnnotations The field, or method parameters annotations.
	 * @param pPermitProvider True, if a provider binding may be created dynamicalla.
	 * @return The requested binding, if any, or null.
	 */
	protected IBinding<Object> findBinding(Type pType, String pName, AnnotatedElement pAnnotations, boolean pPermitProvider) {
		IBinding<Object> binding = null;
		for (Annotation annotation : pAnnotations.getAnnotations()) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			if (annotationClasses.contains(annotationType)) {
				final Key<Object> key1 = Key.of(pType, pName, null, annotation);
				final IBinding<Object> bndng1 = bindings.get(key1);
				if (bndng1 != null) {
					return bndng1;
				}
				if (binding == null) {
					final Key<Object> key2 = Key.of(pType, pName, annotationType, null);
					final IBinding<Object> bndng2 = bindings.get(key2);
					binding = bndng2;
				}
			}
		}
		if (binding == null) {
			final Key<Object> key = Key.of(pType, pName);
			binding = bindings.get(key);
			if (binding == null) {
				// Try to dynamically create a provider binding.
				if (pPermitProvider  &&  pType instanceof ParameterizedType) {
					final ParameterizedType parameterizedType = (ParameterizedType) pType;
					final Type[] genericParameterTypes = parameterizedType.getActualTypeArguments();
					final Type providerType = parameterizedType.getRawType();
					if (genericParameterTypes.length == 1) {
						final Type providedType = genericParameterTypes[0];
						final IBinding<Object> providerLessBinding = findBinding(providedType, pName, pAnnotations, false);
						if (providerLessBinding != null) {
							final ISupplier<Object> supplier = annotationProvider.getProvider(providerType, binding);
							if (supplier != null) {
								return IBinding.of(key, supplier, providerLessBinding.getScope());
							}
						}
					}
				}
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

	@Override
	public Map<Key<Object>, IBinding<Object>> getBindings() {
		// No need to make the map immutable, because it already is.
		return bindings;
	}


}
