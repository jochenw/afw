package com.github.jochenw.afw.core.log.slf4j;

import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.AbstractLogFactory;

public class Slf4jLogFactory extends AbstractLogFactory {
    @Override
    protected AbstractLog newLog(String pId) {
        return new Slf4jLog(this, pId);
    }

	@Override
	protected void init() {
		// Does nothing.
	}

}
