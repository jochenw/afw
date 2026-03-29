/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailablePredicate;


/**
 * Utility class for working with Java reflection.
 */
public class Reflection {
	/** Creates a new instance. Private, to avoid accidental instantiaton.
	 * This default constructor might be removed, it is mainly present to
	 * avoid a Javadoc warning with JDK 21.
	 */
	public Reflection() {}

	/** Searches for a field with the given name in the given
     * instance. If such a field is found, sets it to the given value.
     * Otherwise, throws an exception.
     * @param pInstance The instance being modified by setting the given value
     *   in the given field.
     * @param pField The field name being searched for.
     * @param pValue The fields new value.
     */
	public static void setValue(Object pInstance, String pField, Object pValue) {
    	if (pField == null  ||  pField.length() == 0) {
    		throw new NullPointerException("The field name must not be null, or empty.");
    	}
    	final String fieldName = Character.toLowerCase(pField.charAt(0)) + pField.substring(1);
        try {
            final Field field = findField(pInstance.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldError("Field not found: "
                        + pInstance.getClass().getName() + "." + pField);
            }
            makeAcccessible(field);
            field.set(pInstance, pValue);
        } catch (Throwable t) {
            throw Exceptions.show(t);
        }
    }

	/** Ensures, that the given method, or field, is accessible.
	 * @param pField The method, or field, which is being made accessible.
	 */
	@SuppressWarnings("deprecation")
	public static void makeAcccessible(AccessibleObject pField) {
		if (!pField.isAccessible()) {
			pField.setAccessible(true);
		}
	}
	
    private static Field findField(Class<?> pClass, String pFieldName) {
        final List<String> cache = new ArrayList<String>();
        return findField(cache, pClass, pFieldName);
    }

    private static Field findField(List<String> pCache, Class<?> pClass, String pFieldName) {
        String id = pClass.getName();
        if (pCache.contains(id)) {
            return null;
        }
        try {
            return pClass.getDeclaredField(pFieldName);
        } catch (NoSuchFieldException e) {
            // Ignore the exception.
        }
        final Class<?> superClass = pClass.getSuperclass();
        if (superClass != null  &&  superClass != Object.class) {
            final Field f = findField(pCache, superClass, pFieldName);
            if (f != null) {
                return f;
            }
        }
        pCache.add(id);
        return null;
    }

    private static Field findStaticField(Class<?> pClass, String pFieldName) {
        try {
            return pClass.getDeclaredField(pFieldName);
        } catch (NoSuchFieldException e) {
            // Ignore the exception.
        }
        return null;
    }

    /**  Returns, whether the given method is a public getter.
     * @param pMethod The method being checked.
     * @return True, if the given method is a public getter. Otherwise false.
     */
    public static boolean isPublicGetter(Method pMethod) {
    	if (!isGetter(pMethod)) {
    		return false;
    	}
    	if (!Modifier.isPublic(pMethod.getModifiers())) {
    		return false;
    	}
		return true;
	}

    /**  Returns, whether the given method is a getter.
     * @param pMethod The method being checked.
     * @return True, if the given method is a getter. Otherwise false.
     */
    public static boolean isGetter(Method pMethod) {
    	final int modifiers = pMethod.getModifiers();
		if (Modifier.isAbstract(modifiers)) {
    		return false;
    	}
		if (Modifier.isStatic(modifiers)) {
			return false;
		}
		if (pMethod.getParameterCount() != 0) {
			return false;
		}
		final Class<?> type = pMethod.getReturnType();
		return type != Void.TYPE;
	}

    /** Returns a map of public getters in the given class
     * (including super classes).
     * @param pClass The class, which is being searched for public getters.
     * @return A map. The map keys are bean property names. The map
     *   values are public getters for the respective bean property
     *   names. The map contains no null values.
     */
    public static Map<String, Method> getGetters(Class<?> pClass) {
        final Map<String,Method> map = new HashMap<String,Method>();
        findGetters(map, pClass);
        return map;
    }

    private static void findGetters(Map<String,Method> pMap, Class<?> pClass) {
        final Class<?> superClass = pClass.getSuperclass();
        if (superClass != null  &&  superClass != Object.class) {
            findGetters(pMap, superClass);
        }
        final Method[] methods = pClass.getDeclaredMethods();
        for (Method m : methods) {
            final String beanProperty = checkBeanProperty(m);
            if (beanProperty != null) {
                pMap.put(beanProperty, m);
            }
        }
    }

    private static String checkBeanProperty(Method pMethod) {
        final Class<?> type = pMethod.getReturnType();
        if (type == null  ||  type == Void.TYPE) {
            return null;
        }
        final Class<?>[] parameterTypes = pMethod.getParameterTypes();
        if (parameterTypes == null  ||  parameterTypes.length > 0) {
            return null;
        }
        if (!Modifier.isPublic(pMethod.getModifiers())) {
            return null;
        }
        if (Modifier.isStatic(pMethod.getModifiers())) {
            return null;
        }
        if (Modifier.isAbstract(pMethod.getModifiers())) {
            return null;
        }
        final String name = pMethod.getName();
        final String suffix;
        if (name.length() > 3  &&  name.startsWith("get")) {
            suffix = Character.toLowerCase(name.charAt(3)) + name.substring(4);
        } else if (type == Boolean.TYPE  &&  name.length() > 2   &&  name.startsWith("is")) {
            suffix = Character.toLowerCase(name.charAt(2)) + name.substring(3);
        } else {
            suffix = null;
        }
        return suffix;
    }

	/** Searches for a public void method in the given class, which is
	 * annotated with the given annotation, has the given signature.
	 * 
	 * @param pMethodClass The class being searched for a matching method.
	 *   Super classes are being ignored.
	 * @param pAnnotationClass Class of the annotation, with
	 *   which the requested method must be annotated.
	 * @param pSignature The requested methods signature.
	 * @return A public void method in the given class, which is
	 * annotated with the given annotation, and has the given signature.
	 * If no such method exists, returns null.
	 */
	public static Method findPublicVoidMethodAnnotatedWith(Class<? extends Object> pMethodClass, Class<? extends Annotation> pAnnotationClass,
			                                     Class<?>... pSignature) {
		final Method[] methods = pMethodClass.getDeclaredMethods();
		for (Method m : methods) {
			if (m.isAnnotationPresent(pAnnotationClass)) {
				if (m.getReturnType() == Void.TYPE) {
					if (Modifier.isPublic(m.getModifiers())) {
						if (!Modifier.isStatic(m.getModifiers())) {
							if (!Modifier.isAbstract(m.getModifiers())) {
								final Class<?>[] parameterTypes = m.getParameterTypes();
								if (parameterTypes.length == pSignature.length) {
									boolean match = true;
									for (int i = 0;  i < parameterTypes.length;  i++) {
										if (parameterTypes[i] == pSignature[i]) {
											match = false;
											break;
										}
									}
									if (match) {
										return m;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static interface Matcher<T> {
		boolean isMatching(T pObject);
	}

	/** Checks, whether the given class has a public getter for the given
	 * property. If so, returns that getter. Otherwise, returns null.
	 * @param pClass The class being checked.
	 * @param pProperty The property, for which a public getter is being searched.
	 * @return A public getter for the given property, if such a getter exists, or null.
	 */
	public static Method getPublicGetter(Class<? extends Object> pClass, String pProperty) {
		Objects.requireNonNull(pClass, "Class");
		Objects.requireNonNull(pProperty, "Property");
		if (pProperty.length() == 0) {
			throw new IllegalArgumentException("The property name must not be empty.");
		}
		String suffix = Character.toUpperCase(pProperty.charAt(0)) + pProperty.substring(1);
		final String propertyNameUsingGet = "get" + suffix;
		final String propertyNameUsingIs = "is" + suffix;
		final String propertyNameUsingHas = "has" + suffix;
		final Set<String> classIds = new HashSet<>();
		final Matcher<Method> matcher = new Matcher<Method>(){
			@Override
			public boolean isMatching(Method pMethod) {
				final String methodName = pMethod.getName();
				final Class<?> type = pMethod.getReturnType();
				if (propertyNameUsingGet.equals(methodName)
					  ||  ((propertyNameUsingIs.equals(methodName)  ||  propertyNameUsingHas.equals(methodName))  &&
						   (type == Boolean.class  ||  type == Boolean.TYPE))) {
					final Class<?>[] parameterTypes = pMethod.getParameterTypes();
					if (parameterTypes == null  ||  parameterTypes.length == 0) {
						final int modifiers = pMethod.getModifiers();
						if (Modifier.isPublic(modifiers)
								&&  !Modifier.isAbstract(modifiers)
								&&  !Modifier.isStatic(modifiers)) {
							return true;
						}
					}
				}
				return false;
			}
		};
		return findMethodMatching(classIds, matcher, pClass);
	}

	private static Method findMethodMatching(Set<String> pClassIds, Matcher<Method> pMatcher, Class<?> pClass) {
		if (pClass == null  ||  pClass == Object.class) {
			return null;
		}
		final String id = pClass.getName();
		if (pClassIds.contains(id)) {
			return null;
		}
		pClassIds.add(id);
		final Method[] methods = pClass.getDeclaredMethods();
		for (Method m : methods) {
			if (pMatcher.isMatching(m)) {
				return m;
			}
		}
		return findMethodMatching(pClassIds, pMatcher, pClass.getSuperclass());
	}


	/**
	 * Searches for declared fields (including private fields), in the given
	 * type (including parent type(s)).
	 * Any such field is reported by invoking the given {@code pConsumer}.
	 * @param pType The type to inspect for fields.
	 * @param pConsumer The consumer to notify about fields, which are detected.
	 */
	public static void findFields(@NonNull Class<?> pType, @NonNull FailableConsumer<Field,?> pConsumer) {
		findFields(pType, pConsumer, null);
	}

	/**
	 * Searches for declared fields (including private fields), in the given
	 * type (including parent type(s)).
	 * Any such field is reported by invoking the given {@code pConsumer}.
	 * @param pType The type to inspect for fields.
	 * @param pConsumer The consumer to notify about fields, which are detected.
	 * @param pMatcher A filter, which can be used to ignore fields. May be null,
	 *   in which case a "matches all" filter will be assumed.
	 */
	public static void findFields(@NonNull Class<?> pType, @NonNull FailableConsumer<Field,?> pConsumer,
			                      @Nullable FailablePredicate<Field,?> pMatcher) {
		final @NonNull Class<?> type = Objects.requireNonNull(pType, "Type");
		try {
			if (type != Object.class) {
				Field[] fields = pType.getDeclaredFields();
				if (fields != null) {
					for (Field f : fields) {
						if (pMatcher == null  ||  pMatcher.test(f)) {
							pConsumer.accept(f);
						}
					}
				}
				final @NonNull Class<?> parentType = Objects.requireNonNull(pType.getSuperclass());
				findFields(parentType, pConsumer, pMatcher);
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Returns a setter for the given property name in the given class, if
	 * such a setter exists. Otherwise, returns null. 
	 * @param pClass The class, in which to search for setters.
	 * @param pProperty The property name, for which a public setter is being searched.
	 * @return A public setter with the given property name.
	 */
	public static Method getPublicSetter(Class<? extends Object> pClass, String pProperty) {
		Objects.requireNonNull(pClass, "Class");
		Objects.requireNonNull(pProperty, "Property");
		if (pProperty.length() == 0) {
			throw new IllegalArgumentException("The property name must not be empty.");
		}
		String suffix = Character.toUpperCase(pProperty.charAt(0)) + pProperty.substring(1);
		final String setterName = "set" + suffix;
		final Set<String> classIds = new HashSet<>();
		final Matcher<Method> matcher = new Matcher<Method>(){
			@Override
			public boolean isMatching(Method pMethod) {
				final String methodName = pMethod.getName();
				final Class<?> type = pMethod.getReturnType();
				if (setterName.equals(methodName)  &&  (type == null  ||  Void.TYPE == type)) {
					final Class<?>[] parameterTypes = pMethod.getParameterTypes();
					if (parameterTypes != null  &&  parameterTypes.length == 1) {
						final int modifiers = pMethod.getModifiers();
						if (Modifier.isPublic(modifiers)
								&&  !Modifier.isAbstract(modifiers)
								&&  !Modifier.isStatic(modifiers)) {
							return true;
						}
					}
				}
				return false;
			}
		};
		return findMethodMatching(classIds, matcher, pClass);
	}

	/** Returns a non-static field with the given name in the given class, if
	 * such a field exists. Otherwise, returns null. 
	 * @param pClass The class, in which to search for fields.
	 * @param pName The field name.
	 * @return A private, or public, field with the given name.
	 */
	public static Field getField(Class<? extends Object> pClass, String pName) {
		return findField(pClass, pName);
	}

	/** Returns a static field with the given name in the given class, if
	 * such a field exists. Otherwise, returns null. 
	 * @param pClass The class, in which to search for fields.
	 * @param pName The field name.
	 * @return A private, or public, field with the given name.
	 */
	public static Field getStaticField(Class<? extends Object> pClass, String pName) {
		return findStaticField(pClass, pName);
	}

	/** Creates a new instance of the given type by searching for a constructor, which
	 * accepts the given arguments. If the arguments are not exactly matching the
	 * signature, then you may pass additional arguments, that are class objects, in
	 * front of the actual arguments. If so, then the class arguments are taken to
	 * indicate the actual signature.
	 * For example, suggest the following classes:
	 * <pre>
	 *   public class IntClass { public IntClass(Integer pNumber) { ... } }
	 *   public class NumberClass { public NumberClass(Number pNumber) { ... } } 
	 * </pre>
	 * In the example, the following would work:
	 * <pre>
	 *   newObject("IntClass", Integer.valueOf(42));
	 * </pre>
	 * This however, wouldn't work, because there is no constructor NumberClass(Integer).
	 * <pre>
	 *   newObject("NumberClass", Integer.valueOf(42));
	 * </pre>
	 * To get this working, use
	 * <pre>
	 *   newObject("NumberClass", Number.class, Integer.valueOf(42));
	 * </pre>
	 * @param <O> The result type.
	 * @param pType Name of the class, which is being instantiated
	 * @param pArgs The constructor arguments. If the first arguments is a class, then
	 *   this argument, and all following arguments, that are classes, are used as the
	 *   constructors signature.
	 * @return The created object.
	 */
	public static <O> O newObject(String pType, Object... pArgs) {
		try {
			final List<Class<?>> classes = new ArrayList<>();
			final List<Object> objects = new ArrayList<>(Arrays.asList(pArgs));
			while (!objects.isEmpty()  &&  objects.get(0) instanceof Class) {
				final Class<?> cl = (Class<?>) objects.remove(0);
				classes.add(cl);
			}
			if (classes.isEmpty()) {
				for (Object o : objects) {
					final Class<?> cl = o.getClass();
					classes.add(cl);
				}
			}
			@SuppressWarnings("unchecked")
			final Class<Object> cl1 = (Class<Object>) Class.forName(pType);
			final Class<Object> cl = cl1;
			final Constructor<Object> cons = cl.getDeclaredConstructor(classes.toArray(new Class<?>[classes.size()]));
			cons.setAccessible(true);
			@SuppressWarnings("unchecked")
			final O o = (O) cons.newInstance(objects.toArray(new Object[objects.size()]));
			return o;
		} catch (InvocationTargetException e) {
			throw Exceptions.show(Objects.requireNonNull(e.getCause()));
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * A generic cast operation, that can be used without compiler warnings, or the like.
	 * @param <O> The target type of the cast. (The type, to which the input object is being
	 * casted to.
	 * @param pObject The source object, that is being casted to the target type.
	 * @return The source object. The compiler will now assume, that this object has the
	 *   target type.
	 */
	public static <O> O cast(Object pObject) {
		@SuppressWarnings("unchecked")
		final O o = (O) pObject;
		return o;
	}
}
