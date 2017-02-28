package com.github.jochenw.afw.lc;

import java.sql.Connection;

public interface IConnectionProvider {
	public Connection newConnection();
	public void close(Connection pConnection);
}
