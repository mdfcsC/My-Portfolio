package edu.uob.parser;

import java.util.regex.Pattern;

public class InputParser {
    Pattern entityNamePattern;
    Pattern playerNamePattern;

    public InputParser() {
        this.entityNamePattern = Pattern.compile("^[A-Za-z_-]+$");
        this.playerNamePattern = Pattern.compile("^[A-Za-z\\s'-]+$");
    }

    public boolean isValidEntityName(String input) {
        return this.entityNamePattern.matcher(input).matches();
    }

    public Pattern getEntityNamePattern() {
        return this.entityNamePattern;
    }

    /** Valid player names can consist of uppercase and lowercase letters, spaces, apostrophes and hyphens. */
    public boolean isValidPlayerName(String input) {
        return this.playerNamePattern.matcher(input).matches();
    }
}
