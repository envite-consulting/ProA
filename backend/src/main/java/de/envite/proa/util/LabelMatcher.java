package de.envite.proa.util;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LabelMatcher {

	private static final double THRESHOLD = 0.8;

	public static boolean isSimilar(String str1, String str2) {
		if (str1 == null || str2 == null) {
			return false;
		}
		return levenshteinSimilarity(preprocess(str1), preprocess(str2)) >= THRESHOLD;
	}

	private static double levenshteinSimilarity(String str1, String str2) {
		LevenshteinDistance ld = new LevenshteinDistance();
		int distance = ld.apply(str1, str2);
		int maxLength = Math.max(str1.length(), str2.length());
		return 1.0 - ((double) distance / maxLength);
	}

	private static String preprocess(String str) {
		String processed = str.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
		Set<String> words = new HashSet<>(Arrays.asList(processed.split("\\s+")));
		return words.stream().sorted().collect(Collectors.joining(" "));
	}
}
