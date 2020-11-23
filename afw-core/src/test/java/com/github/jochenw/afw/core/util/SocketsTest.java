package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.net.SocketImplFactory;

import org.junit.Test;

public class SocketsTest {
	@Test
	public void testGetDefaultSocketImplFactory() {
		Sockets.getDefaultSocketImplFactory();
	}

	@Test
	public void testGetDefaultServerSocketImplFactory() {
		Sockets.getDefaultServerSocketImplFactory();
	}
}
