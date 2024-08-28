package de.envite.proa.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

@ApplicationScoped
public class KeyGenerationService {

	private static final Path PRIVATE_KEY_PATH = Paths.get("src/main/resources/privateKey.pem");
	private static final Path PUBLIC_KEY_PATH = Paths.get("src/main/resources/publicKey.pem");

	public void generateKeysIfNeeded(@Observes StartupEvent event) {
		try {
			if (Files.notExists(PRIVATE_KEY_PATH) || Files.notExists(PUBLIC_KEY_PATH)) {
				createDirectoriesIfNotExist();
				generateKeyPair();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error generating keys", e);
		}
	}

	private void generateKeyPair() throws NoSuchAlgorithmException, IOException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair keyPair = keyGen.generateKeyPair();

		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();

		saveKeyToFile(privateKey.getEncoded(), PRIVATE_KEY_PATH.toString());
		saveKeyToFile(publicKey.getEncoded(), PUBLIC_KEY_PATH.toString());
	}

	private void saveKeyToFile(byte[] key, String filePath) throws IOException {
		String keyBase64 = Base64.getEncoder().encodeToString(key);
		String keyType = filePath.contains("private") ? "PRIVATE" : "PUBLIC";
		String keyContent = "-----BEGIN " + keyType + " KEY-----\n" + keyBase64 + "\n-----END " + keyType + " KEY-----";

		try (FileOutputStream fos = new FileOutputStream(filePath)) {
			fos.write(keyContent.getBytes());
		}
	}

	private void createDirectoriesIfNotExist() throws IOException {
		Path keysDir = PRIVATE_KEY_PATH.getParent();

		if (!Files.exists(keysDir)) {
			Files.createDirectories(keysDir);
		}
	}
}