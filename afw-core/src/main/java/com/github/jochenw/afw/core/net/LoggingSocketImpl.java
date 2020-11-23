package com.github.jochenw.afw.core.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.util.function.Consumer;

import org.apache.logging.log4j.util.Strings;

import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.util.Exceptions;

public class LoggingSocketImpl extends SocketImpl {
	private final Delegator delegator;
	private final String id;
	private final LoggingSocketImplFactory factory;

	public LoggingSocketImpl(String pId, LoggingSocketImplFactory pFactory) {
		id = pId;
		factory = pFactory;
		delegator = new Delegator(this, SocketImpl.class, "java.net.SocksSocketImpl");
	}

	private Socket getSocket() {
		try {
			final Field field = SocketImpl.class.getDeclaredField("socket");
			field.setAccessible(true);
			return (Socket) field.get(this);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected void log(Level pLevel, String pMsg) {
		factory.log(pLevel, id, pMsg);
	}

	protected void log(Level pLevel, String... pMsg) {
		factory.log(pLevel, id, pMsg);
	}

	@Override
	public void setOption(int pOptID, Object pValue) throws SocketException {
		log(Level.DEBUG, "setOption: optId=" + pOptID + ", value=" + pValue);
		delegator.invoke("setOption", Integer.valueOf(pOptID), pValue);
	}

	@Override
	public Object getOption(int pOptID) throws SocketException {
		return delegator.invoke("getOption", Integer.valueOf(pOptID));
	}

	@Override
	protected void create(boolean stream) throws IOException {
		log(Level.DEBUG, "create: stream=" + stream);
		delegator.invoke("create", Boolean.valueOf(stream));
	}

	@Override
	protected void connect(String host, int port) throws IOException {
		log(Level.INFO, "connect: host=", host, ", port=" + port);
		delegator.invoke("connect", host, Integer.valueOf(port));
	}

	@Override
	protected void connect(InetAddress pAddress, int port) throws IOException {
		log(Level.INFO, "connect: address=", pAddress.toString(), ", port=" + port);
		delegator.invoke("connect", address, Integer.valueOf(port));
	}

	@Override
	protected void connect(SocketAddress pAddress, int pTimeout) throws IOException {
		log(Level.INFO, "connect: socketAddress=" + pAddress, ", timeout=" + pTimeout);
		delegator.invoke("connect", pAddress, Integer.valueOf(pTimeout));
	}

	@Override
	protected void bind(InetAddress pHost, int pPort) throws IOException {
		log(Level.INFO, "bind: host=" + pHost, ", port=" + pPort);
		delegator.invoke("bind", pHost, Integer.valueOf(pPort));
	}

	@Override
	protected void listen(int backlog) throws IOException {
		log(Level.INFO, "listen: backlog=" + backlog);
		delegator.invoke("listen", Integer.valueOf("backlog"));
	}

	@Override
	protected void accept(SocketImpl s) throws IOException {
		log(Level.INFO, "accept: socketImpl=" + s);
		delegator.invoke("accept", s);
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		log(Level.DEBUG, "getInputStream");
		return delegator.invoke("getInputStream");
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		log(Level.DEBUG, "getOutputStream");
		return delegator.invoke("getOutputStream");
	}

	@Override
	protected int available() throws IOException {
		log(Level.DEBUG, "available");
		final Integer avail = delegator.invoke("available");
		return avail.intValue();
	}

	@Override
	protected void close() throws IOException {
		log(Level.DEBUG, "close");
		delegator.invoke("close");
	}

	@Override
	protected void sendUrgentData(int pData) throws IOException {
		log(Level.INFO, "sendUrgentData: data=" + pData);
		delegator.invoke("sendUrgentData", Integer.valueOf(pData));
	}

}
