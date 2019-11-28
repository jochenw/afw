package com.github.jochenw.afw.core.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.github.jochenw.afw.core.stream.StreamController.MetaData;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;


/** Implementation of {@link StreamWriter}, which creates XML.
 */
public class XmlStreamWriter extends AbstractStreamWriter {
	protected static final Attributes NO_ATTRIBUTES = new AttributesImpl();
	private String namespaceUri;

	/** Returns the namespace URI.
	 * @return The namespace URI.
	 */
	public String getNamespaceUri() {
		return Objects.notNull(namespaceUri, XMLConstants.NULL_NS_URI);
	}

	/** Sets the namespace URI.
	 * @param pNamespaceUri The namespace URI.
	 */
	public void setNamespaceUri(String pNamespaceUri) {
		namespaceUri = pNamespaceUri;
	}

	@Override
	public void write(OutputStream pOut, Object pStreamable) throws IOException {
		final ContentHandler th = Transformers.newTransformerHandler(new StreamResult(pOut), OutputKeys.OMIT_XML_DECLARATION, "yes");
		try {
			th.startDocument();
			th.startPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX, getNamespaceUri());
			write(th, pStreamable);
			th.endPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX);
			th.endDocument();
		} catch (SAXException se) {
			throw Exceptions.show(se);
		}
	}

	@Override
	public void write(Writer pWriter, Object pStreamable) throws IOException {
		final ContentHandler th = Transformers.newTransformerHandler(new StreamResult(pWriter),
				                                                     OutputKeys.OMIT_XML_DECLARATION, "yes",
				                                                     OutputKeys.STANDALONE, "yes",
				                                                     OutputKeys.ENCODING, "UTF-8");
		try {
			th.startDocument();
			th.startPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX, getNamespaceUri());
			write(th, pStreamable);
			th.endPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX);
			th.endDocument();
		} catch (SAXException se) {
			throw Exceptions.show(se);
		}
	}

	/** <p>XML specific method for writing. This is internally used by
	 * {@link #write(OutputStream, Object)}, and {@link #write(Writer, Object)}.</p>
	 * <em>Note:</em< This method doesn't emit any of the events
	 * {@link ContentHandler#startDocument()}, {@link ContentHandler#endDocument()},
	 * {@link ContentHandler#startPrefixMapping(String, String)}, or
	 * {@link ContentHandler#endPrefixMapping(String)}. Doing that is the callers
	 * responsibility.
	 * @param pHandler A SAX handler, which will receive SAX events.
	 * @param pStreamable The object being serialized.
	 * @throws IOException An I/O error occurred.
	 * @throws SAXException A SAX error occurred.
	 */
	public void write(@Nonnull ContentHandler pHandler, @Nonnull Object pStreamable) throws IOException, SAXException {
		final String localName = requireStreamableId(pStreamable);
		final AttributesImpl attrs = new AttributesImpl();
		final MetaData metaData = getMetaData(pStreamable.getClass());
		metaData.forEach((s,f) -> {
			if (isTerse(f)  &&  isAtomic(f)) {
				final String value = asString(f, pStreamable);
				if (value != null) {
					attrs.addAttribute(XMLConstants.NULL_NS_URI, s, s, "CDATA",
							value);
				}
			}
		});
		pHandler.startElement(getNamespaceUri(), localName, localName, attrs);
		metaData.forEach((s,f) -> {
			final boolean terse = isTerse(f);
			final boolean atomic = isAtomic(f);
			if (atomic) {
				if (terse) {
					// Already handled as an attribute, so nothing to do.
				} else {
					final String value = asString(f, pStreamable);
					if (value != null) {
						final char[] valueChars = value.toCharArray();
						pHandler.startElement(getNamespaceUri(), s, s, NO_ATTRIBUTES);
						pHandler.characters(valueChars, 0, valueChars.length);
						pHandler.endElement(getNamespaceUri(), s, s);
					}
				}
			} else {
				if (terse) {
					throw new IllegalStateException("Unable to serialize the non-atomic field "
							+ f + " in a terse manner.");
				} else {
					final Object value = getValue(f, pStreamable);
					if (value != null) {
						write(pHandler, value);
					}
				}
			}
		});
		pHandler.endElement(getNamespaceUri(), localName, localName);
	}

}
