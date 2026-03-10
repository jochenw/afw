package com.github.jochenw.afw.di.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.jochenw.afw.di.api.IAnnotationProvider;
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

		/** Creates a new configuration instance with the given parameters.
		 * @param pBindings The map of bindings.
		 * @param pAnnotationProvider The annotation provider.
		 * @param pDefaultScope The default scope.
		 * @param pParent The component factories parent.
		 */
		public Configuration(Map<Key<Object>, IBinding<Object>> pBindings,
				IAnnotationProvider pAnnotationProvider,
				Scope pDefaultScope, IComponentFactory pParent) {
			bindings = pBindings;
			annotationProvider = pAnnotationProvider;
			defaultScope = pDefaultScope;
			parent = pParent;
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
	}
}
