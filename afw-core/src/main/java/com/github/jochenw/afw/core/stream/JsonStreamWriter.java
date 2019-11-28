package com.github.jochenw.afw.core.stream;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import com.github.jochenw.afw.core.stream.StreamController.MetaData;
import com.github.jochenw.afw.core.util.Streams;


/** Implementation of {@link StreamWriter}, which creates Json.
 */
public class JsonStreamWriter extends AbstractStreamWriter {
	@Override
	public void write(OutputStream pOut, Object pStreamable) throws IOException {
		OutputStream os = Streams.uncloseableStream(pOut);
		try (BufferedOutputStream bos = new BufferedOutputStream(os);
			 JsonGenerator jgen = Json.createGenerator(bos)) {
			write(jgen, pStreamable, null);
		}
	}

	@Override
	public void write(Writer pOut, Object pStreamable) throws IOException {
		Writer w = Streams.uncloseableWriter(pOut);
		try (BufferedWriter bw = new BufferedWriter(w);
			 JsonGenerator jgen = Json.createGenerator(bw)) {
			write(jgen, pStreamable, null);
		}
	}

	/** <p>Json specific method for writing. This is internally used by
	 * {@link #write(OutputStream, Object)}, and {@link #write(Writer, Object)}.</p>
	 * @param pGen A Json generator, to which Json events will be fired.
	 * @param pStreamable The object being serialized.
	 * @param pName The method will generate a Json object. If this parameter is
	 *   null, then the generated Json object will be anonymous. Otherwise, it will
	 *   have this name.
	 * @throws IOException An I/O error occurred.
	 */
	public void write(JsonGenerator pGen, Object pStreamable, String pName) throws IOException {
		final MetaData metaData = getMetaData(pStreamable.getClass());
		if (pName == null) {
			pGen.writeStartObject();
		} else {
			pGen.writeStartObject(pName);
		}
		metaData.forEach((s,f) -> {
			final boolean atomic = isAtomic(f);
			final Object value = getValue(f, pStreamable);
			if (value != null) {
				if (atomic) {
					final Class<?> type = f.getType();
					if (type == Boolean.class  ||  type == Boolean.TYPE) {
						pGen.write(s, ((Boolean) value).booleanValue());
					} else if (type == Long.class  ||  type == Long.TYPE) {
						pGen.write(s, ((Long) value).longValue());
					} else if (type == Integer.class  ||  type == Integer.TYPE) {
						pGen.write(s, ((Integer) value).intValue());
					} else if (type == Short.class  ||  type == Short.TYPE) {
						pGen.write(s, ((Short) value).intValue());
					} else if (type == Byte.class  ||  type == Byte.TYPE) {
						pGen.write(s, ((Byte) value).intValue());
					} else if (type == Double.class  ||  type == Double.TYPE) {
						pGen.write(s, ((Double) value).doubleValue());
					} else if (type == Float.class  ||  type == Float.TYPE) {
						pGen.write(s, ((Double) value).doubleValue());
					} else if (type == BigDecimal.class) {
						pGen.write(s, (BigDecimal) value);
					} else if (type == BigInteger.class) {
						final BigInteger bi = (BigInteger) value;
						final BigDecimal bd = new BigDecimal(bi);
						pGen.write(s, bd);
					} else if (type == Character.class  ||  type == Character.TYPE) {
						final Character c = (Character) value;
						final char[] cArray = new char[] {c.charValue()};
						pGen.write(s, new String(cArray));
					} else {
						pGen.write(s, value.toString());
					}
				} else {
					write(pGen, value, s);
				}
			}
		});
		pGen.writeEnd();
	}
}
