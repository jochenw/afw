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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.BindingBuilder;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.jochenw.afw.di.util.Reflection;


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
	public <O> O getInstance(Key<O> pKey) {
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
	public void configure(IOnTheFlyBinder pOnTheFlyBinder,
			              @Nonnull List<BindingBuilder<Object>> pBuilders,
			              @Nonnull Set<Class<?>> pStaticInjectionClasses) {
		staticInjectionPredicate = (cl) -> pStaticInjectionClasses.contains(cl);
		bindings = new BindingRegistry(this, pBuilders, staticInjectionPredicate);
		try {
			bindings.init(this);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected MetaData newMetaData(Class<Object> pType) {
		final Function<SimpleComponentFactory,Object> instantiator = newInstantiator(pType);
		final BiConsumer<SimpleComponentFactory,Object> injector = newInjector(pType);
		return new MetaData(instantiator, injector);
	}

	public interface TriConsumer<O1,O2,O3> {
		void accept(O1 pO1, O2 pO2, O3 pO3);
	}

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
				if (method.isAnnotationPresent(Inject.class)) {
					if (!Modifier.isStatic(method.getModifiers())  ||  staticInjection) {
						newMethodInjector(list::add, method);
					}
				}
				if (onTheFlyBinder != null  &&  onTheFlyBinder.isInjectable(clazz)) {
					final BiConsumer<IComponentFactory,Object> injector = onTheFlyBinder.getInjector(clazz);
					list.add((scf,o) -> injector.accept(scf, o));
				}
			}
			clazz = clazz.getSuperclass();
		}
		return (scf,o) -> list.forEach((consumer) -> consumer.accept(scf, o));
	}

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

	protected void newFieldInjector(final Consumer<BiConsumer<SimpleComponentFactory, Object>> pSink, Class<Object> pClazz,
			boolean pStaticInjection, final Field pField) {
		if (pField.isAnnotationPresent(Inject.class)) {
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

	protected Binding findBinding(Type pType, Annotation[] pAnnotations) {
		Binding binding = bindings.find(pType, pAnnotations);
		if (binding == null) {
			if (onTheFlyBinder != null  &&  onTheFlyBinder.isInstantiable(pType, pAnnotations)) {
				final Function<IComponentFactory,Object> instantiator = onTheFlyBinder.getInstance(pType, pAnnotations);
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
				if (pt.getRawType() == Provider.class  ||  pt.getRawType() == Supplier.class) {
					final Type[] typeArguments = pt.getActualTypeArguments();
					if (typeArguments.length == 1) {
						final Type typeArgument = typeArguments[0];
						final Binding typeArgumentBinding = findBinding(typeArgument, pAnnotations);
						if (typeArgumentBinding == null) {
							return null;
						} else {
							return new ProviderBinding(typeArgumentBinding);
						}
					}
				}
			}
		}
		return binding;
	}

	protected Function<SimpleComponentFactory,Object> newInstantiator(Class<Object> pType) {
		Function<SimpleComponentFactory,Object> result = null;
		for (Constructor<?> cons : pType.getDeclaredConstructors()) {
			@SuppressWarnings("unchecked")
			final Constructor<Object> constructor = (Constructor<Object>) cons;
			if (constructor.isAnnotationPresent(Inject.class)) {
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

	protected Binding asBinding(BindingBuilder<Object> pBindingBuilder) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public boolean hasKey(Key<?> pKey) {
		@SuppressWarnings("unchecked")
		final Key<Object> key = (Key<Object>) pKey;
		return bindings.find(key) != null;
	}
}
