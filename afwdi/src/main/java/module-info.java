module com.github.jochenw.afw.di {
	exports com.github.jochenw.afw.di.simple;
	exports com.github.jochenw.afw.di.api;

	requires java.base;
	requires com.google.guice;
	requires jakarta.inject;
	requires java.logging;
	requires javax.inject;
}