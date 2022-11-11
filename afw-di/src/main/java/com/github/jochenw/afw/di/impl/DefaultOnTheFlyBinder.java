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

	protected boolean isAnnotationInjectable(Field pField, Annotation pAnnotation, Annotation[] pAnnotations) {
		return pAnnotation instanceof LogInject  ||  pAnnotation instanceof PropInject;
	}

	protected boolean isAnnotationInjectable(Method pMethod, Annotation pAnnotation, Annotation[] pAnnotations) {
		return pAnnotation instanceof PostConstruct  ||  pAnnotation instanceof PreDestroy
				||  pAnnotation instanceof LogInject  ||  pAnnotation instanceof PropInject;
	}

	protected Object getLogger(IComponentFactory pFactory, Class<?> pType, String pId) {
		return null;
	}

	protected Object getProperty(IComponentFactory pFactory, Class<?> pType, String pId, String pDefaultValue,
			                     boolean pNullable) {
		return null;
	}

	
	protected Object getInjectableValue(IComponentFactory pComponentFactory,
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

	protected Object getInjectableProperty(IComponentFactory pComponentFactory, Member pMember, final PropInject pPropInject) {
		final String id;
		if (pPropInject.id().length() == 0) {
			id = pMember.getDeclaringClass().getCanonicalName() + "." + pMember.getName();
		} else {
			id = pPropInject.id();
		}
		final Class<?> type = getInjectableValueType(pMember);
		final Object property = getProperty(pComponentFactory, type, id, pPropInject.defaultValue(), pPropInject.nullable());
		if (property == null) {
			if (!pPropInject.nullable()) {
				throw new IllegalStateException("No property value is available with id " + id
						+ " to inject a value into " + pMember);
			}
		}
		return property;
	}

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

	protected Class<?> getInjectableValueType(Member pMember) {
		final Class<?> type;
		if (pMember instanceof Field) {
			type = ((Field) pMember).getType();
		} else if (pMember instanceof Method) {
			final Method mth = (Method) pMember;
			if (mth.getReturnType() == Void.TYPE  || mth.getReturnType() == Void.class
					||  mth.getReturnType() == null) {
				final Class<?>[] parameterTypes = mth.getParameterTypes();
				if (parameterTypes.length != 1) {
					type = parameterTypes[0];
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
	protected BiConsumer<IComponentFactory, Object> getInjector(Field pField) {
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
			final PostConstruct postConstruct = pMethod.getAnnotation(PostConstruct.class);
			final PreDestroy preDestroy = pMethod.getAnnotation(PreDestroy.class);
			if (postConstruct == null  &&  preDestroy == null) {
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
				final Consumer<Object> starter = postConstruct == null ? null : Reflection.newInvoker(pMethod);
				final Consumer<Object> stopper = preDestroy == null ? null : Reflection.newInvoker(pMethod);
				return (cf,inst) -> {
					final ILifecycleController lc = cf.getInstance(ILifecycleController.class);
					if (lc == null) {
						if (postConstruct == null) {
							throw new IllegalStateException("The method " + pMethod
									+ " is annotated with @PreDestroy, but no component of type "
									+ ILifecycleController.class.getName()
									+ " has been configured in the IComponentFactory.");
						} else {
							throw new IllegalStateException("The method " + pMethod
									+ " is annotated with @PostConstruct, but no component of type "
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
