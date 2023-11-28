package com.github.jochenw.afw.core.crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.Entry.Attribute;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Set;

import org.junit.Test;


/** A test suite for the {@link SelfSignedCertificateGenerator}.
 */
public class SelfSignedCertificateGeneratorTest {
	/** Test case for {@link SelfSignedCertificateGenerator#createSelfSignedCertificate}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testCreateSelfSignedCertificateWithDefaultKeyHandler() throws Exception {
		final SelfSignedCertificateGenerator scgen = new SelfSignedCertificateGenerator();
		validate(scgen);
	}

	/** Test case for {@link SelfSignedCertificateGenerator#createSelfSignedCertificate}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testCreateSelfSignedCertificateWithBcKeyHandler() throws Exception {
		final SelfSignedCertificateGenerator scgen = new SelfSignedCertificateGenerator();
		scgen.setKeyHandler(new BcKeyHandler());
		validate(scgen);
	}

	/** Runs a test for the given {@link SelfSignedCertificateGenerator},
	 * @param pGenerator The generator, that is being tested.
	 * @throws Exception The test failed.
	 */
	protected void validate(SelfSignedCertificateGenerator pGenerator) throws Exception {
		final Path keyStorePath = Paths.get("target/unit-tests/SelfSignedCertificateGeneratorTest/keystore.jks");
		Files.createDirectories(keyStorePath.getParent());
		Files.deleteIfExists(keyStorePath);
		assertFalse(Files.isRegularFile(keyStorePath));
		pGenerator.setAlias("selfsigned");
		pGenerator.setCountry("DE");
		pGenerator.setFileName(keyStorePath.toString());
		pGenerator.setLocation("The Net");
		pGenerator.setName("127.0.0.1");
		pGenerator.setOrganization("Organized? Me?");
		pGenerator.setOrgUnit("None at all");
		pGenerator.setStateOrProvince("Somewhere");
		pGenerator.setStorePassword("okayokay");
		pGenerator.setValidInDays(9999);
		pGenerator.createSelfSignedCertificate();
		pGenerator.setLogger(System.out::println);
		assertTrue(Files.isRegularFile(keyStorePath));
		try (InputStream in = Files.newInputStream(keyStorePath)) {
			final KeyStore keyStore = KeyStore.getInstance("jks");
			keyStore.load(in, "okayokay".toCharArray());
			final Entry entry = keyStore.getEntry("selfsigned", new PasswordProtection("okayokay".toCharArray()));
			assertNotNull(entry);
			assertTrue(entry instanceof PrivateKeyEntry);
			final PrivateKeyEntry pke = (PrivateKeyEntry) entry;
			final PrivateKey privKey = pke.getPrivateKey();
			assertNotNull(privKey);
			final Set<Attribute> attrs = entry.getAttributes();
			assertNotNull(attrs);
			final Certificate cert = keyStore.getCertificate("selfsigned");
			assertNotNull(cert);
			assertEquals("X.509", cert.getType());
			final PublicKey pubKey = cert.getPublicKey();
			assertEquals("RSA", pubKey.getAlgorithm());
			assertEquals("X.509", pubKey.getFormat());
		}
	}
}
