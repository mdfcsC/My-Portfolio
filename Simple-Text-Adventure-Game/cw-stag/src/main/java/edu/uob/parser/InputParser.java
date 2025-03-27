package edu.uob.parser;

import edu.uob.action.GameAction;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputParser {
    private String input;
    private Pattern playerNamePattern;
    private String playerName;
    private String normalCommand;
    private LinkedHashSet<String> normalCommandWords;
    private String mainCommandVerb;

    private HashSet<String> allTriggers;

    public InputParser(ActionParser actionParser) {
        this.input = null;
        this.playerNamePattern = Pattern.compile("^[A-Za-z\\s'-]+$");
        this.playerName = null;
        this.normalCommand = null;
        this.normalCommandWords = new LinkedHashSet<>();
        this.mainCommandVerb = null;
        this.allTriggers = actionParser.getAllTriggers();
    }

    public void parseInput(String input) {
        this.input = input;

        // divide player name and actual command statement
        int colonIndex = input.indexOf(":"); // Returns: the index of the first occurrence of the specified substring, or -1 if there is no such occurrence.
        if (colonIndex == -1) {
            throw new RuntimeException("Invalid command: no colon to split player name and command statement! ");
        }
        this.playerName = input.substring(0, colonIndex).trim().toLowerCase();
        if (!this.playerNamePattern.matcher(this.playerName).matches()) {
            throw new RuntimeException("Valid player name can only consist of uppercase and lowercase letters, spaces, apostrophes and hyphens!");
        }
        String actualCommand = input.substring(colonIndex + 1).trim().toLowerCase();

        /* normalise the actual command statement:
            replace all punctuation marks except apostrophes (') with spaces
            compress multiple consecutive spaces into a single space
            remove opening and closing spaces
         */
        this.normalCommand = actualCommand.replaceAll("\\p{Punct}&&[^']+", " ").replaceAll("\\s+", " ").trim();
        if (this.normalCommand.isEmpty()) {
            throw new RuntimeException("Empty normalCommand! Failed to normalise the actual command!");
        }

        this.mainCommandVerb = findActionTrigger(this.normalCommand, this.allTriggers);
        // if found valid action trigger, then remove it from the normalised actual command
        String normalCommandWithoutVerb = this.normalCommand;
        if (this.mainCommandVerb != null) {
            StringBuilder regex = new StringBuilder();
            regex.append("\\b").append(Pattern.quote(this.mainCommandVerb)).append("\\b");
            Pattern verbPattern = Pattern.compile(regex.toString());
            normalCommandWithoutVerb = verbPattern.matcher(this.normalCommand).replaceAll("");
        }

        Scanner scanner = new Scanner(normalCommandWithoutVerb);
        while (scanner.hasNext()) {
            String nextWord = scanner.next().trim();

            // ensure at most one built-in command or action trigger
            if (this.mainCommandVerb != null && isBuiltInCommand(nextWord)) {
                throw new RuntimeException("Multiple commands! You can only issue one command at a time!");
            }

            if (isBuiltInCommand(nextWord)) {
                this.mainCommandVerb = nextWord;
            } else {
                // only add non-built-in command word
                this.normalCommandWords.add(nextWord);
            }
        }
        scanner.close();

        if (this.mainCommandVerb == null) {
            throw new RuntimeException("No valid command found! You cannot do that!");
        }
    }

    private String findActionTrigger(String normalCommand, HashSet<String> allTriggers) {
        String mainCommandVerb = null;
        int verbCounter = 0;

        // check if input contains custom action trigger
        for (String trigger : allTriggers) {
            StringBuilder regex = new StringBuilder();
            regex.append("\\b").append(Pattern.quote(trigger)).append("\\b");
            Pattern triggerPattern = Pattern.compile(regex.toString());
            Matcher triggerMatcher = triggerPattern.matcher(normalCommand);
            if (triggerMatcher.find()) {
                mainCommandVerb = triggerMatcher.group();
                verbCounter++;
            }
        }

        // ensure at most one custom action trigger
        if (verbCounter > 1) {
            throw new RuntimeException("Multiple commands! You can only issue one command at a time!");
        }
        return mainCommandVerb;
    }

    private boolean isBuiltInCommand(String word) {
        return word.equals("inventory") || word.equals("inv") ||
                word.equals("get") ||
                word.equals("drop") ||
                word.equals("goto") ||
                word.equals("look");
    }

    public String getPlayerName() {
        return this.playerName;
    }

    /** tokenised normalised actual command words without player name, valid built-in commands and action triggers */
    public LinkedHashSet<String> getNormalCommandWords() {
        return this.normalCommandWords;
    }

    public String getMainCommandVerb() {
        return this.mainCommandVerb;
    }
}
