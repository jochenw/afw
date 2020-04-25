/**
 * 
 */
package com.github.jochenw.afw.core.log.app;


/** Default implementation of {@link IAppLog}, writing to {@link System#out}.
 */
public class SystemOutAppLog extends DefaultAppLog {
	/**
	 */
	public SystemOutAppLog() {
		super(System.out);
	}

}
