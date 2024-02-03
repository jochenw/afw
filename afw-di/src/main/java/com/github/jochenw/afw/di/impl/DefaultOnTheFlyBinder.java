package com.github.jochenw.afw.di.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.api.ILifecycleController;
import com.github.jochenw.afw.di.api.ILifecycleController.TerminableListener;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.api.PropInject;
import com.github.jochenw.afw.di.util.Reflection;


/** Default implementation of {@link IOnTheFlyBinder}.
 * Supports {@code PostConstruct}, and {@code PreDestroy}.
 */
public class DefaultOnTheFlyBinder extends AbstractOnTheFlyBinder {
	@Override
	protected boolean isInjectable(Field pField) {
		final Annotation[] annotations = pField.getAnnotations();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (isAnnotationInjectable(pField, annotation, annotations)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean isInjectable(Method pMethod) {
		final Class<?> cl = pMethod.getDeclaringClass();
		final Class<?> returnType = pMethod.getReturnType();
		final Class<?>[] parameterTypes = pMethod.getParameterTypes();
		if ((IComponentFactoryAware.class.isAssignableFrom(cl)  &&
				"init".equals(pMethod.getName())  &&
				(returnType == null  ||  returnType == Void.TYPE)  &&
				parameterTypes.length == 0  &&  IComponentFactory.class == parameterTypes[0])) {
			return true;
		} else {
			Annotation[] annotations = pMethod.getAnnotations();
			for (Annotation annotation : annotations) {
				if (isAnnotationInjectable(pMethod, annotation, annotations)) {
					return true;
				}
			}
			return false;
		}
	}

	/** Returns, whether the given annotation indicates, that a value must
	 * be injected into the given field.
	 * @param pField The field, which is being tested.
	 * @param pAnnotation An annotation of the given field.
	 * @param pAnnotations The array of all the fields annotations.
	 *   The annotation {@code pAnnotation} is an element in this
	 *   array.
	 * @return True, if a value must be injected into the given field.
	 *   This is the case, if the given annotation is an instance of
	 *   {@link LogInject}, or {@link PropInject}. Otherwise, false is
	 *   returned.
	 */
	protected boolean isAnnotationInjectable(Field pField, Annotation pAnnotation, Annotation[] pAnnotations) {
		return pAnnotation instanceof LogInject  ||  pAnnotation instanceof PropInject;
	}

	/** Returns, whether the given annotation indicates, that a value must
	 * be injected by invoking the given method.
	 * @param pMethod The method, which is being annotated.
	 * @param pAnnotation An annotation of the given method.
	 * @param pAnnotations The array of all the methods annotations.
	 *   The annotation {@code pAnnotation} is an element in this
	 *   array.
	 * @return True, if a value must be injected by invoking the
	 *   given method, This is the case, if the given annotation
	 *   is an instance of {@link LogInject}, or {@link PropInject}.
	 *   Otherwise, false is returned.
	 */
	protected boolean isAnnotationInjectable(Method pMethod, Annotation pAnnotation, Annotation[] pAnnotations) {
		return pAnnotation instanceof PostConstruct  ||  pAnnotation instanceof PreDestroy
				||  pAnnotation instanceof jakarta.annotation.PostConstruct
				||  pAnnotation instanceof jakarta.annotation.PreDestroy
				||  pAnnotation instanceof LogInject  ||  pAnnotation instanceof PropInject;
	}

	/** Creates an instance of the given logger type, with the given
	 * logger id.
	 * @param pFactory The component factory, which is being used to
	 *   create the instance.
	 * @param pType Type of the created logger.
	 * @param pId Id of the created logger.
	 * @return The created logger instance.
	 */
	protected Object getLogger(IComponentFactory pFactory, Class<?> pType, String pId) {
		return null;
	}

	/** Creates an instance of the given property type, with the given
	 * property key, and the given default value.
	 * @param pFactory The component factory, which is being used to
	 *   create the instance.
	 * @param pType Type of the created property.
	 * @param pId Key of the created property.
	 * @param pDefaultValue The created properties default value.
	 * @param pNullable True, if the requested property value may be
	 *   null, after applying a potential default value. 
	 * @return The created property instance.
	 */
	protected Object getProperty(@NonNull IComponentFactory pFactory, @NonNull Class<?> pType, @NonNull String pId, @NonNull String pDefaultValue,
			                     boolean pNullable) {
		return null;
	}


	/** Creates an injectable value for the given field, or method.
	 * This method delegates to {@link #getInjectableLogger(IComponentFactory, Member, LogInject)},
	 * or {@link #getInjectableProperty(IComponentFactory, Member, PropInject)},
	 * depending on the type of the given {@code pAnnotation}.
	 * @param pComponentFactory The component factory, which is being used to
	 *   create the instance.
	 * @param pMember The fieĺd, or method, for which an injectable value
	 *   is being created.
	 * @param pAnnotation The annotation, which requests, that a value
	 *   must be injected.
	 * @param pAllAnnotations The members array of annotations. The
	 *   annotation {@code pAnnotation} is an element in the array.
	 * @return The created injectable value.
	 */
	protected Object getInjectableValue(@NonNull IComponentFactory pComponentFactory,
										Member pMember,
										Annotation pAnnotation,
			                            Annotation[] pAllAnnotations) {
		if (pAnnotation instanceof LogInject) {
			final LogInject logInject = (LogInject) pAnnotation;
			return getInjectableLogger(pComponentFactory, pMember, logInject);
		} else if (pAnnotation instanceof PropInject) {
			final PropInject propInject = (PropInject) pAnnotation;
			return getInjectableProperty(pComponentFactory, pMember, propInject);
		} else {
			throw new IllegalStateException("Invalid annotation type: " + pAnnotation.getClass().getName());
		}
	}

	/** Creates an injectable property value for the given field, or method.
	 * @param pComponentFactory The component factory, which is being used to
	 *   create the instance.
	 * @param pMember The fieĺd, or method, for which an injectable value
	 *   is being created.
	 * @param pPropInject The annotation, which requests, that a value
	 *   must be injected.
	 * @return The created injectable value.
	 */
	protected Object getInjectableProperty(@NonNull IComponentFactory pComponentFactory, Member pMember, final PropInject pPropInject) {
		final @NonNull String id;
		if (pPropInject.id().length() == 0) {
			id = pMember.getDeclaringClass().getCanonicalName() + "." + pMember.getName();
		} else {
			@SuppressWarnings("null")
			final @NonNull String injectId = pPropInject.id();
			id = injectId;
		}
		final @NonNull Class<?> type = getInjectableValueType(pMember);
		@SuppressWarnings("null")
		final @NonNull String defaultValue = pPropInject.defaultValue();
		final Object property = getProperty(pComponentFactory, type, id, defaultValue, pPropInject.nullable());
		if (property == null) {
			if (!pPropInject.nullable()) {
				throw new IllegalStateException("No property value is available with id " + id
						+ " to inject a value into " + pMember);
			}
		}
		return property;
	}

	/** Creates an injectable logger value for the given field, or method.
	 * depending on the type of the given {@code pAnnotation}.
	 * @param pComponentFactory The component factory, which is being used to
	 *   create the instance.
	 * @param pMember The fieĺd, or method, for which an injectable value
	 *   is being created.
	 * @param pLogInject The annotation, which requests, that a value
	 *   must be injected.
	 * @return The created injectable value.
	 */
	protected Object getInjectableLogger(IComponentFactory pComponentFactory, Member pMember, final LogInject pLogInject) {
		final String id;
		if (pLogInject.id().length() == 0) {
			id = pMember.getDeclaringClass().getCanonicalName();
		} else {
			id = pLogInject.id();
		}
		final Class<?> type = getInjectableValueType(pMember);
		final Object logger = getLogger(pComponentFactory, type, id);
		if (logger == null) {
			throw new IllegalStateException("No logger found for " + pMember);
		}
		return logger;
	}

	/** Returns the type of the value, that must be injected
	 * for the given fieĺd, or method.
	 * @param pMember The field, or method, which requires an
	 *   injectable value.
	 * @return The requested type. If the member is a field,
	 *   then the fields type. For a method, returns the
	 *   type of the first parameter.
	 */
	protected @NonNull Class<?> getInjectableValueType(Member pMember) {
		final @NonNull Class<?> type;
		if (pMember instanceof Field) {
			@SuppressWarnings("null")
			final @NonNull Class<?> tp = ((Field) pMember).getType();
			type = tp;
		} else if (pMember instanceof Method) {
			final Method mth = (Method) pMember;
			if (mth.getReturnType() == Void.TYPE  || mth.getReturnType() == Void.class
					||  mth.getReturnType() == null) {
				final Class<?>[] parameterTypes = mth.getParameterTypes();
				if (parameterTypes.length != 1) {
					@SuppressWarnings("null")
					final @NonNull Class<?> tp = parameterTypes[0];
					type = tp;
				} else {
					throw new IllegalStateException("Method " + mth + " is annotated with @LogInject, "
							+ " but is not a setter, because it takes " + parameterTypes.length	
							+ " parameters, instead of one.");
				}
			} else {
				throw new IllegalStateException("Method " + mth + " is annotated with @LogInject, "
						+ " but is not a setter, because it's return type isn't void.");
			}
		} else {
			throw new IllegalArgumentException("Invalid member type: " + pMember.getName());
		}
		return type;
	}

	@Override
	protected BiConsumer<@NonNull IComponentFactory, Object> getInjector(Field pField) {
		Annotation[] annotations = pField.getAnnotations();
		for (Annotation annotation : annotations) {
			if (isAnnotationInjectable(pField, annotation, annotations)) {
				final BiConsumer<Object,Object> injector = Reflection.newInjector(pField);
				return (cf,o) -> {
					final Object value = getInjectableValue(cf, pField, annotation, annotations);
					injector.accept(o,  value);
				};
			}
		}
		return null;
	}

	private <A extends Annotation> boolean hasAnnotation(@NonNull Method pMethod, @NonNull Class<A> pAnnotationType) {
		final A annotation = pMethod.getAnnotation(pAnnotationType);
		return annotation != null;
	}

	@Override
	protected BiConsumer<IComponentFactory, Object> getInjector(Method pMethod) {
		final Class<?> cl = pMethod.getDeclaringClass();
		final Class<?> returnType = pMethod.getReturnType();
		final Class<?>[] parameterTypes = pMethod.getParameterTypes();
		if (IComponentFactoryAware.class.isAssignableFrom(cl)  &&
				"init".equals(pMethod.getName())  &&
				(returnType == null  ||  returnType == Void.TYPE)  &&
				parameterTypes.length == 0  &&  IComponentFactory.class == parameterTypes[0]) {
			final Consumer<Object> invoker = Reflection.newInvoker(pMethod);
			return (cf,inst) -> {
				invoker.accept(inst);
			};
		} else {
			final boolean postConstructPresent =
					hasAnnotation(pMethod, PostConstruct.class)
					||  hasAnnotation(pMethod, jakarta.annotation.PostConstruct.class);
			final boolean preDestroyPresent =
					hasAnnotation(pMethod, PreDestroy.class)
					||  hasAnnotation(pMethod, jakarta.annotation.PreDestroy.class);
			if (!postConstructPresent  &&  !preDestroyPresent) {
				throw new IllegalStateException("Invalid method: " + pMethod
						+ ", it isn't annotated with @PostConstruct, or @PreDestroy.");
			} else {
				if (returnType == Void.TYPE  ||  returnType == Void.class
						||  returnType == null) {
					if (parameterTypes.length != 0) {
						throw new IllegalStateException("Method " + pMethod + " is annotated with @PostConstruct, or @PreDestroy, "
								+ " but takes parameters.");
					}
				}
				final Consumer<Object> starter = postConstructPresent ? Reflection.newInvoker(pMethod) : null;
				final Consumer<Object> stopper = preDestroyPresent ? Reflection.newInvoker(pMethod) : null;
				return (cf,inst) -> {
					final ILifecycleController lc = cf.getInstance(ILifecycleController.class);
					if (lc == null) {
						if (postConstructPresent) {
							throw new IllegalStateException("The method " + pMethod
									+ " is annotated with @PostConstruct, but no component of type "
									+ ILifecycleController.class.getName()
									+ " has been configured in the IComponentFactory.");
						} else {
							throw new IllegalStateException("The method " + pMethod
									+ " is annotated with @PreDestroy, but no component of type "
									+ ILifecycleController.class.getName()
									+ " has been configured in the IComponentFactory.");
						}
					}
					final TerminableListener lcListener = new TerminableListener() {
						@Override
						public void start() {
							if (starter != null) {
								starter.accept(inst);
							}
						}

						@Override
						public void shutdown() {
							if (stopper != null) {
								stopper.accept(inst);
							}
						}
					};
					lc.addListener(lcListener);
				};
			}
		}
	}

	@Override
	public boolean isInstantiable(Type pType, Annotation[] pAnnotations) {
		return false;
	}

	@Override
	public Function<IComponentFactory, Object> getInstantiator(Type pType, Annotation[] pAnnotations) {
		return null;
	}

}
