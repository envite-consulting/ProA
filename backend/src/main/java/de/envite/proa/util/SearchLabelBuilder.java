package de.envite.proa.util;

import java.util.Arrays;

public class SearchLabelBuilder {
	public static String buildSearchLabel(String label) {
		if (label == null)
			return null;
		String cleanedLabel = label
				.toLowerCase()
				.replaceAll("[^a-zA-Z0-9\\s]", " ")
				.trim()
				.replaceAll("\\s+", " ");
		String[] words = cleanedLabel.split("\\s+");
		Arrays.sort(words);
		return String.join("", words);
	}
}
