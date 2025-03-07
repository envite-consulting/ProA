package de.envite.proa.util;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResourceLoaderTest {

	private final ResourceLoader resourceLoader = new ResourceLoader();

	@Test
	void testLoadExistingResource() {
		InputStream inputStream = resourceLoader.loadResource("test-index.html");

		assertNotNull(inputStream, "Resource should be found and return a non-null InputStream");
	}

	@Test
	void testLoadNonExistingResource() {
		InputStream inputStream = resourceLoader.loadResource("non-existent-file.html");

		assertNull(inputStream, "ResourceLoader should return null when the file is not found");
	}
}
