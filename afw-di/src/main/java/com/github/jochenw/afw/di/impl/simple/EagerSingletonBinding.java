package com.github.jochenw.afw.di.impl.simple;

import java.util.Objects;
import java.util.function.Function;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.api.Scopes;

/** Implementation of a binding with scope {@link Scopes#EAGER_SINGLETON}.
 */
public class EagerSingletonBinding extends AbstractBinding implements IComponentFactoryAware {
	/** Creates a new instance with the given supplier.
	 * @param pBaseSupplier The supplier, that is actually creating the instance.
	 */
	public EagerSingletonBinding(Function<SimpleComponentFactory,Object> pBaseSupplier) {
		super(pBaseSupplier);
	}

	private Object instance;

	@Override
	public void init(IComponentFactory pFactory) throws Exception {
		instance = super.apply((SimpleComponentFactory) pFactory);
	}

	@Override
	public Object apply(SimpleComponentFactory pFactory) {
		return Objects.requireNonNull(instance, "Instance");
	}
}
