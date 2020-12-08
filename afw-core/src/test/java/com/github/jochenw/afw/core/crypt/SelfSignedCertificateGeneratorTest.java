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


public class SelfSignedCertificateGeneratorTest {
	@Test
	public void testFindKeytool() {
		assertNotNull(new SelfSignedCertificateGenerator().findKeyTool());
	}

	@Test
	public void testCreateSelfSignedCertificate() throws Exception {
		final Path keyStorePath = Paths.get("target/unit-tests/SelfSignedCertificateGeneratorTest/keystore.jks");
		Files.createDirectories(keyStorePath.getParent());
		Files.deleteIfExists(keyStorePath);
		assertFalse(Files.isRegularFile(keyStorePath));
		final SelfSignedCertificateGenerator scgen = new SelfSignedCertificateGenerator();
		scgen.setAlias("selfsigned");
		scgen.setCountry("DE");
		scgen.setFileName(keyStorePath.toString());
		scgen.setKeyAlgorithm("RSA");
		scgen.setKeySize(4096);
		scgen.setLocation("The Net");
		scgen.setName("127.0.0.1");
		scgen.setOrganization("Organized? Me?");
		scgen.setOrgUnit("None at all");
		scgen.setStateOrProvince("Somewhere");
		scgen.setStorePassword("okayokay");
		scgen.setValidInDays(9999);
		scgen.createSelfSignedCertificate();
		scgen.setLogger(System.out::println);
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
