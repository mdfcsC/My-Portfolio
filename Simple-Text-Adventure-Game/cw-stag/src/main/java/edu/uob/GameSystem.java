package edu.uob;

import edu.uob.action.GameAction;
import edu.uob.entity.GameEntity;
import edu.uob.entity.Location;
import edu.uob.entity.Player;
import edu.uob.parser.ActionParser;
import edu.uob.parser.EntityParser;
import edu.uob.parser.InputParser;

import java.util.*;
import java.util.regex.Pattern;

public class GameSystem {
    private HashMap<String, Location> locationsHashMap;
    private Location startLocation;
    private Location storeroom;

    private HashMap<String, GameEntity> entitiesHashMap;

    private HashMap<String, Player> playersHashMap;
    private int maxHealth;

    private HashSet<GameAction> gameActions;

    private InputParser inputParser;

    public GameSystem(EntityParser entityParser, ActionParser actionParser) {
        this.locationsHashMap = entityParser.getLocationMap();
        this.startLocation = entityParser.getStartLocation();
        this.storeroom = entityParser.getStoreroom();
        this.entitiesHashMap = entityParser.getEntityMap();
        this.playersHashMap = new HashMap<>();
        this.maxHealth = 3;
        this.gameActions = actionParser.getGameActions();
        this.inputParser = new InputParser(actionParser);
    }

    public String handleInput(String input) {
        this.inputParser.parseInput(input);
        String playerName = this.inputParser.getPlayerName();
        LinkedHashSet<String> normalCommandWords = this.inputParser.getNormalCommandWords();
        String mainCommandVerb = this.inputParser.getMainCommandVerb();

        // check if the player name exists
        if (!playersHashMap.containsKey(playerName)) {
            Player newPlayer = new Player(playerName, this.maxHealth, this.startLocation);
            this.playersHashMap.put(playerName, newPlayer);
        }

        Player currentPlayer = this.playersHashMap.get(playerName);
        Location currentLocation = currentPlayer.getCurrentLocation();

        String result = switch (mainCommandVerb) {
            case "inventory", "inv" -> executeInventoryCommand(normalCommandWords, currentPlayer);
            case "get" -> executeGetCommand(normalCommandWords, currentPlayer, currentLocation);
            case "drop" -> executeDropCommand(normalCommandWords, currentPlayer, currentLocation);
            case "goto" -> executeGotoCommand(normalCommandWords, currentPlayer);
            case "look" -> executeLookCommand(normalCommandWords, currentLocation);
            default -> executeCustomAction(normalCommandWords, currentPlayer, currentLocation, mainCommandVerb);
        };

        // process normalised actual command, check if it is a built-in command or a custom action
        if (result == null) {
            throw new RuntimeException(String.format("Unknown command: %s", normalCommandWords.toString()));
        }
        return result;
    }

    private String executeInventoryCommand(LinkedHashSet<String> normalCommandWords, Player currentPlayer) {
        Set<String> inventorySet = currentPlayer.getInventory().keySet();
        if (inventorySet.isEmpty()) {
            return "You have nothing.";
        }

        StringBuilder invBuilder = new StringBuilder();

        invBuilder.append("You are carrying: ");
        for (String key : inventorySet) {
            invBuilder.append(key);
            invBuilder.append(", ");
        }
        // delete the last comma and blank
        invBuilder.delete(invBuilder.length() - 2, invBuilder.length());
        return invBuilder.toString();
    }

    private String executeGetCommand(LinkedHashSet<String> normalCommandWords, Player currentPlayer, Location currentLocation) {
        if (normalCommandWords.isEmpty()) {
            return "What do you want to get?";
        }

        String objectToGet = null;

        for (String word : normalCommandWords) {
            if (objectToGet == null && currentPlayer.getInventory().containsKey(word)) {
                return "You already had that.";
            }

            if (objectToGet != null && this.entitiesHashMap.containsKey(word)) {
                return "You can only get artefacts.";
            }

            if (currentLocation.getArtefacts().containsKey(word)) {
                if (objectToGet != null) {
                    return "Which one do you want to get?";
                }

                objectToGet = word;
            }
        }

        try {
            currentPlayer.pushInventory(currentLocation.popArtefact(objectToGet));
            return String.format("Taken: %s", objectToGet);
        } catch (Exception e) {
            return "You cannot get that thing.";
        }
    }

    private String executeDropCommand(LinkedHashSet<String> normalCommandWords, Player currentPlayer, Location currentLocation) {
        if (normalCommandWords.isEmpty()) {
            return "What do you want to drop?";
        }

        String objectToDrop = null;
        for (String word : normalCommandWords) {
            if (objectToDrop != null && this.entitiesHashMap.containsKey(word)) {
                return "You can only drop artefacts.";
            }

            if (currentPlayer.getInventory().containsKey(word)) {
                if (objectToDrop != null) {
                    return "Which one do you want to drop?";
                }
                objectToDrop = word;
            }
        }

        try {
            currentLocation.pushArtefact(currentPlayer.popInventory(objectToDrop));
            return String.format("Dropped: %s", objectToDrop);
        } catch (Exception e) {
            return "You cannot drop that thing.";
        }
    }

    private String executeGotoCommand(LinkedHashSet<String> normalCommandWords, Player currentPlayer) {
        if (normalCommandWords.isEmpty()) {
            return "Where do you want to go?";
        }

        Location toLocation = null;

        for (String word : normalCommandWords) {
            if (toLocation != null && this.entitiesHashMap.containsKey(word)) {
                return "You can only go to somewhere.";
            }

            // check if there is a valid path from current location to target location
            if (currentPlayer.getCurrentLocation().hasPath(word)) {
                if (toLocation != null) {
                    return "Which place do you want to go?";
                }
                // switch player's current location to the new location
                toLocation = this.locationsHashMap.get(word);
                currentPlayer.setCurrentLocation(toLocation);
            }
        }
        if (toLocation == null) {
            return "You cannot go to that location.";
        }
        // display new location's description
        return showLocationDetails(toLocation);
    }

    private String executeLookCommand(LinkedHashSet<String> normalCommandWords, Location currentLocation) {
        return showLocationDetails(currentLocation);
    }

    private String showLocationDetails(Location currentLocation) {
        StringBuilder details = new StringBuilder();
        details.append("You are at: ");
        details.append(currentLocation.getDescription());
        details.append("\n");

        if (!currentLocation.getPaths().isEmpty()) {
            details.append("Paths: ");
            for (String path : currentLocation.getPaths()) {
                details.append(path);
                details.append(", ");
            }
            // remove the last comma and blank, then add newline
            details.replace(details.length() - 2, details.length(), "\n");
        }

        if (!currentLocation.getCharacters().isEmpty()) {
            details.append("Characters: ");
            details.append(showObjectsDetails(currentLocation.getCharacters()));
        }

        if (!currentLocation.getArtefacts().isEmpty()) {
            details.append("Artefacts: ");
            details.append(showObjectsDetails(currentLocation.getArtefacts()));
        }

        if (!currentLocation.getFurniture().isEmpty()) {
            details.append("Furniture: ");
            details.append(showObjectsDetails(currentLocation.getFurniture()));
        }

        // delete the newline at the end
        details.delete(details.length() - 1, details.length());
        return details.toString();
    }

    private String showObjectsDetails(HashMap<String, GameEntity> objectsMap) {
        StringBuilder objectsDetails = new StringBuilder();
        for (String objectName : objectsMap.keySet()) {
            String characterDetail = objectsMap.get(objectName).getDescription();
            objectsDetails.append(characterDetail);
            objectsDetails.append(", ");
        }
        // remove the last comma and blank, then add newline
        objectsDetails.replace(objectsDetails.length() - 2, objectsDetails.length(), "\n");
        return objectsDetails.toString();
    }

    private String executeCustomAction(LinkedHashSet<String> normalCommandWords, Player currentPlayer, Location currentLocation, String mainCommandVerb) {
        if (normalCommandWords.isEmpty()) {
            return String.format("What exactly do you want to %s?", mainCommandVerb);
        }

        HashSet<GameAction> relatedActions = new HashSet<>();
        GameAction commandAction = null;

        for (GameAction gameAction : this.gameActions) {
            if (gameAction.getTriggers().contains(mainCommandVerb)) {
                relatedActions.add(gameAction);
            }
        }

        for (String word : normalCommandWords) {
            for (GameAction relatedAction : relatedActions) {
                if (relatedAction.getSubjects().contains(word)) {
                    if (commandAction != null) {
                        return String.format("Which subject do you want to %s?", mainCommandVerb);
                    }
                    commandAction = relatedAction;
                }
            }
        }

        if (commandAction == null) {
            return String.format("You cannot %s that thing.", mainCommandVerb);
        }

        // consume execution
        for (String objectToConsume : commandAction.getConsumed()) {
            if (currentPlayer.getInventory().containsKey(objectToConsume)) {
                this.storeroom.pushArtefact(currentPlayer.popInventory(objectToConsume));
            }
            if (objectToConsume.equals("health")) {
                currentPlayer.damageHealth(1);
            }
            if (currentLocation.getArtefacts().containsKey(objectToConsume)) {
                this.storeroom.pushArtefact(currentLocation.popArtefact(objectToConsume));
            }
            if (currentLocation.getFurniture().containsKey(objectToConsume)) {
                this.storeroom.pushFurniture(currentLocation.popFurniture(objectToConsume));
            }
            if (currentLocation.getCharacters().containsKey(objectToConsume)) {
                this.storeroom.pushCharacter(currentLocation.popCharacter(objectToConsume));
            }
            if (this.locationsHashMap.containsKey(objectToConsume)) {
                currentLocation.removePath(objectToConsume);
            }
        }

        // produce execution
        for (String objectToProduce : commandAction.getProduced()) {
            if (currentLocation.getArtefacts().containsKey(objectToProduce)) {
                currentPlayer.pushInventory(currentLocation.popArtefact(objectToProduce));
            }
            if (objectToProduce.equals("health")) {
                currentPlayer.restoreHealth(1);
            }
            if (this.storeroom.getArtefacts().containsKey(objectToProduce)) {
                currentPlayer.pushInventory(this.storeroom.popArtefact(objectToProduce));
            }
            if (this.storeroom.getFurniture().containsKey(objectToProduce)) {
                currentLocation.pushFurniture(this.storeroom.popFurniture(objectToProduce));
            }
            if (this.storeroom.getCharacters().containsKey(objectToProduce)) {
                currentLocation.pushCharacter(this.storeroom.popCharacter(objectToProduce));
            }
            if (this.locationsHashMap.containsKey(objectToProduce)) {
                currentLocation.addPath(objectToProduce);
            }
        }

        return commandAction.getNarration();
    }
}
