package com.github.jochenw.afw.di.api;


/** Interface of an object, that wants to be notified, after
 * injection is done, and the object is ready to use.
 */
public interface IComponentFactoryAware {
	void init(IComponentFactory pFactory) throws Exception;
}
