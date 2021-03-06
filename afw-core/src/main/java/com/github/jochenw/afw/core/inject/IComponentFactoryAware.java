package com.github.jochenw.afw.core.inject;


/** Interface of an object, that should be notified, as soon as it has
 * been configured by the {@link IComponentFactory}.
 */
public interface IComponentFactoryAware {
	/** Called, after the instance has been created, and it's dependencies
	 * have been injected.
	 * @param pFactory The component factory, that created this instance.
	 */
	public void init(IComponentFactory pFactory);
}
