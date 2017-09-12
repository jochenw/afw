package com.github.jochenw.afw.core.log;

import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;

public class DefaultLogFactory extends AbstractLogFactory implements ILogFactory, LogManager.Listener {
    private AbstractLogFactory lf = new SimpleLogFactory();
    private boolean initialized;
    private final List<DefaultLog> loggers = new ArrayList<DefaultLog>();

    @Override
    protected AbstractLog newLog(String pId) {
        if (initialized) {
            return (AbstractLog) lf.getLog(pId);
        } else {
            final DefaultLog df = new DefaultLog(this, pId);
            df.logFactoryChanged(lf);
            loggers.add(df);
            return df;
        }
    }

    @Override
    public synchronized void logFactoryChanged(ILogFactory pFactory) {
        lf = (AbstractLogFactory) pFactory;
        for (DefaultLog log : loggers) {
            log.logFactoryChanged(pFactory);
        }
        initialized = true;
    }

	@Override
	protected void init() {
		// Does nothing.
	}
}
