package com.github.jochenw.afw.di.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.di.api.ComponentFactoryBuilder;
import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;


/** Abstract base class for implementations of {@link IComponentFactory}.
 * The {@link ComponentFactoryBuilder} assumes, that the type of the
 * created instance is derived from this class.
 */
public abstract class AbstractComponentFactory implements IComponentFactory {
	/** Creates a new instance. Protected, to avoid accidental instantiation.
	 * This constructor might be removed, it is mainly present to avoid a Javadoc
	 * warning with JDK 21.
	 */
	protected AbstractComponentFactory() {}

	private IAnnotationProvider annotationProvider;

	@Override
	public IAnnotationProvider getAnnotations() {
		return annotationProvider;
	}

	/** Sets the component factories annotation provider.
	 * The purpose of the annotation provider is adaptation
	 * to varying annotation namespaces, like
	 * {@code javax.inject}, or {@code jakarta.inject}. 
	 * @param pAnnotationProvider The annotation provider,
	 *   which is being used by the component factory.
	 */
	protected void setAnnotationProvider(IAnnotationProvider pAnnotationProvider) {
		annotationProvider = pAnnotationProvider;
	}

	/** Called by the {@link ComponentFactoryBuilder} to configure the
	 * component factory, after it has been created. A successfull
	 * invocation of this method indicates, that the component
	 * factory is now ready to use.
	 * @param pAnnotationProvider The annotation provider, that is being used.
	 * @param pOnTheFlyBinder Sets the on-the-fly binder, if any.
	 * @param pBuilders The list of binding builders
	 * @param pStaticInjectionClasses The set of classes, that need static
	 *   injection.
	 * @param pLogger The component factories logger.
	 */
	public abstract void configure(@NonNull IAnnotationProvider pAnnotationProvider,
			                       @NonNull IOnTheFlyBinder pOnTheFlyBinder,
			                       @NonNull List<BindingBuilder<Object>> pBuilders,
			                       @NonNull Set<Class<?>> pStaticInjectionClasses,
			                       @Nullable Consumer<String> pLogger);
			                       
}
