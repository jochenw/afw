package com.github.jochenw.afw.di.api;


/** Interface of an object, that wants to be notified, after
 * injection is done, and the object is ready to use.
 */
public interface IComponentFactoryAware {
	/** Called by the component factory, when the object has been
	 *   created, and all values have been injected. In other
	 *   words: Called, if the component factory assumes, that
	 *   the object is ready to use. However, the object remains
	 *   hidden from application code, until this method has
	 *   been invoked successfully.
	 * @param pFactory The factory, that created this object. The factory may be
	 *   queried for required components.
	 * @throws Exception Initializing this object has failed.
	 */
	void init(IComponentFactory pFactory) throws Exception;
}
