package com.github.jochenw.afw.rcm.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface RmResourceRefRepository {
	List<RmResourceRef> getResources(RmLogger pLogger);
	InputStream open(RmResourceRef pResource) throws IOException;
}
