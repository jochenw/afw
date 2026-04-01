package com.github.jochenw.afw.di.api;


/** Interface of an object, which implements
 * custom initialization beyond injection of
 * fields, or methods.
 */
public interface IComponentFactoryAware {
	/** Invoked, after the component factory has
	 * finished injection of fields, or methods
	 * to complete the objects initialization.
	 * The component factory will wait for
	 * the method calls completion, before it
	 * considers the object as ready to use.
	 * 
	 * @param pCf The component factory, which
	 *   initializes the object.
	 */
	public void init(IComponentFactory pCf);
}
