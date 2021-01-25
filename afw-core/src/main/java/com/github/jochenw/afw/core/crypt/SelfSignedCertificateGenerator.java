package com.github.jochenw.afw.core.crypt;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Executor;
import com.github.jochenw.afw.core.util.Keys;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.core.util.Systems;

public class SelfSignedCertificateGenerator {
	private Consumer<String> logger;
	private @Inject IKeyHandler keyHandler;
	private String keyToolExe;
	private String keyAlgorithm, fileName, keyPassword, storePassword, alias, storeType;
	private int validInDays, keySize;
	private String name, orgUnit, organization, location, stateOrProvince, country;

	
	public void setLogger(Consumer<String> pLogger) {
		logger = pLogger;
	}

	public Consumer<String> getLogger() {
		return logger;
	}

	public void log(String pMessage) {
		if (logger != null) {
			logger.accept(pMessage);
		}
	}

	public String getKeyPassword() {
		return keyPassword;
	}

	public void setKeyPassword(String pKeyPassword) {
		keyPassword = pKeyPassword;
	}

	public void setKeyHandler(IKeyHandler pKeyHandler) {
		keyHandler = pKeyHandler;
	}

	public IKeyHandler getKeyHandler() {
		return Objects.notNull(keyHandler, Keys.getKeyHandler());
	}

	public void setStoreType(String pStoreType) {
		storeType = pStoreType;
	}

	public String getStoreType() {
		return Objects.notNull(storeType, "JKS");
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String pAlias) {
		alias = pAlias;
	}

	public String getKeyToolExe() {
		return keyToolExe;
	}

	public void setKeyToolExe(String pKeyToolExe) {
		keyToolExe = pKeyToolExe;
	}

	public String getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(String pKeyAlgorithm) {
		keyAlgorithm = pKeyAlgorithm;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String pFileName) {
		fileName = pFileName;
	}

	public String getStorePassword() {
		return storePassword;
	}

	public void setStorePassword(String pStorePassword) {
		storePassword = pStorePassword;
	}

	public int getValidInDays() {
		return validInDays;
	}

	public void setValidInDays(int pValidInDays) {
		validInDays = pValidInDays;
	}

	public int getKeySize() {
		return keySize;
	}

	public void setKeySize(int pKeySize) {
		keySize = pKeySize;
	}

	public String getName() {
		return name;
	}

	public void setName(String pName) {
		name = pName;
	}

	public String getOrgUnit() {
		return orgUnit;
	}

	public void setOrgUnit(String pOrgUnit) {
		orgUnit = pOrgUnit;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String pOrganization) {
		organization = pOrganization;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String pLocation) {
		location = pLocation;
	}

	public String getStateOrProvince() {
		return stateOrProvince;
	}

	public void setStateOrProvince(String pStateOrProvince) {
		stateOrProvince = pStateOrProvince;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String pCountry) {
		country = pCountry;
	}

	/**
	 * This method basically executes the following:
	 * $ keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass okayokay -validity 360 -keysize 4096
	 * What is your first and last name?
	 *   [Unknown]:  Jochen Wiedmann
	 * What is the name of your organizational unit?
	 *   [Unknown]:  https://github.com/jochenw/afw
	 * What is the name of your organization?
	 *   [Unknown]:  https://github.com/jochenw
	 * What is the name of your City or Locality?
	 *   [Unknown]:  The Net
	 * What is the name of your State or Province?
	 *   [Unknown]:  Baden Wuerttemberg
	 * What is the two-letter country code for this unit?
	 *   [Unknown]:  DE
	 * Is CN=Jochen Wiedmann, OU=https://github.com/jochenw/afw,
	 *  O=https://github.com/jochenw, L=The Net, ST=Baden Wuerttemberg,
	 *  C=DE correct?
	 *    [no]:  yes
	 */
	public void createSelfSignedCertificate() {
		final StringBuilder inputSb = new StringBuilder();
		final BiConsumer<String,String> appender = (sh,st) -> {
			if (st != null) {
				if (inputSb.length() > 0) {
					inputSb.append(", ");
				}
				inputSb.append(sh);
				inputSb.append('=');
				inputSb.append(st);
			}
		};
		appender.accept("CN", getName());
		appender.accept("OU", getOrgUnit());
		appender.accept("O", getOrganization());
		appender.accept("L", getLocation());
		appender.accept("S", getStateOrProvince());
		appender.accept("C", getCountry());
		final IKeyHandler kh = getKeyHandler();
		final KeyPair keyPair = kh.createKeyPair();
		final Certificate certificate = kh.generateCertificate(inputSb.toString(), keyPair, validInDays);
		final Path path = Paths.get(fileName);
		final Path dir = path.getParent();
		try {
			if (dir != null) {
				Files.createDirectories(dir);
			}
			try (OutputStream os = Files.newOutputStream(path)) {
				kh.createKeyStore(os, keyPair.getPrivate(), certificate, getAlias(), getStoreType(),
						          getStorePassword(), getKeyPassword());
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
			
		
	}

	public String findKeyTool() {
		final List<String> searchDirs = new ArrayList<>();
		if (keyToolExe != null) {
			final Path binaryPath = Paths.get(keyToolExe);
			final Path binaryDir = binaryPath.getParent();
			if (binaryDir != null) {
				searchDirs.add(binaryDir.toString());
			}
			if (Files.isRegularFile(binaryPath)  &&  Files.isExecutable(binaryPath)) {
				return binaryPath.toAbsolutePath().toString();
			}
		}
		final String binary = Systems.isWindows() ? "keytool.exe" : "keytool";
		final Path javaHomeDir = Paths.get("java.home");
		final Path javaHomeBinDir = javaHomeDir.resolve("bin");
		searchDirs.add(javaHomeBinDir.toString());
		if (Files.isDirectory(javaHomeDir)) {
			if (Files.isDirectory(javaHomeBinDir)) {
				final Path binaryPath = javaHomeBinDir.resolve(binary);
				if (Files.isRegularFile(binaryPath)  &&  Files.isExecutable(binaryPath)) {
					return binaryPath.toAbsolutePath().toString();
				}
			}
		}
		final String envPath = System.getenv("PATH");
		for (String dir : Strings.split(envPath, File.pathSeparator)) {
			searchDirs.add(dir);
			final Path path = Paths.get(dir);
			if (Files.isDirectory(path)) {
				final Path binaryPath = path.resolve(binary);
				if (Files.isRegularFile(binaryPath)  &&  Files.isExecutable(binaryPath)) {
					return binaryPath.toAbsolutePath().toString();
				}
			}
		}
		throw new IllegalStateException("Unable to locate " + binary + "in either of the following directories: "
				+ String.join(", ", searchDirs));
	}
}

