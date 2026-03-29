/**
 * 
 */
package com.github.jochenw.afw.core.log.app;

import com.github.jochenw.afw.core.util.Objects;

/** Default implementation of {@link IAppLog}, writing to {@link System#out}.
 */
public class SystemOutAppLog extends DefaultAppLog {
	/** Creates a new instance.
	 */
	public SystemOutAppLog() {
		super(Objects.requireNonNull(System.out));
	}
}
