package com.github.jochenw.afw.core.util;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Utility class for working with Stax parsers.
 */
public class Stax {
	/**
	 * Returns a string, which includes the given message, and the given location information.
	 * @param pLoc The object providing the location information. If there is no such object
	 *   (the value is null), then the message string will be returned, as it is.
	 * @param pMsg The message string, which is being augmented with location information.
	 * @return A string in the format "At <LOCATION_INFORMATION>: <MESSAGE>", if location
	 *   information is provided. Otherwise, returns the message string, as it is.
	 */
	public static String asLocalizedMessage(Location pLoc, String pMsg) {
		if (pLoc == null) {
			return pMsg;
		} else {
			return asLocalizedMessage(pMsg, pLoc.getSystemId(), pLoc.getLineNumber(), pLoc.getColumnNumber());
		}
	}

	/**
	 * Returns a string, which describes the given location.
	 * @param pLoc The object providing the location information. If this parameter is null,
	 *   then the words "Unknown location" will be returned.
	 * @return A string in the format "<LOCATION_INFORMATION>", if location
	 *   information is provided. Otherwise, the words "Unknown location".
	 */
	public static String asLocation(Location pLoc) {
		if (pLoc == null) {
			return "Unknown location";
		} else {
			return asLocation(pLoc.getSystemId(), pLoc.getLineNumber(), pLoc.getColumnNumber());
		}
	}

	/**
	 * Returns a string, which includes the given message, and the given location information.
	 * @param pMsg The message string, which is being augmented with location information.
	 * @param pSystemId System id of the location information (if available), or null.
	 * @param pLineNumber Line number of the location information (if available), or -1.
	 * @param pColumnNumber Column number of the location information (if available), or -1
	 * @return A string in the format "At <LOCATION_INFORMATION>: <MESSAGE>", if location
	 *   information is provided. Otherwise, returns the message string, as it is.
	 */
	public static String asLocalizedMessage(String pMsg, String pSystemId, int pLineNumber, int pColumnNumber) {
		if (pSystemId == null  &&  pLineNumber == -1  &&  pColumnNumber == -1) {
			return pMsg;
		} else {
			final StringBuilder sb = new StringBuilder();
			String sep = "At ";
			if (pSystemId != null) {
				sb.append(sep);
				sb.append(pSystemId);
				sep = ", ";
			}
			if (pLineNumber != -1) {
				sb.append(sep);
				sb.append("line ");
				sb.append(pLineNumber);
				sep = ", ";
			}
			if (pColumnNumber != -1) {
				sb.append(sep);
				sb.append("column ");
				sb.append(pColumnNumber);
			}
			sb.append(": ");
			sb.append(pMsg);
			return sb.toString();
		}
	}

	/**
	 * Returns a string, describing the given location information.
	 * @param pSystemId System id of the location information (if available), or null.
	 * @param pLineNumber Line number of the location information (if available), or -1.
	 * @param pColumnNumber Column number of the location information (if available), or -1
	 * @return A string in the format "<LOCATION_INFORMATION>", if location
	 *   information is provided. Otherwise, returns the words "Unknown location".
	 */
	public static String asLocation(String pSystemId, int pLineNumber, int pColumnNumber) {
		if (pSystemId == null  &&  pLineNumber == -1  &&  pColumnNumber == -1) {
			return "Unknown location";
		} else {
			final StringBuilder sb = new StringBuilder();
			String sep = "";
			if (pSystemId != null) {
				sb.append(sep);
				sb.append(pSystemId);
				sep = ", ";
			}
			if (pLineNumber != -1) {
				sb.append(sep);
				sb.append("line ");
				sb.append(pLineNumber);
				sep = ", ";
			}
			if (pColumnNumber != -1) {
				sb.append(sep);
				sb.append("column ");
				sb.append(pColumnNumber);
			}
			return sb.toString();
		}
	}

	/** Returns a simple description of the given {@link XMLStreamReader}, which is mainly
	 * intended for debugging purposes. Most importantly, it contains the stream
	 * readers line, and column number, so that you get an idea, where it is currently
	 * positioned.
	 * @param pReader The stream reader to describe.
	 * @return The stream readers description.
	 */
	public static String asString(XMLStreamReader pReader) {
		final StringBuilder sb = new StringBuilder();
		sb.append(pReader.getClass().getSimpleName());
		sb.append('@');
		sb.append(Integer.toHexString(pReader.hashCode()));
		final Location loc = pReader.getLocation();
		if (loc != null) {
			sb.append(": ");
			sb.append(loc.getLineNumber());
                        sb.append(": ");
                        sb.append(loc.getColumnNumber());
		}
		return sb.toString();
	}

	/**
	 * Creates a new {@link XMLStreamException} with the given readers {@link Location}.
	 * @param pReader The reader, which is being queried for its location.
	 * @param pMsg The error message.
	 * @return An {@link XMLStreamException}, with location information.
	 */
	public static XMLStreamException error(XMLStreamReader pReader, String pMsg) {
		final Location loc = pReader.getLocation();
		if (loc == null) {
			return new XMLStreamException(pMsg);
		} else {
			return new XMLStreamException(asLocalizedMessage(loc, pMsg), loc);
		}
	}

	/** Asserts, that the Stax parser is currently located at an element with the default namespace,
	 * and the given local name.
	 * @param pReader The Stax parser being checked.
	 * @param pTagName The expected local name.
	 * @throws XMLStreamException The Stax parser is not located at a start element event,
	 * or the element name is not as expected, or the namespace URI is not the default namespace.
	 */
	public static void assertElement(XMLStreamReader pReader, String pTagName) throws XMLStreamException {
		assertStartElementState(pReader);
	    if (!isDefaultNamespace(pReader)) {
	    	final String uri = pReader.getNamespaceURI();
	    	throw error(pReader, "Expected default namespace, got " + uri);
	    }
	}

	/** Asserts, that the Stax parser is currently located at a start element event.
	 * @param pReader The Stax parser being checked.
	 * @throws XMLStreamException The Stax parser is not located at a start element event.
	 */
	public static void assertStartElementState(XMLStreamReader pReader) throws XMLStreamException {
		final int state = pReader.getEventType();
		if (XMLStreamReader.START_ELEMENT != state) {
			throw error(pReader, "Expected state START_ELEMENT, got " + state);
		}
	}

	/** Returns, whether the Stax parser is currently located at a start element, or an end
	 * element event with the default namespace.
	 * @param pReader The Stax parser being checked.
	 * @return True, if the current element has the default namespace URI.
	 * @throws XMLStreamException The Stax parser is not located at a start element, or an end
	 *   element event, or the elements namespace URI is not the default namespace.
	 */
	public static boolean isDefaultNamespace(XMLStreamReader pReader) throws XMLStreamException {
		final String uri = pReader.getNamespaceURI();
		if (uri == null) {
			return true;
		} else {
			return uri.length() == 0;
		}
	}

	/** Asserts, that the Stax parser is currently located at a start element, or an end
	 * element event with the default namespace.
	 * @param pReader The Stax parser being checked.
	 * @throws XMLStreamException The Stax parser is not located at a start element, or an end
	 *   element event, or the elements namespace URI is not the default namespace.
	 */
	public static void assertDefaultNamespace(XMLStreamReader pReader) throws XMLStreamException {
		if (!isDefaultNamespace(pReader)) {
			final String uri = pReader.getNamespaceURI();
			throw error(pReader, "Expected default namespace, got " + uri);
		}
	}

	/** Skips the current element, including it's contents, up to, and including, the
	 * corresponding end element event.
	 * @param pReader The Stax parser, which is to skip an element.
	 * @throws XMLStreamException Skipping the element failed.
	 */
	public static void skipElementRecursively(XMLStreamReader pReader) throws XMLStreamException {
		int level = 0;
		final String uri = pReader.getNamespaceURI();
		final String localName = pReader.getLocalName();
		while (pReader.hasNext()) {
			final int state = pReader.next();
			switch (state) {
			case XMLStreamReader.COMMENT:
			case XMLStreamReader.CHARACTERS:
			case XMLStreamReader.CDATA:
			case XMLStreamReader.SPACE:
				// Ignore this.
				break;
			case XMLStreamReader.START_ELEMENT:
				++level;
				break;
			case XMLStreamReader.END_ELEMENT:
				if (level-- == 0) {
					if (uri == null  ||  uri.length() == 0) {
						assertDefaultNamespace(pReader);
					} else if (!uri.equals(pReader.getNamespaceURI())) {
						throw error(pReader, "Expected namespace=" + uri + ", got " + pReader.getNamespaceURI());
					}
					if (!localName.equals(pReader.getLocalName())) {
						throw error(pReader, "Expected localName=" + localName + ", got " + pReader.getLocalName());
					}
					return;
				}
				break;
			default:
				throw error(pReader, "Unexpected state: " + state);
			}
		}
	}

	/**
	 * Reads, and returns the current elements text content.
	 * @param pReader
	 * @return The current elements text content.
	 * @throws XMLStreamException
	 */
	public static String getElementText(XMLStreamReader pReader) throws XMLStreamException {
		final String uri = Objects.notNull(pReader.getNamespaceURI(), "");
		final String localName = pReader.getLocalName();
		final StringBuilder sb = new StringBuilder();
		while (pReader.hasNext()) {
			final int state = pReader.next();
			switch(state) {
			case XMLStreamReader.CHARACTERS:
			case XMLStreamReader.CDATA:
			case XMLStreamReader.SPACE:
				sb.append(pReader.getText());
				break;
			case XMLStreamReader.COMMENT:
				break;
			case XMLStreamReader.END_ELEMENT:
				final String endUri = Objects.notNull(pReader.getNamespaceURI(), "");
				if (!uri.equals(endUri)  ||
					!localName.equals(pReader.getLocalName())) {
					throw error(pReader, "Expected /" + Sax.asQName(uri, localName)
					            + ", got " + Sax.asQName(endUri,
					            		                 pReader.getLocalName()));
				}
				return sb.toString();
			default:
				throw error(pReader, "Unexpected state: " + state);
			case XMLStreamReader.START_ELEMENT:
				throw error(pReader, "Unexpected start element: " + Sax.asQName(pReader.getNamespaceURI(), pReader.getLocalName()));
			}
		}
		throw error(pReader, "Unexpected end of document");
	}
}
