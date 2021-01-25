package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.junit.Test;

import com.github.jochenw.afw.core.crypt.DefaultKeyHandler;
import com.github.jochenw.afw.core.crypt.IKeyHandler;

public class KeysTest {
	@Test
	public void testEncryptDecryptUsingKeyHandler() throws Exception {
		final IKeyHandler kh = new DefaultKeyHandler();
		testWithoutKeyPassword("keystore0.jks", kh);
	}

	protected void testWithoutKeyPassword(String pFileName, IKeyHandler pHandler) throws IOException {
		final Path dir = Paths.get("target/unit-tests/KeysTest");
		final Path file = dir.resolve(pFileName);
		java.nio.file.Files.createDirectories(dir);
		java.nio.file.Files.deleteIfExists(file);
		final KeyPair kp = pHandler.createKeyPair();
		final Certificate cert = pHandler.generateCertificate("CN=Unknown", kp, 9999);
		final KeyStore ks0 = pHandler.createKeyStore(kp.getPrivate(), cert, "main", null, "654321", null);
		try (OutputStream os = Files.newOutputStream(file)) {
			ks0.store(os, "654321".toCharArray());
		} catch (Throwable e) {
			throw Exceptions.show(e);
		}
		final Tupel<PrivateKey,Certificate> ks1;
		try (InputStream in = Files.newInputStream(file)) {
			ks1 = pHandler.readPrivateKey(in, "main", "654321", null);
		} catch (Throwable e) {
			throw Exceptions.show(e);
		}
		final String password = "My Secret Password";
		final String encryptedPassword = pHandler.encryptToString(ks1.getAttribute1(), password);
		final String decryptedPassword = pHandler.decrypt(ks1.getAttribute2().getPublicKey(), encryptedPassword);
		assertEquals(password, decryptedPassword);
	}
}
