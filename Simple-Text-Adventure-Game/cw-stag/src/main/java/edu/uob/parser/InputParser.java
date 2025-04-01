package edu.uob.parser;

import edu.uob.GameState;
import edu.uob.action.GameAction;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputParser {
    private Pattern playerNamePattern;
    private String playerName;

    private String normalCommand; // tokenised normalised actual command without player name
    private LinkedHashSet<String> commandEntities;
    private String mainCommandVerb;

    private Set<String> gameEntitiesSet;
    private HashSet<GameAction> gameActionsSet;

    public InputParser(GameState gameState) {
        this.playerNamePattern = Pattern.compile("^[A-Za-z\\s'-]+$");
        this.playerName = null;

        this.normalCommand = null;
        this.commandEntities = null;
        this.mainCommandVerb = null;

        this.gameEntitiesSet = gameState.getAllEntities().keySet();
        this.gameActionsSet = gameState.getAllActions();
    }

    public void parseInput(String input) {
        // each call creates a new instance so it won't contain previous input words
        this.commandEntities = new LinkedHashSet<>();

        // divide player name and actual command statement
        int colonIndex = input.indexOf(":"); // Returns: the index of the first occurrence of the specified substring, or -1 if there is no such occurrence.
        if (colonIndex == -1) {
            throw new RuntimeException("Invalid command: no colon to split player name and command statement!");
        }
        this.playerName = input.substring(0, colonIndex).trim();
        if (!this.playerNamePattern.matcher(this.playerName).matches()) {
            throw new RuntimeException("Valid player name can only consist of uppercase and lowercase letters, spaces, apostrophes and hyphens!");
        }

        int actualCommandStart = colonIndex + 1;
        if (actualCommandStart >= input.length()) {
            throw new RuntimeException("Invalid command: no actual command!");
        }
        String actualCommand = input.substring(actualCommandStart).trim().toLowerCase();

        /* normalise the actual command statement:
            replace all punctuation marks except apostrophes (') with spaces
            compress multiple consecutive spaces into a single space
            remove opening and closing spaces
         */
        this.normalCommand = actualCommand.replaceAll("[\\p{Punct}&&[^']]+", " ").replaceAll("\\s+", " ").trim();
        if (this.normalCommand.isEmpty()) {
            throw new RuntimeException("Empty normalCommand! Failed to normalise the actual command!");
        }

        // search if normalised actual command contains custom action trigger, null if not
        this.mainCommandVerb = this.findActionTrigger();

        Scanner scanner = new Scanner(this.normalCommand);
        while (scanner.hasNext()) {
            String nextWord = scanner.next().trim();

            // ensure at most one built-in command or action trigger
            if (this.mainCommandVerb != null && this.isBuiltInCommand(nextWord)) {
                throw new RuntimeException("Multiple commands! You can only issue one command at a time!");
            }
            if (this.isBuiltInCommand(nextWord)) {
                this.mainCommandVerb = nextWord;
            }

            if (this.gameEntitiesSet.contains(nextWord)) {
                this.commandEntities.add(nextWord);
            }
        }
        scanner.close();

        if (this.mainCommandVerb == null) {
            throw new RuntimeException("No valid command found! You cannot do that!");
        }
//
//        System.out.println("++++++++++");
//        System.out.println("Player name: " + this.playerName + "\nCommand verb: " + this.mainCommandVerb + "\nCommand entity words: " + this.commandEntities);
//        System.out.println("==========");
    }

    private String findActionTrigger() {
        String mainCommandVerb = null;
        int verbCounter = 0;

        // check if input contains custom action trigger
        for (GameAction gameAction : this.gameActionsSet) {
            for (String trigger : gameAction.getTriggers()) {
                StringBuilder regex = new StringBuilder();
                regex.append("\\b").append(Pattern.quote(trigger)).append("\\b");
                Pattern triggerPattern = Pattern.compile(regex.toString());
                Matcher triggerMatcher = triggerPattern.matcher(this.normalCommand);
                if (triggerMatcher.find()) {
                    mainCommandVerb = triggerMatcher.group();
                    verbCounter++;
                    // if found valid custom action trigger in this GameAction, mark and skip checking the rest of triggers in this GameAction (if there are)
                    break;
                }
            }
        }

        // ensure at most one custom action trigger
        if (verbCounter > 1) {
            throw new RuntimeException("Multiple custom action commands! You can only issue one command at a time!");
        }
        return mainCommandVerb;
    }

    private boolean isBuiltInCommand(String word) {
        return word.equals("inventory") || word.equals("inv") ||
                word.equals("get") ||
                word.equals("drop") ||
                word.equals("goto") ||
                word.equals("look") ||
                word.equals("health");
    }

    public String getPlayerName() {
        return this.playerName;
    }

    /** entities within entities.dot */
    public LinkedHashSet<String> getCommandEntities() {
        return this.commandEntities;
    }

    public String getMainCommandVerb() {
        return this.mainCommandVerb;
    }
}
