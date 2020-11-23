package com.github.jochenw.afw.core.net;

import java.net.SocketImpl;
import java.net.SocketImplFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.log.ILog.Level;

public class LoggingSocketImplFactory implements SocketImplFactory {
	private final Consumer<String> logger;
	private final long startTime = System.currentTimeMillis();
	private final Level level;

	public LoggingSocketImplFactory(Consumer<String> pLogger, Level pLevel) {
		this.logger = pLogger;
		this.level = pLevel;
	}

	protected boolean isEnabled(Level pLevel) {
		return pLevel.ordinal() >= level.ordinal();
	}

	protected void log(Level pLevel, String pId, String pMsg) {
		if (isEnabled(pLevel)) {
			final StringBuilder sb = new StringBuilder();
			sb.append((System.currentTimeMillis()-startTime));
			sb.append(" ");
			sb.append(pLevel.name());
			sb.append(" ");
			if (pId != null) {
				sb.append(pId);
				sb.append(": ");
			}
			sb.append(pMsg);
			logger.accept(sb.toString());
		}
	}

	protected void log(Level pLevel, String pId, String... pMsg) {
		if (isEnabled(pLevel)) {
			final StringBuilder sb = new StringBuilder();
			sb.append((System.currentTimeMillis()-startTime));
			sb.append(" ");
			sb.append(pLevel.name());
			sb.append(" ");
			if (pId != null) {
				sb.append(pId);
				sb.append(": ");
			}
			for (String s : pMsg) {
				sb.append(s);
			}
			logger.accept(sb.toString());
		}
	}

	private final AtomicLong idCounter = new AtomicLong();

	@Override
	public SocketImpl createSocketImpl() {
		return new LoggingSocketImpl(String.valueOf(idCounter.incrementAndGet()), this);
	}

}
