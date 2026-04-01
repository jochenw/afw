/** The module {@code com.github.jochenw.afw.core} contains a set of tools,
 * and utilities, roughly like Guava, or the Apache Commons.
 */
module com.github.jochenw.afw.core {
	exports com.github.jochenw.afw.core.extprop;
	exports com.github.jochenw.afw.core.log.simple;
	exports com.github.jochenw.afw.core.components;
	exports com.github.jochenw.afw.core.data;
	exports com.github.jochenw.afw.core.template;
	exports com.github.jochenw.afw.core.vdn;
	exports com.github.jochenw.afw.core.csv;
	exports com.github.jochenw.afw.core.json;
	exports com.github.jochenw.afw.core.log;
	exports com.github.jochenw.afw.core.io;
	exports com.github.jochenw.afw.core.el.jcc;
	exports com.github.jochenw.afw.core.log.log4j;
	exports com.github.jochenw.afw.core.el;
	exports com.github.jochenw.afw.core.log.slf4j;
	exports com.github.jochenw.afw.core.el.tree;
	exports com.github.jochenw.afw.core.app;
	exports com.github.jochenw.afw.core.exec;
	exports com.github.jochenw.afw.core.scripts;
	exports com.github.jochenw.afw.core.cli;
	exports com.github.jochenw.afw.core;
	exports com.github.jochenw.afw.core.util.tests;
	exports com.github.jochenw.afw.core.util;
	exports com.github.jochenw.afw.core.props;
	exports com.github.jochenw.afw.core.rflct;
	exports com.github.jochenw.afw.core.function;
	exports com.github.jochenw.afw.core.log.app;
	exports com.github.jochenw.afw.core.plugins;
	exports com.github.jochenw.afw.core.crypt;
	exports com.github.jochenw.afw.core.jdbc;

	requires com.google.guice;
	requires jakarta.annotation;
	requires jakarta.inject;
	requires java.inject;
	requires transitive java.sql;
	requires ch.qos.reload4j;
	requires java.xml;
	requires transitive org.glassfish.java.json;
	requires java.annotation;
	requires transitive org.apache.groovy;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires org.bouncycastle.pkix;
	requires org.bouncycastle.provider;
	requires org.jspecify;
	requires org.slf4j;
	requires transitive com.github.jochenw.afw.di;
}