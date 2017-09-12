package com.github.jochenw.afw.jsgen.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSGQName implements Serializable {
	private static final long serialVersionUID = -1649650425118415663L;
	public static final JSGQName BOOLEAN = newInstance(Boolean.TYPE);
	public static final JSGQName BOOLEAN_OBJ = newInstance(Boolean.class);
	public static final JSGQName BYTE = newInstance(Byte.TYPE);
	public static final JSGQName BYTE_OBJ = newInstance(Byte.class);
	public static final JSGQName CHAR = newInstance(Character.TYPE);
	public static final JSGQName CHAR_OBJ = newInstance(Character.class);
	public static final JSGQName DOUBLE = newInstance(Double.TYPE);
	public static final JSGQName DOUBLE_OBJ = newInstance(Double.class);
	public static final JSGQName FLOAT = newInstance(Float.TYPE);
	public static final JSGQName FLOAT_OBJ = newInstance(Float.class);
	public static final JSGQName INT = newInstance(Integer.TYPE);
	public static final JSGQName INT_OBJ = newInstance(Integer.class);
	public static final JSGQName LIST = newInstance(List.class);
	public static final JSGQName LONG = newInstance(Long.TYPE);
	public static final JSGQName LONG_OBJ = newInstance(Long.class);
	public static final JSGQName MAP = newInstance(Map.class);
	public static final JSGQName OBJECT = newInstance(Object.class);
	public static final JSGQName SET = newInstance(Set.class);
	public static final JSGQName SHORT = newInstance(Short.TYPE);
	public static final JSGQName SHORT_OBJ = newInstance(Short.class);
	public static final JSGQName STRING = newInstance(String.class);
	public static final JSGQName VOID = newInstance(Void.TYPE);

	private final String packageName, qName, simpleName;
	private final boolean isArray, isPrimitive, isInner;
	private final JSGQName componentName, outerName;

	private JSGQName(String pPackageName, String pQName, String pSimpleName,
			         boolean pArray, boolean pPrimitive, boolean pInner, JSGQName pComponentName, JSGQName pOuterName) {
		packageName = pPackageName;
		qName = pQName;
		simpleName = pSimpleName;
		isArray = pArray;
		isPrimitive = pPrimitive;
		isInner = pInner;
		componentName = pComponentName;
		outerName = pOuterName;
	}

	public String getPackageName() {
		return packageName;
	}


	public String getQName() {
		return qName;
	}


	public String getSimpleName() {
		return simpleName;
	}


	public boolean isArray() {
		return isArray;
	}


	public boolean isPrimitive() {
		return isPrimitive;
	}


	public boolean isInnerName() {
		return isInner;
	}


	public JSGQName getComponentName() {
		return componentName;
	}


	public JSGQName getOuterName() {
		return outerName;
	}

	public static JSGQName newInstance(Class<?> pClass) {
		String qName = pClass.getName();
		if (pClass.isPrimitive()) {
			return new JSGQName("", qName, qName, false, true, false, null, null);
		}
		final boolean isArray = pClass.isArray();
		final JSGQName componentName;
		if (isArray) {
			qName = pClass.getComponentType().getName();
			componentName = newInstance(pClass.getComponentType());
		} else {
			componentName = null;
		}
		final int offset = qName.lastIndexOf('.');
		if (offset == -1) {
			int innerOffset = qName.lastIndexOf('$');
			if (innerOffset != -1) {
				throw new IllegalStateException("Inner classes not implemented for default package.");
			}
			return new JSGQName("", qName, qName, isArray, false, false, componentName, null);
		} else {
			final String simpleName = qName.substring(offset+1);
			final String packageName = qName.substring(0, offset);
			int innerOffset = simpleName.lastIndexOf('$');
			if (innerOffset != -1) {
				throw new IllegalStateException("Inner classes not yet implemented.");
			}
			return new JSGQName(packageName, qName, simpleName, isArray, false, false, componentName, null);
		}
	}
}
