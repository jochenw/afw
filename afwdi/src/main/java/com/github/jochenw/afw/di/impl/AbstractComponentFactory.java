package com.github.jochenw.afw.di.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.api.IBindingProvider;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.api.Scopes.Scope;


public abstract class AbstractComponentFactory implements IComponentFactory {
	public static class Configuration {
		private final Map<Key<Object>,IBinding<Object>> bindings;
		private final IAnnotationProvider annotationProvider;
		private final Scopes.Scope defaultScope;
		private final IComponentFactory parent;
		private final Set<Class<?>> staticInjectionClasses;
		private final List<IBindingProvider> bindingProviders;

		/** Creates a new configuration instance with the given parameters.
		 * @param pBindings The map of bindings.
		 * @param pAnnotationProvider The annotation provider.
		 * @param pDefaultScope The default scope.
		 * @param pParent The component factories parent.
		 * @param pStaticInjectionClasses The set of classes, that require injection of static
		 * methods, or fields.
		 * @param pBindingProviders A list with additional binding providers.
		 */
		public Configuration(Map<Key<Object>, IBinding<Object>> pBindings,
				IAnnotationProvider pAnnotationProvider,
				Scope pDefaultScope, IComponentFactory pParent,
				Set<Class<?>> pStaticInjectionClasses,
				List<IBindingProvider> pBindingProviders) {
			bindings = pBindings;
			annotationProvider = pAnnotationProvider;
			defaultScope = pDefaultScope;
			parent = pParent;
			staticInjectionClasses = pStaticInjectionClasses;
			bindingProviders = pBindingProviders;
		}

		/** Returns the map of bindings.
		 * @return The map of bindings.
		 */
		public Map<Key<Object>, IBinding<Object>> getBindings() { return bindings; }
		/** Returns the annotation provider.
		 * @return The annotation provider.
		 */
		public IAnnotationProvider getAnnotationProvider() { return annotationProvider;	}
		/** Returns the default scope.
		 * @return The default scope.
		 */
		public Scopes.Scope getDefaultScope() { return defaultScope; }
		/** Returns the component factories parent, if any, or null.
		 * @return The component factories parent, if any, or null.
		 */
		public IComponentFactory getParent() { return parent; }
		/** Returns the set of classes, that require injection of static
		 * methods, or fields.
		 * @return The set of classes, that require injection of static
		 * methods, or fields.
		 */
		public Set<Class<?>> getStaticInjectionClasses() { return staticInjectionClasses; }

		/** Returns the list of additional binding providers. These
		 * are not yet initialized, and it is the component factories
		 * responsibility to do that.
		 * @return The list of additional binding providers.
		 */
		public List<IBindingProvider> getBindingProviders() {
			return bindingProviders;
		}
	}

	private final Configuration configuration;

	/** Creates a new instance with the given configuration.
	 * @param pConfiguration The component factories configuration.
	 */
	protected AbstractComponentFactory(Configuration pConfiguration) {
		configuration = pConfiguration;
	}

	/** Returns the component factories configuration.
	 * @return The component factories configuration.
	 */
	public Configuration getConfiguration() {
		return configuration;
	}
}
