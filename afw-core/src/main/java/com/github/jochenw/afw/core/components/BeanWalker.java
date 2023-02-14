/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Reflection;


/**
 * This component allows to iterate over a Java beans properties, using a
 * {@link BeanVisitor visitor}.
 */
public class BeanWalker {
	/** The context object is created by the {@link BeanVisitor visitor}.
	 * The visitor may use this to store the necessary information inside the
	 * context. Applied carefully, this allows thread safe visitor implementations. 
	 */
	public static class Context {
	}
	/** The visitor is invoked by the {@link BeanWalker bean walker},
	 * whenever the walker detects a new field.
	 * @param <C> The type of the context object.
	 */
	public interface BeanVisitor<C extends Context> {
		/** Called, when the walker starts iteration over a top level object.
		 * The main purpose is the creation of the context object.
		 * @param pObject The top level object, which is being iterated over.
		 * @return The context object.
		 */
		@Nonnull C startWalking(@Nonnull Object pObject);
		/** Called, when the walker has successfully iterated over a top
		 * level object. The main purpose is the cleanup of resources in
		 * the context object.
		 * @param pContext The context object.
		 * @param pObject The top level object, which has been iterated
		 * over.
		 */
		void endWalking(@Nonnull C pContext, @Nonnull Object pObject);
		/**
		 * Called, when a field has been detected. Takes as input the
		 * fields type.
		 * @param pContext The context object.
		 * @param pFieldName The fields name.
		 * @param pType The fields type.
		 * @return True, if this field is considered atomic, in which
		 *   case {@link #visitAtomicProperty(BeanWalker.Context, Field, Supplier, Consumer)}
		 *   will be invoked for this field. False, if this field isn't considered
		 *   atomic, in which case {@link #isComplex(BeanWalker.Context, String, Class)}
		 *   will be invoked.
		 */
		boolean isAtomic(C pContext, String pFieldName, Class<?> pType);
		/**
		 * Called, when a non-atomic field has been detected. Takes as input the
		 * fields type.
		 * @param pContext The context object.
		 * @param pFieldName The fields name.
		 * @param pType The fields type.
		 * @return True, if this field is considered complex, in which
		 *   case {@link #startVisiting(BeanWalker.Context, Object)}, and (later on)
		 *   {@link #endVisiting(BeanWalker.Context, Object)} will be invoked. False,
		 *   if this field isn't complex, in which case the field will be
		 *   ignored.
		 */
		boolean isComplex(C pContext, String pFieldName, Class<?> pType);
		/** Called, when an atomic field has been detected.
		 * @param pContext The context object.
		 * @param pField The field, which has been detected.
		 * @param pSupplier A supplier for the fields value.
		 * @param pConsumer A consumer for setting the fields value.
		 */
		void visitAtomicProperty(C pContext, Field pField, Supplier<Object> pSupplier,
				                 Consumer<Object> pConsumer);
		/** Called, when a complex field has been detected.
		 * @param pContext The context object.
		 * @param pBean The bean, which is about to be visited.
		 */
		void startVisiting(C pContext, Object pBean);
		/** Called, when a complex field has been detected, and iterated over.
		 * @param pContext The context object.
		 * @param pBean The bean, which has been visited.
		 */
		void endVisiting(C pContext, Object pBean);
	}

	private Comparator<Field> fieldComparator;

	/**
	 * Called to iterate over the given bean, and all its attributes.
	 * Complex attributes are being iterated over, recursively.
	 * @param <C> The type of the context object.
	 * @param pVisitor The visitor, which is being notified, if attributes
	 *   are found.
	 * @param pObject The object to iterate over.
	 */
	public <C extends Context> void walk(BeanVisitor<C> pVisitor, Object pObject) {
		final C context = pVisitor.startWalking(pObject);
		walkComplexObject(pVisitor, pObject, context);
		pVisitor.endWalking(context, pObject);
	}

	/**
	 * Called to iterate over a complex attribute of a parent bean.
	 * Complex attributes are being iterated over, recursively.
	 * @param <C> The type of the context object.
	 * @param pVisitor The visitor, which is being notified, if attributes
	 *   are found.
	 * @param pObject The object to iterate over.
	 * @param pContext The context object.
	 */
	protected <C extends Context> void walkComplexObject(BeanVisitor<C> pVisitor, Object pObject, final C pContext) {
		final Set<String> propertyNames = new HashSet<>();
		final FailableConsumer<Field, ?> consumer = (f) -> {
			final Class<?> type = f.getType();
			if (!"$jacocoData".equals(f.getName())) {
				if (pVisitor.isAtomic(pContext, f.getName(), type)) {
					final Supplier<Object> supplier = () -> {
						return getFieldValue(pObject, f);
					};
					final Consumer<Object> consmr = (o) -> {
						setFieldValue(pObject, f, o);
					};
					pVisitor.visitAtomicProperty(pContext, f, supplier, consmr);
					propertyNames.add(f.getName());
				} else if (pVisitor.isComplex(pContext, f.getName(), type)) {
					final Object object = getFieldValue(pObject, f);
					pVisitor.startVisiting(pContext, object);
					walkComplexObject(pVisitor, object, pContext);
					pVisitor.endVisiting(pContext, object);
				}
			}
		};
		final Comparator<Field> fieldComp = getFieldComparator();
		if (fieldComp == null) {
			Reflection.findFields(pObject.getClass(), consumer);
		} else {
			final List<Field> fields = new ArrayList<Field>();
			Reflection.findFields(pObject.getClass(), (f) -> fields.add(f));
			Collections.sort(fields, fieldComp);
			fields.forEach((f) -> Functions.accept(consumer, f));
		}
	}

	/** Called to inject a field value into a bean.
	 * @param pBean The bean, that will be updated.
	 * @param pField The field, that will be used to inject a value into the bean.
	 * @param pValue The value, that will be injected into the bean.
	 */
	@SuppressWarnings("deprecation")
	protected void setFieldValue(Object pBean, Field pField, Object pValue) {
		try {
			if (!pField.isAccessible()) {
				pField.setAccessible(true);
			}
			pField.set(pBean, pValue);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Called to retrieve a field value from a bean.
	 * @param pBean The bean, that will be updated.
	 * @param pField The field, that will be used to retrieve a value from the bean.
	 * @return The given fields value in the given bean.
	 */
	@SuppressWarnings("deprecation")
	protected Object getFieldValue(Object pBean, Field pField) {
		try {
			if (!pField.isAccessible()) {
				pField.setAccessible(true);
			}
			return pField.get(pBean);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** The order of visitor invocations depends on the order of fields, as returned by
	 * {@link Class#getDeclaredFields()}. In other words, the order of visitor invocations
	 * is, by default, unpredictable. Setting a field comparator changes this, because
	 * in that case the field list will be sorted.
	 * @param pComparator The comparator to use for sorting fields.
	 */
	public void setFieldComparator(Comparator<Field> pComparator) {
		fieldComparator = pComparator;
	}

	/** The order of visitor invocations depends on the order of fields, as returned by
	 * {@link Class#getDeclaredFields()}. In other words, the order of visitor invocations
	 * is, by default, unpredictable. Setting a field comparator changes this, because
	 * in that case the field list will be sorted.
	 * @return The comparator to use for sorting fields.
	 */
	public Comparator<Field> getFieldComparator() {
		return fieldComparator;
	}
}
