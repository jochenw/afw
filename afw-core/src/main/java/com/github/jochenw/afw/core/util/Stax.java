package com.github.jochenw.afw.core.util;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;

public class Stax {
	public static String asLocalizedMessage(Location pLoc, String pMsg) {
		if (pLoc == null) {
			return pMsg;
		} else {
			return asLocalizedMessage(pMsg, pLoc.getSystemId(), pLoc.getLineNumber(), pLoc.getColumnNumber());
		}
	}

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
			sb.append(loc.getLineNumber() + ": " + loc.getColumnNumber());
		}
		return sb.toString();
	}
}
