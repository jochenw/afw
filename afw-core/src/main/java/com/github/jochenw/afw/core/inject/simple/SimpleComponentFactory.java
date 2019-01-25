package com.github.jochenw.afw.core.inject.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Provider;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.Key;
import com.github.jochenw.afw.core.inject.OnTheFlyBinder;
import com.github.jochenw.afw.core.inject.Scope;
import com.github.jochenw.afw.core.inject.Scopes;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;

public class SimpleComponentFactory implements IComponentFactory {
	public abstract static class Binding<T> implements Supplier<T> {
	}

	protected class MetaData {
		private final Consumer<Object> initializer;
		private final Provider<Object> instantiator;

		public MetaData(Consumer<Object> pInitializer, Provider<Object> pInstantiator) {
			initializer = pInitializer;
			instantiator = pInstantiator;
		}

		public void init(Object pObject) {
			initializer.accept(pObject);
		}
		public Object newInstance() {
			return instantiator.get();
		}
	}

	private Predicate<Class<?>> staticInjectionPredicate;
	private OnTheFlyBinder onTheFlyBinder;
	private final Map<Type,SimpleBindingList> bindings = new HashMap<>();
	private final Map<Type,MetaData> metaDataMap = new HashMap<>();
	private final List<Runnable> finalizers = new ArrayList<>();
	@SuppressWarnings("unused")
	private boolean immutable;
	private static final Predicate<Annotation> SBL_ANNOTATION_FILTER = (a) ->
		!(a instanceof Inject);

	public Predicate<Class<?>> getStaticInjectionPredicate() {
		return staticInjectionPredicate;
	}

	void setStaticInjectionPredicate(Predicate<Class<?>> pPredicate) {
		staticInjectionPredicate = pPredicate;
	}

	public OnTheFlyBinder getOnTheFlyBinder() {
		return onTheFlyBinder;
	}

	void setOnTheFlyBinder(OnTheFlyBinder pBinder) {
		onTheFlyBinder = pBinder;
	}
	
	@Override
	public void init(Object pObject) {
		getMetaData(pObject.getClass()).init(pObject);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O> O getInstance(Key<O> pKey) {
		final SimpleBindingList sbl = findBindingList(pKey.getType(), true);
		if (sbl == null) {
			return null;
		} else {
			final Binding<O> binding;
			synchronized(sbl) {
				binding = (Binding<O>) sbl.find(pKey);
			}
			if (binding == null) {
				return null;
			} else {
				return binding.get();
			}
		}
	}

	@Override
	public <O> O newInstance(Class<? extends O> pImplClass) {
		@SuppressWarnings("unchecked")
		final O o = (O) getMetaData(pImplClass).newInstance();
		return o;
	}

	public void addBinding(Key<?> pKey, Binding<?> pBinding, boolean pReplace) {
		final SimpleBindingList sbl = findBindingList(pKey.getType(), false);
		synchronized(sbl) {
			sbl.add(pKey, pBinding, pReplace);
		}
	}

	protected SimpleBindingList findBindingList(Type pType, boolean pNullableResult) {
		final SimpleBindingList nullableSbl;
		synchronized(bindings) {
			nullableSbl = bindings.get(pType);
		}
		final SimpleBindingList sbl;
		if (nullableSbl == null) {
			if (pNullableResult) {
				return null;
			}
			sbl = new SimpleBindingList();
			synchronized(bindings) {
				bindings.put(pType, sbl);
			}
		} else {
			sbl = nullableSbl;
		}
		return sbl;
	}
	
	protected void addFinalizer(Runnable pRunnable) {
		finalizers.add(pRunnable);
	}

	protected class SingletonBinding<T> extends Binding<T> {
		private final Provider<T> provider;
		private boolean created, initialized, initializing;
		private T instance;

		public SingletonBinding(Provider<T> pProvider) {
			provider = pProvider;
		}

		@Override
		public T get() {
			final String t = this.toString().replace("com.github.jochenw.afw.core.inject.simple.SimpleComponentFactory$", "");
			System.out.println("get: -> " + t);
			if (!initialized) {
				synchronized(this) {
					System.out.println("get: " + created + ", " + initialized + ", " + initializing);
					if (!created) {
						instance = provider.get();
						created = true;
					}
					if (!initialized) {
						instance = provider.get();
						System.out.println("get: Created instance");
						if (!initializing) {
							initializing = true;
							init(instance);
						}
						initialized = true;
					}
				}
			}
			System.out.println("get: <- " + t + ", " + instance);
			return instance;
		}
	}
	protected Binding<?> newBinding(Provider<?> pProvider, Scope pScope) {
		@SuppressWarnings("unchecked")
		final Provider<Object> prov = (Provider<Object>) pProvider;
		final Binding<Object> binding;
		if (pScope == Scopes.EAGER_SINGLETON) {
			binding = new SingletonBinding<Object>(prov);
			addFinalizer(() -> binding.get());
		} else if (pScope == Scopes.SINGLETON) {
			binding = new SingletonBinding<Object>(prov);
		} else if (pScope == Scopes.NO_SCOPE  ||  pScope == null) {
			binding = new Binding<Object>() {
				@Override
				public Object get() {
					final Object instance = prov.get();
					init(instance);
					return instance;
				}
			};
		} else {
			throw new IllegalStateException("Invalid scope: " + pScope);
		}
		final Binding<?> b = (Binding<?>) binding;
		return b;
	}

	protected void makeImmutable() {
		finalizers.forEach((r) -> r.run());
		finalizers.clear();
		immutable = true;
	}

	protected MetaData getMetaData(Type pType) {
		synchronized(metaDataMap) {
			final MetaData md = metaDataMap.get(pType);
			if (md != null) {
				return md;
			}
		}
		final MetaData md = newMetaData(pType);
		synchronized(metaDataMap) {
			metaDataMap.put(pType, md);
		}
		return md;
	}

	protected MetaData newMetaData(Type pType) {
		final Class<?> type = (Class<?>) pType;
		final Consumer<Object> initializer = newInitializer(type);
		final Provider<Object> instantiator = newInstantiator(type);
		return new MetaData(initializer, instantiator);
	}

	private Consumer<Object> newInitializer(Class<?> pType) {
		final List<Consumer<Object>> initializers = new ArrayList<>();
		final Consumer<Consumer<Object>> initializerSink = (co) -> initializers.add(co);
		final List<Class<?>> classes = new ArrayList<>();
		Class<?> cl = pType;
		while (cl != null &&  !Object.class.equals(cl)) {
			classes.add(0, cl);
		}
		for (Class<?> c : classes) {
			findFieldInitializers(pType, c, initializerSink);
			findMethodInitializers(pType, c, initializerSink);
		};
		return (o) -> initializers.forEach((i) -> i.accept(o));
	}

	private Provider<Object> newInstantiator(Class<?> pType) {
		for (Constructor<?> c : pType.getDeclaredConstructors()) {
			if (c.isAnnotationPresent(Inject.class)) {
				final Binding<?>[] bindings = findBindings(c.getParameters(), c.getGenericParameterTypes(), c.getParameterAnnotations(),
						"Constructor of class " + c.getName());
				return () -> {
					try {
						if (!c.isAccessible()) {
							c.setAccessible(true);
						}
						System.out.println("Constructor: " + c.getName());
						return c.newInstance(getParameters(bindings));
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				};
			}
		}
		return () -> {
			try {
				return pType.newInstance();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}

	protected boolean isStaticInjectionRequested(Class<?> pType) {
		return staticInjectionPredicate != null  &&  staticInjectionPredicate.test(pType);
	}
	
	protected void findFieldInitializers(Class<?> pType, Class<?> pSuperClass, Consumer<Consumer<Object>> pInitializerSink) {
		final BiConsumer<Field, Provider<Object>> processor = (f,p) -> {
			final Consumer<Object> consumer = (o) -> {
				try {
					if (!f.isAccessible()) {
						f.setAccessible(true);
					}
					f.set(o, p.get());
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};
			pInitializerSink.accept(consumer);
		};
		for (Field f : pSuperClass.getDeclaredFields()) {
			if (f.isAnnotationPresent(Inject.class)) {
				if (!Modifier.isStatic(f.getModifiers())  ||  isStaticInjectionRequested(pSuperClass)) {
					Binding<?> binding = findBinding(f, f.getGenericType(), f.getAnnotations());
					if (binding == null) {
						throw new IllegalStateException("No binding registered for field " + f.getName() + " in class " + pSuperClass.getName());
					}
					final Binding<?> b = binding;
					processor.accept(f,() -> b.get());
				}
			}
			if (onTheFlyBinder != null) {
				final Provider<Object> prov = onTheFlyBinder.getProvider(this, f);
				if (prov != null) {
					processor.accept(f, prov);
				}
			}
		}
	}

	protected void findMethodInitializers(Class<?> pType, Class<?> pSuperClass, Consumer<Consumer<Object>> pInitializerSink) {
		for (Method m : pSuperClass.getDeclaredMethods()) {
			if (m.isAnnotationPresent(Inject.class)) {
				if (Modifier.isStatic(m.getModifiers())  &&  !isStaticInjectionRequested(pSuperClass)) {
					continue;
				}
				if (Modifier.isAbstract(m.getModifiers())) {
					continue;
				}
				if (isInjectedMethodOverwrittenByInjectedMethod(pType, m)) {
					continue;
				}
				if (isPackagePrivateMethodOverwritten(pType, m)) {
					continue;
				}
				if (isPublicMethodOverwrittenByNotAnnotatedMethod(pType, m)) {
					continue;
				}
				final Method mth = Objects.notNull(findOverridingPublicMethod(pType, m), m);
				final Binding<?>[] bindings = findBindings(mth.getParameters(), mth.getGenericParameterTypes(), mth.getParameterAnnotations(),
						"Method " + mth.getName() + " in class " + pSuperClass.getName());
				final Consumer<Object> consumer = (o) -> {
					if (!m.isAccessible()) {
						m.setAccessible(true);
					}
					try {
						m.invoke(o, getParameters(bindings));
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				};
				pInitializerSink.accept(consumer);
			}
		}
	}

	protected boolean isPublicMethodOverwrittenByNotAnnotatedMethod(Class<?> pType, Method pMethod) {
		if (!Modifier.isPublic(pMethod.getModifiers())) {
			return false;
		}
		final String name = pMethod.getName();
		final Class<?>[] paramTypes = pMethod.getParameterTypes();
		Class<?> cl = pType;
		while (cl != null  &&  cl != Object.class  &&  !pMethod.getDeclaringClass().equals(cl)) {
			Method m2;
			try {
				m2 = cl.getDeclaredMethod(name, paramTypes);
			} catch (NoSuchMethodException e) {
				m2 = null;
			}
			if (m2 != null) {
				return !m2.isAnnotationPresent(Inject.class);
			}
			cl = cl.getSuperclass();
		}
		return false;
	}

	protected Method findOverridingPublicMethod(Class<?> pType, Method pMethod) {
		final int mod = pMethod.getModifiers();
		if (!Modifier.isPublic(mod)) {
			return null;
		}
		final String name = pMethod.getName();
		final Class<?>[] paramTypes = pMethod.getParameterTypes();
		Class<?> cl = pType;
		while (cl != null  &&  cl != Object.class  &&  !pMethod.getDeclaringClass().equals(cl)) {
			Method m2;
			try {
				m2 = cl.getDeclaredMethod(name, paramTypes);
			} catch (NoSuchMethodException e) {
				m2 = null;
			}
			if (m2 != null) {
				return m2;
			}
			cl = cl.getSuperclass();
		}
		return null;
	}

	protected boolean isPackagePrivateMethodOverwritten(Class<?> pType, Method pMethod) {
		final int mod = pMethod.getModifiers();
		if (Modifier.isPrivate(mod)  ||  Modifier.isProtected(mod)  ||  Modifier.isPublic(mod)) {
			return false;
		}
		final String name = pMethod.getName();
		final Class<?>[] paramTypes = pMethod.getParameterTypes();
		Class<?> cl = pType;
		while (cl != null  &&  cl != Object.class  &&  !pMethod.getDeclaringClass().equals(cl)) {
			Method m2;
			try {
				m2 = cl.getDeclaredMethod(name, paramTypes);
			} catch (NoSuchMethodException e) {
				m2 = null;
			}
			if (m2 != null) {
				return true;
			}
			cl = cl.getSuperclass();
		}
		return false;
	}

	protected boolean isInjectedMethodOverwrittenByInjectedMethod(Class<?> pType, Method pMethod) {
		final String name = pMethod.getName();
		final Class<?>[] paramTypes = pMethod.getParameterTypes();
		Class<?> cl = pType;
		while (cl != null  &&  cl != Object.class  &&  !pMethod.getDeclaringClass().equals(cl)) {
			Method m2;
			try {
				m2 = cl.getDeclaredMethod(name, paramTypes);
			} catch (NoSuchMethodException e) {
				m2 = null;
			}
			if (m2 != null &&  m2.isAnnotationPresent(Inject.class)) {
				return true;
			}
			cl = cl.getSuperclass();
		}
		return false;
	}
	protected Object[] getParameters(Binding<?>[] pBindings) {
		final Object[] array = new Object[pBindings.length];
		for (int i = 0;  i < array.length;  i++) {
			array[i] = pBindings[i].get();
		}
		return array;
	}

	protected Binding<?>[] findBindings(AnnotatedElement[] pAnnotatables, Type[] pTypes, Annotation[][] pAnnotations, String pDescription) {
		final Binding<?>[] bindings = (Binding<?>[]) Array.newInstance(Binding.class, pTypes.length);
		for (int j = 0;  j < bindings.length;  j++) {
			final int i = j;
			final Binding<?> b = findBinding(pAnnotatables[i], pTypes[i], pAnnotations[i]);
			if (b == null) {
				throw new IllegalStateException("No binding available for parameter " + i + " of " + pDescription);
			}
			bindings[i] = b;
		}
		return bindings;
	}
	
	protected Binding<?> findBinding(AnnotatedElement pAnnotatable, Type pType, Annotation[] pAnnotations) {
		final SimpleBindingList sbl = findBindingList(pType, true);
		Binding<?> binding;
		if (sbl == null) {
			binding = null;
		} else {
			synchronized(sbl) {
				binding = sbl.find(pAnnotations, SBL_ANNOTATION_FILTER);
			}
		}
		if (binding == null) {
			if (onTheFlyBinder != null  &&  pAnnotatable instanceof Field) {
				final Provider<?> prov = onTheFlyBinder.getProvider(this, (Field) pAnnotatable);
				if (prov != null) {
					binding = newBinding(prov, Scopes.NO_SCOPE);
				}
			}
		}
		if (binding == null  &&  pType instanceof ParameterizedType) {
			final ParameterizedType ptype = (ParameterizedType) pType;
			if (Provider.class.equals(ptype.getRawType())) {
				final Type[] argTypes = ptype.getActualTypeArguments();
				if (argTypes != null  &&  argTypes.length == 1) {
					final Binding<?> b = findBinding(pAnnotatable, argTypes[0], pAnnotations);
					if (b != null) {
						final Provider<Object> prov = () -> b.get();
						final Provider<Provider<Object>> prov2 = () -> prov;
						binding = newBinding(prov2, Scopes.NO_SCOPE);
					}
				}
			}
		}
		return binding;
	}
}
