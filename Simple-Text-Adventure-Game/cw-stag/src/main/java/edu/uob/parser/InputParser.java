package edu.uob.parser;

import edu.uob.GameState;
import edu.uob.action.GameAction;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputParser {
    private Pattern playerNamePattern;
    private String playerName;

    private LinkedHashSet<String> commandEntities;
    private String mainCommandVerb; // only for built-in command
    private LinkedList<String> possibleTriggers;

    private Set<String> gameEntitiesSet;
    private HashSet<GameAction> gameActionsSet;

    public InputParser(GameState gameState) {
        // Valid player names can consist of uppercase and lowercase letters, spaces, apostrophes and hyphens
        this.playerNamePattern = Pattern.compile("^[A-Za-z\\s'-]+$");
        this.playerName = null;

        this.commandEntities = null;
        this.mainCommandVerb = null;
        this.possibleTriggers = null;

        this.gameEntitiesSet = gameState.getAllEntities().keySet();
        this.gameActionsSet = gameState.getAllActions();
    }

    public void parseInput(String input) {
        // each call creates a new instance so it won't contain previous input words
        this.commandEntities = new LinkedHashSet<>();
        this.possibleTriggers = new LinkedList<>();

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

        String normalCommand = Normaliser.normalizeString(actualCommand); // tokenised normalised actual command without player name
        if (normalCommand.isEmpty()) {
            throw new RuntimeException("Empty normalCommand! Failed to normalise the actual command!");
        }

        this.findCommandEntities(normalCommand);

        // check if there is a built-in command
        String possibleBuiltInVerb = this.findBuiltInCommand(normalCommand);
        // check if there is a custom action trigger
        LinkedList<String> possibleActionTriggers = this.findActionTriggers(normalCommand);

        if (possibleBuiltInVerb != null && !possibleActionTriggers.isEmpty()) {
            throw new RuntimeException("Multiple commands! You can only issue one command at a time!");
        }
        if (possibleBuiltInVerb == null && possibleActionTriggers.isEmpty()) {
            throw new RuntimeException("No valid command found! What do you want to do?");
        }

        this.mainCommandVerb = possibleBuiltInVerb;
        this.possibleTriggers = possibleActionTriggers;

//        System.out.println("++++++++++");
//        System.out.printf("Player name: %s\nCommand verb: %s\nPossible Triggers:%s\nCommand entity words: %s\n", this.playerName, this.mainCommandVerb, this.possibleTriggers, this.commandEntities);
//        System.out.println("==========");
    }

    private void findCommandEntities(String normalCommand) {
        Scanner scanner = new Scanner(normalCommand);
        while (scanner.hasNext()) {
            String nextWord = scanner.next().trim();
            if (this.gameEntitiesSet.contains(nextWord)) {
                this.commandEntities.add(nextWord);
            }
        }
        scanner.close();

        this.checkExtraneousEntities();
    }

    /** case that player's input contains more than one entity
     * <br>valid only for multiple subjects in the same custom action
     * <br>built-in commands never can have more than one entity
     */
    private void checkExtraneousEntities() {
        if (this.commandEntities.size() > 1) {
            boolean extraneous = true;
            for (GameAction gameAction : this.gameActionsSet) {
                boolean sameActionSubjects = true;
                for (String commandEntity : this.commandEntities) {
                    if (!gameAction.getSubjects().contains(commandEntity)) {
                        sameActionSubjects = false;
                        break;
                    }
                }
                if (sameActionSubjects) {
                    extraneous = false;
                    break;
                }
            }
            if (extraneous) {
                throw new RuntimeException("Multiple extraneous entities! You can only issue one command at a time!");
            }
        }
    }

    private String findBuiltInCommand(String normalCommand) {
        int verbCounter = 0;
        String validBuiltInCommand = null;
        HashSet<String> builtInCommands = new HashSet<>(Set.of("inventory", "inv", "get", "drop", "goto", "look", "health"));

        for (String builtInCommand : builtInCommands) {
            Matcher matcher = this.compileMatchPattern(builtInCommand).matcher(normalCommand);

            if (matcher.find()) {
                validBuiltInCommand = builtInCommand;
                verbCounter++;
            }
        }
        if (verbCounter > 1) {
            throw new RuntimeException("Multiple built-in commands! You can only issue one command at a time!");
        }
        if (verbCounter == 0) {
            return null;
        }
        return validBuiltInCommand;
    }

    private LinkedList<String> findActionTriggers(String normalCommand) {
        LinkedList<String> verbs = new LinkedList<>();

        // check if input contains custom action trigger
        for (GameAction gameAction : this.gameActionsSet) {
            for (String trigger : gameAction.getTriggers()) {
                Matcher triggerMatcher = this.compileMatchPattern(trigger).matcher(normalCommand);

                // find matching substring
                if (triggerMatcher.find()) {
                    verbs.add(trigger);
                    // if found valid custom action trigger in this GameAction, mark and skip checking the rest of triggers in this GameAction (if there are)
                    break;
                }
            }
        }
        return verbs;
    }

    private Pattern compileMatchPattern (String checkingWord) {
        StringBuilder verbRegex = new StringBuilder();

        // Negative lookbehind: Ensures the trigger is not preceded by a letter, hyphen, or apostrophe
        verbRegex.append("(?<![A-Za-z'-])")
                // Quote the trigger to treat any special characters as literals
                .append(Pattern.quote(checkingWord))
                // Negative lookahead: Ensures the trigger is not followed by a letter, hyphen, or apostrophe
                .append("(?![A-Za-z'-])");

        return Pattern.compile(verbRegex.toString());
    }

    public String getPlayerName() {
        return this.playerName;
    }

    /** entities within entities.dot, but only this InputParser parsed ones */
    public LinkedHashSet<String> getCommandEntities() {
        return this.commandEntities;
    }

    /** only could be built-in command */
    public String getMainCommandVerb() {
        return this.mainCommandVerb;
    }

    public LinkedList<String> getPossibleTriggers() {
        return this.possibleTriggers;
    }
}
