/**
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class Reflection {
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
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(pInstance, pValue);
        } catch (Throwable t) {
            throw Exceptions.show(t);
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

    public static boolean isPublicGetter(Method pMethod) {
    	if (!isGetter(pMethod)) {
    		return false;
    	}
    	if (!Modifier.isPublic(pMethod.getModifiers())) {
    		return false;
    	}
		return true;
	}

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
		final String id = pClassIds.getClass().getName();
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
}
