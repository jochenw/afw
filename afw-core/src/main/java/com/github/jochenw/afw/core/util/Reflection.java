package com.github.jochenw.afw.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

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

    public static Map<String, Method> getGetters(Class<?> pClass) {
        final Map<String,Method> map = new HashMap<String,Method>();
        findGetters(map, pClass);
        return map;
    }

    private static void findGetters(Map<String,Method> pMap,
                                                  Class<?> pClass) {
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
}
