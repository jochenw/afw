package com.github.jochenw.afw.rcm.api;

import java.io.IOException;
import java.io.InputStream;

public interface RmResourceRefGuesser {
	public interface RmResourceInfoRequest {
		RmLogger getLogger();
		RmResourceRef getResourceRef();
		InputStream open() throws IOException;
	}
	RmResourceInfo getInfo(RmResourceInfoRequest pRequest) throws IOException;
}
