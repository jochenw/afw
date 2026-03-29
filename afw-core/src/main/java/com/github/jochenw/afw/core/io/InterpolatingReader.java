package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.github.jochenw.afw.core.props.Interpolator;
import com.github.jochenw.afw.core.util.Streams;


/** A filtering {@link Reader}, which reads the underlying data, while resolving
 * property references like "${some.property}" on the fly, returning the resulting
 * data.
 */
public class InterpolatingReader extends Reader {
	private final Interpolator interpolator;
	private final String text;
	private final StringReader sr;

	/** Creates a new instance, that reads data from the given {@link Reader
	 * parent reader}, using the given {@link Interpolator interpolator} for
	 * resolving property values.
	 * @param pInterpolator The {@link Interpolator interpolator} for
	 * resolving property values. For example, if the {@link InterpolatingReader}
	 * encounters a property reference "${some.property}", then the interpolator
	 * would be invoked with the property key "some.property".
	 * @param pParent The parent {@link Reader}, from which to read data.
	 */
	public InterpolatingReader(Interpolator pInterpolator, Reader pParent) {
		super();
		interpolator = pInterpolator;
		final String t = Streams.read(pParent);
		text = interpolator.interpolate(t);
		sr = new StringReader(text);
	}

	@Override
	public int read(char[] pBuffer, int pOffset, int pLength) throws IOException {
		return sr.read(pBuffer, pOffset, pLength);
	}

	@Override
	public int read(char[] pBuffer) throws IOException {
		return sr.read(pBuffer);
	}

	@Override
	public int read() throws IOException {
		return sr.read();
	}

	@Override
	public void close() throws IOException {
		sr.close();
	}

}
