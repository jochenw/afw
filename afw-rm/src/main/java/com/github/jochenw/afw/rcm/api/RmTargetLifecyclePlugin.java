package com.github.jochenw.afw.rcm.api;

public interface RmTargetLifecyclePlugin {
	public abstract static class Context {
		private final String id;
		protected Context(String pId) {
			id = pId;
		}
		public String getId() {
			return id;
		}
	}
	Context start();
	void commit(Context pContext);
	void rollback(Context pContext);
}
