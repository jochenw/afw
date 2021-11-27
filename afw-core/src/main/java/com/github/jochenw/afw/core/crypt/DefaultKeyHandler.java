package com.github.jochenw.afw.core.crypt;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.core.util.Tupel;

/** Default implementation of {@link IKeyHandler}. Attempts to use the {@link JceKeyHandler},
 * which should work on Java 8. If that doesn't work, uses the {@link BcKeyHandler}.
 */
public class DefaultKeyHandler implements IKeyHandler {
	private static final IKeyHandler keyHandler;
	static {
		IKeyHandler kh;
		try {
			final Object x509CertInfo = Reflection.newObject("sun.security.x509.X509CertInfo");
			kh = new JceKeyHandler();
		} catch (Throwable t) {
			kh = new BcKeyHandler();
		}
		keyHandler = kh;
	}

	@Override
	public KeyPair createKeyPair() {
		return keyHandler.createKeyPair();
	}

	@Override
	public Certificate generateCertificate(String pDn, KeyPair pKeyPair, int pValidity) {
		return keyHandler.generateCertificate(pDn, pKeyPair, pValidity);
	}

	@Override
	public KeyStore createKeyStore(PrivateKey pPrivateKey, Certificate pCertificate, String pAlias, String pStoreType,
			String pStorePass, String pKeyPass) {
		return keyHandler.createKeyStore(pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
	}

	@Override
	public void createKeyStore(OutputStream pOut, PrivateKey pPrivateKey, Certificate pCertificate, String pAlias,
			String pStoreType, String pStorePass, String pKeyPass) {
		keyHandler.createKeyStore(pOut, pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
	}

	@Override
	public Tupel<PrivateKey, Certificate> readPrivateKey(InputStream pIn, String pAlias, String pStorePass,
			String pKeyPass) {
		return keyHandler.readPrivateKey(pIn, pAlias, pStorePass, pKeyPass);
	}

	@Override
	public byte[] encrypt(Key pKey, byte[] pBytes) {
		return keyHandler.encrypt(pKey, pBytes);
	}

	@Override
	public byte[] decrypt(Key pKey, byte[] pBytes) {
		return keyHandler.encrypt(pKey, pBytes);
	}
}
