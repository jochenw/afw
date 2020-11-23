package com.github.jochenw.afw.core.util;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImplFactory;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.net.LoggingSocketImplFactory;
import com.github.jochenw.afw.core.util.Functions.FailableCallable;
import com.github.jochenw.afw.core.util.Functions.FailableRunnable;

public class Sockets {
	public static <O> O callWithLogging(@Nonnull Consumer<String> pLogger, @Nonnull Level pLevel, @Nonnull FailableCallable<O,?> pCallable) {
		LoggingSocketImplFactory lsif = new LoggingSocketImplFactory(pLogger, pLevel);
		Throwable th = null;
		SocketImplFactory socketSif = null;
		SocketImplFactory serverSocketSif = null;
		O result = null;
		try {
			socketSif = getDefaultSocketImplFactory();
			serverSocketSif = getDefaultServerSocketImplFactory();
			Socket.setSocketImplFactory(lsif);
			ServerSocket.setSocketFactory(lsif);
			result = pCallable.call();
			Socket.setSocketImplFactory(socketSif);
			socketSif = null;
			ServerSocket.setSocketFactory(serverSocketSif);
			serverSocketSif = null;
		} catch (Throwable t) {
			th = t;
		} finally {
			if (socketSif != null) {
				try {
					Socket.setSocketImplFactory(socketSif);
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
			if (serverSocketSif != null) {
				try {
					Socket.setSocketImplFactory(serverSocketSif);
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
		}
		if (th != null) {
			throw Exceptions.show(th);
		}
		return result;
	}

	public static SocketImplFactory getDefaultServerSocketImplFactory() {
		try {
			final Field field = Reflection.getStaticField(Socket.class, "factory");
			field.setAccessible(true);
			return (SocketImplFactory) field.get(null);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static SocketImplFactory getDefaultSocketImplFactory() {
		try {
			final Field field = Reflection.getStaticField(ServerSocket.class, "factory");
			field.setAccessible(true);
			return (SocketImplFactory) field.get(null);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static void runWithLogging(@Nonnull Consumer<String> pLogger, @Nonnull Level pLevel, @Nonnull FailableRunnable<?> pRunnable) {
		final FailableCallable<Object,?> callable = () -> { pRunnable.run(); return null; };
		callWithLogging(pLogger, pLevel, callable);
	}

}
