package com.github.jochenw.afw.core.crypt;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.util.Executor;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.core.util.Systems;

public class SelfSignedCertificateGenerator {
	private Consumer<String> logger;
	private String keyToolExe;
	private String keyAlgorithm, fileName, storePassword, alias;
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
		final String keytool = findKeyTool();
		final List<String> cmd = new ArrayList<>();
		cmd.add(keytool);
		cmd.add("-genkey");
		cmd.add("-keyalg");
		cmd.add(Objects.notNull(getKeyAlgorithm(), "RSA"));
		if (getAlias() == null) {
			throw new IllegalStateException("No alias given");
		} else {
			cmd.add("-alias");
			cmd.add(getAlias());
		}
		if (getFileName() == null) {
			throw new IllegalStateException("No fileName given");
		} else {
			cmd.add("-keystore");
			cmd.add(getFileName());
		}

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
		cmd.add("-dname");
		cmd.add(inputSb.toString());
		if (getStorePassword() != null) {
			cmd.add("-storepass");
			cmd.add(getStorePassword());
		}
		
		final String[] cmdArray = cmd.toArray(new String[cmd.size()]);
		log("Executing command: " + String.join(" ", cmdArray));
		final Executor executor = new Executor();
		executor.setInput((in) -> in.write(inputSb.toString().getBytes()));
		final Consumer<InputStream> outConsumer = (out) -> Streams.copy(out, System.out);
		final Consumer<InputStream> errConsumer = (err) -> Streams.copy(err, System.err);
		executor.run(null, cmdArray, null, outConsumer, errConsumer, null);
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

