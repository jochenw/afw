package com.github.jochenw.afw.core.crypt;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.crypto.Cipher;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Tupel;

/** Implementation of IKeyHandler, based on the BounceCastle JCE provider.
 */
public class BcKeyHandler implements IKeyHandler {
	@Override
	public @NonNull KeyPair createKeyPair() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(4096, SecureRandom.getInstanceStrong());
			return Objects.requireNonNull(keyPairGenerator.generateKeyPair());
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public @NonNull Certificate generateCertificate(@NonNull String pDn,
			                                        @NonNull KeyPair pKeyPair,
			                                        int pValidity) {
		try {
			Provider bcProvider = new BouncyCastleProvider();
			long now = System.currentTimeMillis();
			Date startDate = new Date(now);
			X500Name dnName = new X500NameBuilder(X500Name.getDefaultStyle()).addRDN(BCStyle.CN, pDn).build();
			BigInteger serialNumber = new BigInteger(String.valueOf(now));
			LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault());
			final LocalDateTime ldtEnd = ldt.plusDays(pValidity);
			final Instant instantEnd = ldtEnd.toInstant(OffsetDateTime.now().getOffset());
			Date endDate = Date.from(instantEnd);
			final String sigAlgorithm = "SHA256WithRSA";
			ContentSigner contentSigner;
			contentSigner = new JcaContentSignerBuilder(sigAlgorithm).build(pKeyPair.getPrivate());
			final JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, serialNumber, startDate, endDate, dnName, pKeyPair.getPublic());
			BasicConstraints basicConstraints = new BasicConstraints(true);
			certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints);
			JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(bcProvider);
			return Objects.requireNonNull(converter.getCertificate(certBuilder.build(contentSigner)));
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public @NonNull KeyStore createKeyStore(@NonNull PrivateKey pPrivateKey,
			                                @NonNull Certificate pCertificate,
			                                @NonNull String pAlias,
			                                @Nullable String pStoreType,
			                                @NonNull String pStorePass,
			                                @Nullable String pKeyPass) {
		try {
			KeyStore keyStore = KeyStore.getInstance(Objects.notNull(pStoreType, "JKS"));
			keyStore.load(null, pStorePass.toCharArray());
			final String pwd = Objects.notNull(pKeyPass, pStorePass);
			keyStore.setKeyEntry(pAlias, pPrivateKey, pwd.toCharArray(), new Certificate[] {pCertificate});
			return keyStore;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public void createKeyStore(@NonNull OutputStream pOut,
			                   @NonNull PrivateKey pPrivateKey,
			                   @NonNull Certificate pCertificate,
			                   @NonNull String pAlias,
			                   @Nullable String pStoreType,
			                   @NonNull String pStorePass,
			                   @Nullable String pKeyPass) {
		try {
			final KeyStore keyStore = createKeyStore(pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
			keyStore.store(pOut, pStorePass.toCharArray());
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public Tupel<PrivateKey, Certificate> readPrivateKey(@NonNull InputStream pIn,
			                                             @NonNull String pAlias,
			                                             @NonNull String pStorePass,
			                                             @Nullable String pKeyPass) {
		try {
			final KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(pIn, pStorePass.toCharArray());
			final String pwd = Objects.notNull(pKeyPass, pStorePass);
			final PrivateKey pk = (PrivateKey) keyStore.getKey(pAlias, pwd.toCharArray());
			final Certificate cert = (Certificate) keyStore.getCertificate(pAlias);
			return new Tupel<PrivateKey,Certificate>(pk, cert);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public byte[] encrypt(Key pKey, byte[] pBytes) {
		try {
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, pKey);
			cipher.update(pBytes);
			return cipher.doFinal();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public byte[] decrypt(Key pKey, byte[] pBytes) {
		try {
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			cipher.init(Cipher.DECRYPT_MODE, pKey);
			cipher.update(pBytes);
			return cipher.doFinal();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

}
