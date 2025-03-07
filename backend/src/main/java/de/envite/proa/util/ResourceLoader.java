package de.envite.proa.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;

@ApplicationScoped
public class ResourceLoader {
	public InputStream loadResource(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}
}