package com.github.jochenw.afw.core.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.Test;

/** Test case for the {@link WriterOutputStream}.
 */
public class WriterOutputStreamTest {
    private static final String TEST_STRING = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";
    private static final String LARGE_TEST_STRING;

    static {
        final StringBuilder buffer = new StringBuilder();
        for (int i=0; i<100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }

    private final Random random = new Random();

    /** Test for {@link OutputStreamWriter#flush()}.
     * @throws Exception The test failed.
     */
    @Test
    public void testFlush() throws Exception {
        final StringWriter writer = new StringWriter();
        try (final WriterOutputStream out = new WriterOutputStream(writer, "us-ascii", 1024, false)) {
            out.write("abc".getBytes(StandardCharsets.US_ASCII));
            assertEquals(0, writer.getBuffer().length());
            out.flush();
            assertEquals("abc", writer.toString());
        }
    }

    /** Test for a large test string with UTF-8, using a buffered write.
     * @throws Exception The test failed.
     */
    @Test
    public void testLargeUTF8WithBufferedWrite() throws Exception {
        testWithBufferedWrite(LARGE_TEST_STRING, "UTF-8");
    }

    /** Test for a large test string with UTF-8, using a single byte write.
     * @throws Exception The test failed.
     */
    @Test
    public void testLargeUTF8WithSingleByteWrite() throws Exception {
        testWithSingleByteWrite(LARGE_TEST_STRING, "UTF-8");
    }

    /** Test for a large test string with UTF-16BE, using a buffered write.
     * @throws Exception The test failed.
     */
    @Test
    public void testUTF16BEWithBufferedWrite() throws Exception {
        testWithBufferedWrite(TEST_STRING, "UTF-16BE");
    }

    /** Test for a large test string with UTF-16BE, using a single byte write.
     * @throws Exception The test failed.
     */
    @Test
    public void testUTF16BEWithSingleByteWrite() throws Exception {
        testWithSingleByteWrite(TEST_STRING, "UTF-16BE");
    }

    /** Test for a large test string with UTF-16LE, using a buffered write.
     * @throws Exception The test failed.
     */
    @Test
    public void testUTF16LEWithBufferedWrite() throws Exception {
        testWithBufferedWrite(TEST_STRING, "UTF-16LE");
    }

    /** Test for a large test string with UTF-16LE, using a single byte write.
     * @throws Exception The test failed.
     */
    @Test
    public void testUTF16LEWithSingleByteWrite() throws Exception {
        testWithSingleByteWrite(TEST_STRING, "UTF-16LE");
    }

    /** Test for a large test string with UTF-16, using a buffered write.
     * @throws Exception The test failed.
     */
    @Test
    public void testUTF16WithBufferedWrite() throws Exception {
    	testWithBufferedWrite(TEST_STRING, "UTF-16");
    }

    /** Test for a large test string with UTF-16, using a single byte write.
     * @throws Exception The test failed.
     */
    @Test
    public void testUTF16WithSingleByteWrite() throws Exception {
    	testWithSingleByteWrite(TEST_STRING, "UTF-16");
    }

    /** Test for a large test string with UTF-8, using a buffered write.
     * @throws Exception The test failed.
     */
    @Test
    public void testUTF8WithBufferedWrite() throws Exception {
        testWithBufferedWrite(TEST_STRING, "UTF-8");
    }

    /** Test for a large test string with UTF-8, using a single byte write.
     * @throws Exception The test failed.
     */
    @Test
    public void testUTF8WithSingleByteWrite() throws Exception {
        testWithSingleByteWrite(TEST_STRING, "UTF-8");
    }

    private void testWithBufferedWrite(final String testString, final String charsetName) throws IOException {
        final byte[] expected = testString.getBytes(charsetName);
        final StringWriter writer = new StringWriter();
        try (final WriterOutputStream out = new WriterOutputStream(writer, charsetName)) {
            int offset = 0;
            while (offset < expected.length) {
                final int length = Math.min(random.nextInt(128), expected.length - offset);
                out.write(expected, offset, length);
                offset += length;
            }
        }
        assertEquals(testString, writer.toString());
    }


    private void testWithSingleByteWrite(final String testString, final String charsetName) throws IOException {
        final byte[] bytes = testString.getBytes(charsetName);
        final StringWriter writer = new StringWriter();
        try (final WriterOutputStream out = new WriterOutputStream(writer, charsetName)) {
            for (final byte b : bytes) {
                out.write(b);
            }
        }
        assertEquals(testString, writer.toString());
    }

    /** Test for a short string, that is being written immediately.
     * @throws Exception The test failed.
     */
    @Test
    public void testWriteImmediately() throws Exception {
        final StringWriter writer = new StringWriter();
        try (final WriterOutputStream out = new WriterOutputStream(writer, "us-ascii", 1024, true)) {
            out.write("abc".getBytes(StandardCharsets.US_ASCII));
            assertEquals("abc", writer.toString());
        }
    }
}