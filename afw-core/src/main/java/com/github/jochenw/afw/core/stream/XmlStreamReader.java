package com.github.jochenw.afw.core.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.github.jochenw.afw.core.util.Sax;


/** Implementation of {@link StreamReader} for XML files, as created by the
 * {@link XmlStreamWriter}.
 */
public class XmlStreamReader extends AbstractStreamReader {
	/** A SAX handler, which is internally used by the {@link XmlStreamReader}.
	 */
	public class Handler implements ContentHandler {
		private final Class<Object> rootType;
		private final List<Data> dataStack = new ArrayList<>();
		private Data currentData;
		private Object bean;
		private Locator locator;
		private int level;
		private StringBuilder sb;
		private Consumer<String> sbUser;
		/**
		 * Creates a new instance, which is creating an instance of the given type by parsing an XML element.
		 * @param pType The type of the object, which is being created by the parser. Must
		 *   have a public default constructor, so that it can be instantiated via
		 *   {@link Class#newInstance()}.
		 */
		public Handler(Class<Object> pType) {
			rootType = pType;
		}
		protected SAXException error(String pMsg) {
			final String msg = Sax.asLocalizedMessage(locator, pMsg);
			return new SAXParseException(msg, locator);
		}
		protected void checkNamespace(String pExpectedUri, String pUri) throws SAXException {
			final String uri = pExpectedUri;
			if (uri == null  ||  uri.length() == 0) {
				if (pUri != null  &&  pUri.length() > 0) {
					throw error("Expected default namespace, got: " + pUri);
				}
			} else {
				if (pUri == null  ||  pUri.length() == 0) {
					throw error("Expected namespace " + uri + ", got default namespace");
				} else if (!uri.equals(pUri)) {
					throw error("Expected namespace " + uri + ", got " + pUri);
				}
			}
		}
		@Override
		public void setDocumentLocator(Locator pLocator) {
			locator = pLocator;
		}
		@Override
		public void startDocument() throws SAXException {
			level = 0;
			dataStack.clear();
			currentData = null;
			sb = null;
			sbUser = null;
		}
		@Override
		public void endDocument() throws SAXException {
			bean = Objects.requireNonNull(currentData.getBean(), "Bean");
		}
		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			// Ignore this
		}
		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			// Ignore this
		}
		@Override
		public void startElement(String pUri, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
			checkNamespace(getNamespaceUri(), pUri);
			if (pAttrs != null) {
				for (int i = 0;  i < pAttrs.getLength();  i++) {
					final String uri = pAttrs.getURI(i);
					checkNamespace(null, pUri);
				}
			}
			if (level++ == 0) {
				final Data data = newData(rootType);
				currentData = data;
			} else {
				final Field field = currentData.getMetaData().getField(pLocalName);
				if (field == null) {
					throw error("Invalid element " + pLocalName
							    + ", no matching field available in class "
							    + currentData.getType().getName());
				}
				if (isAtomic(field)) {
					final Object bean = currentData.getBean();
					sb = new StringBuilder();
					sbUser = (s) -> {
						final Object o = fromString(s, field);
						setValue(field, bean, o);
						return;
					};
				} else {
					@SuppressWarnings("unchecked")
					final Class<Object> cl = (Class<Object>) Objects.requireNonNull(field.getType(), "Field type");
					final Data data = newData(cl);
					dataStack.add(currentData);
					currentData = data;
				}
			}
		}
		@Override
		public void endElement(String pUri, String pLocalName, String pQName) throws SAXException {
			checkNamespace(getNamespaceUri(), pUri);
			--level;
			if (sb != null) {
				sbUser.accept(sb.toString());
				sb = null;
				sbUser = null;
			} else {
				if (level > 0) {
					final Data curData = currentData;
					currentData = dataStack.remove(dataStack.size()-1);
					final Field field = currentData.getMetaData().getField(pLocalName);
					setValue(field, currentData.getBean(), curData.getBean());
				}
			}
		}
		@Override
		public void characters(char[] pChars, int pStart, int pLength) throws SAXException {
			if (sb != null) {
				sb.append(pChars, pStart, pLength);
			}
		}

		@Override
		public void ignorableWhitespace(char[] pChars, int pStart, int pLength) throws SAXException {
			if (sb != null) {
				sb.append(pChars, pStart, pLength);
			}
		}
		@Override
		public void processingInstruction(String pTarget, String pData) throws SAXException {
			throw error("Unexpected Processing Instruction: target=" + pTarget + ", data=" + pData);
		}
		@Override
		public void skippedEntity(String pName) throws SAXException {
			throw error("Unexpected Skipped Entity: name=" + pName);
		}
		/**
		 * Returns the instance, which has been created by the parser.
		 * @return The instance, which has been created by the parser.
		 * @throws IllegalStateException No parsing has been done, so a result
		 *   bean is not yet available.
		 */
		public Object getBean() {
			if (bean == null) {
				throw new IllegalStateException("Result bean is not yet available.");
			}
			return bean;
		}
	}

	private String namespaceUri;

	/**
	 * Returns the namespace URI, which elements in the parsed document are supposed
	 * to have.
	 * @return The namespace URI, which elements in the parsed document are supposed
	 * to have.
	 * @see #setNamespaceUri(String)
	 */
	public String getNamespaceUri() {
		return namespaceUri;
	}

	/**
	 * Sets the namespace URI, which elements in the parsed document are supposed
	 * to have.
	 * @param pNamespaceUri The namespace URI, which elements in the parsed document are supposed
	 * to have.
	 * @see #getNamespaceUri()
	 */
	public void setNamespaceUri(String pNamespaceUri) {
		this.namespaceUri = pNamespaceUri;
	}

	public <O> O read(@Nonnull InputSource pSource, @Nonnull Class<O> pType) {
		@SuppressWarnings("unchecked")
		final Class<Object> cl = (Class<Object>) pType;
		final Handler handler = new Handler(cl);
		Sax.parse(pSource, handler);
		@SuppressWarnings("unchecked")
		final O o = (O) handler.getBean();
		return o;
	}

	@Override
	public <O> O read(InputStream pIn, Class<O> pType) throws IOException {
		final InputSource isource = new InputSource(pIn);
		return read(isource, pType);
	}

	@Override
	public <O> O read(Reader pReader, Class<O> pType) throws IOException {
		final InputSource isource = new InputSource(pReader);
		return read(isource, pType);
	}

}
