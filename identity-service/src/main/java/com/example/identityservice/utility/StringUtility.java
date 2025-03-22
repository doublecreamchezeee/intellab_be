package com.example.identityservice.utility;

import java.text.Normalizer;

public class StringUtility {
    public static String convertToAscii(String input) {
        // Normalize the string to decompose accented characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Remove diacritical marks
        String withoutDiacritics = normalized.replaceAll("\\p{M}", "");

        // Remove all special characters
        String asciiString = withoutDiacritics.replaceAll("[^\\p{ASCII}]", "");

        // Remove punctuation and special characters
        asciiString = asciiString.replaceAll("[\\p{Punct}]", "");

        return asciiString;
    }
}
