/** The module {@code com.github.jochenw.afw.di} provides a JSR330 compliant
 * dependency injection framework.
 */
module com.github.jochenw.afw.di {
	exports com.github.jochenw.afw.di.api;
	exports com.github.jochenw.afw.di.guice;
	exports com.github.jochenw.afw.di.simple;

	requires java.base;
	requires jakarta.inject;
	requires java.logging;
	requires java.inject;
	requires com.google.guice;
	requires jakarta.annotation;
	requires java.annotation;
	requires transitive org.jspecify;
}
