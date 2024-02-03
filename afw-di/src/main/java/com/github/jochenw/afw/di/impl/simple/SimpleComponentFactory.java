package com.github.jochenw.afw.di.impl.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.di.api.Annotations;
import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.BindingBuilder;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.jochenw.afw.di.util.Reflection;


/** Default implementation of an {@link IComponentFactory}:
 * A simple, and fast, standalone implementation, without
 * any need to bring Guava, or Spring, into the dependency hell.
 */
public class SimpleComponentFactory extends AbstractComponentFactory {
	private final ConcurrentMap<Class<Object>, MetaData> metaDataByClass = new ConcurrentHashMap<>();
	private Predicate<Class<?>> staticInjectionPredicate;
	private BindingRegistry bindings;
	private IOnTheFlyBinder onTheFlyBinder;

	@Override
	public void init(Object pObject) {
		final Object object = Objects.requireNonNull(pObject, "Object");
		@SuppressWarnings("unchecked")
		final Class<Object> clazz = (Class<Object>) object.getClass();
		final MetaData metaData = metaDataByClass.computeIfAbsent(clazz, (cl) -> {
			return newMetaData(cl);
		});
		metaData.getInitializer().accept(this, object);
	}

	@Override
	public <O> @Nullable O getInstance(Key<O> pKey) {
		@SuppressWarnings("unchecked")
		final Key<Object> key = (Key<Object>) Objects.requireNonNull(pKey, "Key");
		final Binding binding = bindings.find(key);
		if (binding == null) {
			return null;
		} else {
			@SuppressWarnings("unchecked")
			final O o = (O) binding.apply(this);
			if (o == null) {
				throw new NullPointerException("Binding returned null for key: " + pKey);
			}
			return o;
		}
		
	}

	@Override
	public <O> O newInstance(Class<? extends O> pImplClass) {
		@SuppressWarnings("unchecked")
		final Class<Object> implClass = (Class<Object>) pImplClass;
		final MetaData metaData = metaDataByClass.computeIfAbsent(implClass, (cl) -> {
			return newMetaData(cl);
		});
		final Function<SimpleComponentFactory, Object> instantiator = metaData.getInstantiator();
		if (instantiator == null) {
			throw new IllegalStateException("No public default constructor found for class "
					+ implClass.getName() + ", and no constructor annotated with @Inject.");
		}
		@SuppressWarnings("unchecked")
		final O o = (O) instantiator.apply(this);
		metaData.getInitializer().accept(this, o);
		return o;
	}

	@Override
	public <O> O newInstance(Constructor<? extends O> pConstructor) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void configure(@NonNull IAnnotationProvider pAnnotationProvider,
			              @NonNull IOnTheFlyBinder pOnTheFlyBinder,
			              @NonNull List<BindingBuilder<Object>> pBuilders,
			              @NonNull Set<Class<?>> pStaticInjectionClasses) {
		setAnnotationProvider(pAnnotationProvider);
		onTheFlyBinder = pOnTheFlyBinder;
		staticInjectionPredicate = (cl) -> pStaticInjectionClasses.contains(cl);
		bindings = new BindingRegistry(this, pBuilders);
		try {
			bindings.init(this);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Creates a metadata instance for the given type.
	 * @param pType The type, which is being inspected.
	 * @return The created metadata instance.
	 */
	protected MetaData newMetaData(Class<Object> pType) {
		final Function<SimpleComponentFactory,Object> instantiator = newInstantiator(pType);
		final BiConsumer<SimpleComponentFactory,Object> injector = newInjector(pType);
		return new MetaData(instantiator, injector);
	}

	/** Three arguments version of a {@link BiConsumer}.
     * @param <O1> Type of the first argument.
	 * @param <O2> Type of the second argument.
	 * @param <O3> Type of the third argument.
	 */
	public interface TriConsumer<O1,O2,O3> {
		/** Calls the consumer to provide an action, based on the three arugments.
		 * @param pO1 The first argument.
		 * @param pO2 The second argument.
		 * @param pO3 The third argument.
		 */
		void accept(O1 pO1, O2 pO2, O3 pO3);
	}

	/** Returns an injector for the given type.
	 * The injector has the ability to configure an instance
	 * of the given type by injecting values.
	 * @param pType The type, for which an injector must be created.
	 * @return The created injector.
	 */
	protected BiConsumer<SimpleComponentFactory,Object> newInjector(Class<Object> pType) {
		final List<BiConsumer<SimpleComponentFactory,Object>> list = new ArrayList<>();
		Class<Object> clazz = pType;
		boolean staticInjection = staticInjectionPredicate.test(pType);
		while (clazz != null  &&  clazz != Object.class) {
			staticInjection =  staticInjection  ||  staticInjectionPredicate.test(clazz);
			for (Field fld : clazz.getDeclaredFields()) {
				final Field field = fld;
				newFieldInjector(list::add, clazz, staticInjection, field);
			}
			for (Method method : clazz.getDeclaredMethods()) {
				if (Annotations.isInjectPresent(method)) {
					if (!Modifier.isStatic(method.getModifiers())  ||  staticInjection) {
						newMethodInjector(list::add, method);
					}
				}
			}
			if (onTheFlyBinder != null  &&  onTheFlyBinder.isInjectable(clazz)) {
				final BiConsumer<IComponentFactory,Object> injector = onTheFlyBinder.getInjector(clazz);
				list.add((scf,o) -> injector.accept(scf, o));
			}
			clazz = clazz.getSuperclass();
		}
		return (scf,o) -> list.forEach((consumer) -> consumer.accept(scf, o));
	}

	/** Creates a method injector for the given method.
	 * The injector has the ability to configure an instance
	 * of the methods declaring type by invoking the given method,
	 * passing injectable values as the parameters.
	 * @param pSink A consumer for the created injector.
	 * @param pMethod The method, for which an injector
	 *   must be created.
	 */
	protected void newMethodInjector(final Consumer<BiConsumer<SimpleComponentFactory, Object>> pSink, Method pMethod) {
		if (Modifier.isAbstract(pMethod.getModifiers())) {
			throw new IllegalStateException("The method " + asString(pMethod)
				+ " is abstract. Therefore, it must not be annotated with @Inject.");
		}
		final Type[] parameterTypes = pMethod.getGenericParameterTypes();
		final Annotation[][] parameterAnnotations = pMethod.getParameterAnnotations();
		final Binding[] parameterBindings = new Binding[parameterTypes.length];
		for (int i = 0;  i < parameterTypes.length;  i++) {
			final Binding b = findBinding(parameterTypes[i], parameterAnnotations[i]);
			if (b == null) {
				throw new IllegalStateException("No binding registered for parameter " + i + " in method "
						+ asString(pMethod));
			}
			parameterBindings[i] = b;
		}
		BiConsumer<Object,Object[]> invoker = Reflection.newInjector(pMethod);
		pSink.accept((scf,o) -> {
			final Object[] parameters = new Object[parameterBindings.length];
			for (int i = 0;  i < parameterBindings.length;  i++) {
				parameters[i] = parameterBindings[i].apply(scf);
			}
			invoker.accept(o, parameters);
		});
	}

	/** Creates an injector for the given field.
	 * The injector has the ability to configure an instance
	 * of the fields declaring type by injecting a value
	 * into the field, passing an injectable value.
	 * @param pSink A consumer for the created injector.
	 * @param pClazz The class, to which the field belongs.
	 *   (The field's declaring class, or a subclass.)
	 * @param pStaticInjection True, if static injection is
	 *   being supported.
	 * @param pField The field, for which an injector
	 *   must be created.
	 */
	protected void newFieldInjector(final Consumer<BiConsumer<SimpleComponentFactory, Object>> pSink, Class<Object> pClazz,
			boolean pStaticInjection, final Field pField) {
		if (Annotations.isInjectPresent(pField)) {
			if (Modifier.isStatic(pField.getModifiers())  &&  !pStaticInjection) {
				throw new IllegalStateException("The field " + pField.getName()
					+ " is static. No static injection has been requested for class "
					+ pField.getDeclaringClass().getName() + ". Therefore, it must not be annotated with @Inject.");
			}
			if (Modifier.isFinal(pField.getModifiers())) {
				throw new IllegalStateException("The field " + pField.getName() + " in class " + pClazz.getName()
					+ " is final. Therefore, it must not be annotated with @Inject.");
			}
			final Binding binding = findBinding(pField.getGenericType(), pField.getAnnotations());
			if (binding == null) {
				throw new IllegalStateException("No binding registered for field "
						+ pField.getName() + " in class " + pClazz.getName());
			}
			final BiConsumer<Object,Object> injector
			= Reflection.newInjector(pField);
			pSink.accept((scf,o) -> injector.accept(o, binding.apply(scf)));
		}
	}

	/** Creates a textual description of the given method,
	 * for use in error messages.
	 * @param pMethod The method, that is being described.
	 * @return The created description.
	 */
	protected String asString(Method pMethod) {
		final Type[] parameterTypes = pMethod.getGenericParameterTypes();
		final StringBuilder sb = new StringBuilder();
		sb.append(pMethod.getName());
		sb.append("(");
		for (int j = 0;  j < parameterTypes.length;  j++) {
			if (j > 0) {
				sb.append(",");
			}
			sb.append(parameterTypes[j]);
		}
		sb.append(") in class " + pMethod.getDeclaringClass().getName());
		return sb.toString();
	}

	/** Called to find a binding with the given type, that matches the
	 * given set of annotations.
	 * @param pType The requested bindings type.
	 * @param pAnnotations The set of annotations, that are being matched
	 *   by the requested binding.
	 * @return The requested binding, if any, or null.
	 */
	protected Binding findBinding(Type pType, Annotation[] pAnnotations) {
		Binding binding = bindings.find(pType, pAnnotations);
		if (binding == null) {
			if (onTheFlyBinder != null  &&  onTheFlyBinder.isInstantiable(pType, pAnnotations)) {
				final Function<IComponentFactory,Object> instantiator = onTheFlyBinder.getInstantiator(pType, pAnnotations);
				return new Binding() {
					@Override
					public Object apply(SimpleComponentFactory pScf) {
						return instantiator.apply(pScf);
					}
				};
			}
		}
		if (binding == null) {
			if (pType instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) pType;
				final Type rawType = pt.getRawType();
				final BiFunction<Class<?>,Function<Binding,Binding>,Binding> providerCreator = (cl,func) -> {
					if (rawType == cl) {
						final Type[] typeArguments = pt.getActualTypeArguments();
						if (typeArguments.length == 1) {
							final Type typeArgument = typeArguments[0];
							final Binding typeArgumentBinding = findBinding(typeArgument, pAnnotations);
							if (typeArgumentBinding != null) {
								return func.apply(typeArgumentBinding);
							}
						}
					}
					return null;
				};
				for (IAnnotationProvider ap : Annotations.getProviders()) {
					final Binding bnd = providerCreator.apply(ap.getProviderClass(), (b) -> ap.getProvider(b));
					if (bnd != null) {
						return bnd;
					}
				}
				return providerCreator.apply(Supplier.class, (b) -> {
					return new Binding() {
						@Override
						public Object apply(SimpleComponentFactory pCf) {
							return new Supplier<Object>() {
								@Override
								public Object get() {
									return b.apply(pCf);
								}
							};
						}
					};
				});
			}
		}
		return binding;
	}

	/** Creates a supplier for an object of the given type:
	 * <ol>
	 *   <li>If the given type has a constructor, that is annotated
	 *   with {@code @Inject}, then that constructor will be used
	 *     by the created supplier. If so, the given component
	 *     factory will be used as a supplier for the constructor
	 *     parameters.
	 *   </li>
	 *   <li>Otherwise, of the given type has a default constructor
	 *     (a public no-arguments constructor), then the created
	 *     supplier will use that constructor, and the given
	 *     component factory will be ignored, because no arguments
	 *     are required.</li>
	 *   <li>Otherwise, the value null (no function) will be returned.
	 * </ol>
	 * @param pType The type, for which a supplier must be created.
	 * @return The created function, if any, or null.
	 */
	protected Function<SimpleComponentFactory,Object> newInstantiator(Class<Object> pType) {
		Function<SimpleComponentFactory,Object> result = null;
		for (Constructor<?> cons : pType.getDeclaredConstructors()) {
			@SuppressWarnings("unchecked")
			final Constructor<Object> constructor = (Constructor<Object>) cons;
			if (Annotations.isInjectPresent(constructor)) {
				if (result != null) {
					throw new IllegalStateException("Multiple constructors annotated with @Inject in class " + pType.getName());
				}
				final Type[] parameterTypes = constructor.getGenericParameterTypes();
				final Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
				final Binding[] parameterBindings = new Binding[parameterTypes.length];
				for (int i = 0;  i < parameterTypes.length;  i++) {
					Binding binding = findBinding(parameterTypes[i], parameterAnnotations[i]);
					if (binding == null) {
						throw new IllegalStateException("No binding registered for parameter " + i + " of constructor " + constructor);
					}
					parameterBindings[i] = binding;
				}
				result = (scf) -> {
					final Supplier<Object> supplier = Reflection.newInstantiator(constructor, (i) -> parameterBindings[i].apply(scf));
					return supplier.get();
				};
			}
		}
		if (result == null) {
			final Constructor<Object> constr;
			try {
				constr = pType.getDeclaredConstructor();
			} catch (Throwable t) {
				return null;
			}
			result = (scf) -> {
				final Supplier<Object> supplier = Reflection.newInstantiator(constr, null);
				return supplier.get();
			};
		}
		return result;
	}

	@Override
	public boolean hasKey(Key<?> pKey) {
		@SuppressWarnings("unchecked")
		final Key<Object> key = (Key<Object>) pKey;
		return bindings.find(key) != null;
	}
}
