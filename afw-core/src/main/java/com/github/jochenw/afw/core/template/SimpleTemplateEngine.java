package com.github.jochenw.afw.core.template;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.jochenw.afw.core.el.DefaultPropertyResolver;
import com.github.jochenw.afw.core.el.ElEvaluator;
import com.github.jochenw.afw.core.el.ElReader;
import com.github.jochenw.afw.core.el.PropertyResolver;
import com.github.jochenw.afw.core.util.Exceptions;


public class SimpleTemplateEngine implements ITemplateEngine<Map<String,Object>> {
	private PropertyResolver propertyResolver;
	private final ElReader elReader = new ElReader();
	private ElEvaluator elEvalutor;
	private ClassLoader classLoader;
	private Charset templateCharset;
	private String uri;

	public SimpleTemplateEngine(PropertyResolver pPropertyResolver, ElEvaluator pEvaluator) {
		propertyResolver = pPropertyResolver;
		elEvalutor = pEvaluator;
	}

	public SimpleTemplateEngine() {
		this(new DefaultPropertyResolver(), null);
		elEvalutor = new ElEvaluator(propertyResolver);
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader pClassLoader) {
		classLoader = pClassLoader;
	}

	public Charset getTemplateCharset() {
		return templateCharset;
	}

	public void setTemplateCharset(Charset pTemplateCharset) {
		templateCharset = pTemplateCharset;
	}

	public PropertyResolver getPropertyResolver() {
		return propertyResolver;
	}

	public void setPropertyResolver(PropertyResolver pPropertyResolver) {
		propertyResolver = pPropertyResolver;
	}

	public ElEvaluator getElEvalutor() {
		return elEvalutor;
	}

	public void setElEvalutor(ElEvaluator pElEvalutor) {
		elEvalutor = pElEvalutor;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String pUri) {
		uri = pUri;
	}

	public ElReader getElReader() {
		return elReader;
	}

	@Override
	public Template<Map<String, Object>> getTemplate(String pUri) {
		final URL url = getClassLoader().getResource(pUri);
		if (url == null) {
			throw new IllegalArgumentException("Unable to locate template: " + pUri);
		}
		try (final InputStream in = url.openStream();
			 final Reader r = new InputStreamReader(in, getTemplateCharset())) {
			return getTemplate(r);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public Template<Map<String, Object>> getTemplate(Reader pReader) {
		final List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(pReader)) {
			for (;;) {
				final String line = br.readLine();
				if (line == null) {
					break;
				}
				lines.add(line);
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		return compile(lines);
	}

	protected Template<Map<String,Object>> compile(List<String> pLines) {
		final String[] array = pLines.toArray(new String[pLines.size()]);
		return new SimpleTemplateCompiler<Map<String,Object>>(propertyResolver, elReader, elEvalutor, uri).compile(array);
	}

	/** Creates a new instance with default settings: A {@link #DefaultPropertyResolver}, the current threads {@link Thread#getContextClassLoader()
	 * context class loader}, and the "UTF-8" character set.
	 * @return The created instance, ready to use.
	 */
	public static SimpleTemplateEngine newInstance() {
		final PropertyResolver pr = new DefaultPropertyResolver();
		final ElEvaluator evaluator = new ElEvaluator(pr);
		final ElReader reader = new ElReader();
		final SimpleTemplateEngine ste = new SimpleTemplateEngine();
		ste.setElEvalutor(evaluator);
		ste.setPropertyResolver(pr);
		ste.setTemplateCharset(StandardCharsets.UTF_8);
		ste.setClassLoader(Thread.currentThread().getContextClassLoader());
		return ste;
	}
}
