package edu.uob.parser;

/**
 * Utility class for string operations used throughout the application.
 */
public final class Normaliser {

    // private constructor to avoid instantiating
    private Normaliser() {}

    /**
     * Normalizes input strings by converting to lowercase,
     * replacing all punctuation marks except apostrophes (') and hyphens (-) with spaces,
     * compressing multiple spaces into one, and trimming.
     *
     * @param input The string to normalize
     * @return Normalized string
     */
    public static String normalizeString(String input) {
        if (input == null) {
            return "";
        }
        String normalized = input.toLowerCase();
        normalized = normalized.replaceAll("[\\p{Punct}&&[^'-]]+", " ");
        normalized = normalized.replaceAll("\\s+", " ");
        return normalized.trim();
    }
}
