package edu.uob;

import edu.uob.action.GameAction;
import edu.uob.entity.EntityType;
import edu.uob.entity.GameEntity;
import edu.uob.entity.Location;
import edu.uob.entity.Player;
import edu.uob.parser.ActionParser;
import edu.uob.parser.EntityParser;
import edu.uob.parser.InputParser;

import java.util.*;

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
        this.inputParser = new InputParser(entityParser, actionParser);
    }

    public String handleInput(String input) {
        this.inputParser.parseInput(input);

        String playerName = this.inputParser.getPlayerName();
        LinkedHashSet<String> commandEntities = this.inputParser.getCommandEntities();
        String mainCommandVerb = this.inputParser.getMainCommandVerb();

        // check if the player name exists
        if (!playersHashMap.containsKey(playerName)) {
            Player newPlayer = new Player(playerName, this.maxHealth, this.startLocation);
            this.playersHashMap.put(playerName, newPlayer);
        }

        Player currentPlayer = this.playersHashMap.get(playerName);
        Location currentLocation = currentPlayer.getCurrentLocation();

        String result = switch (mainCommandVerb) {
            case "inventory", "inv" -> executeInventoryCommand(currentPlayer);
            case "get" -> executeGetCommand(commandEntities, currentPlayer, currentLocation);
            case "drop" -> executeDropCommand(commandEntities, currentPlayer, currentLocation);
            case "goto" -> executeGotoCommand(commandEntities, currentPlayer);
            case "look" -> showLocationDetails(currentLocation);
            default -> executeCustomAction(commandEntities, currentPlayer, currentLocation, mainCommandVerb);
        };

        if (result == null) {
            throw new RuntimeException(String.format("For command verb: \"%s\", cannot find the command entity: %s", mainCommandVerb, commandEntities));
        }
        return result;
    }

    private String executeInventoryCommand(Player currentPlayer) {
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

    private String executeGetCommand(LinkedHashSet<String> commandEntities, Player currentPlayer, Location currentLocation) {
        String objectToGet = null;

        if (commandEntities.size() == 1) {
            objectToGet = commandEntities.iterator().next();
        } else if (commandEntities.size() > 1) {
            return "You can only get one thing at a time.";
        } else {
            return "What do you want to get?";
        }

        if (currentPlayer.getInventory().containsKey(objectToGet)) {
            return "You already had that.";
        }

        try {
            currentPlayer.pushInventory(currentLocation.popArtefact(objectToGet));
            return String.format("Taken: %s", objectToGet);
        } catch (Exception e) {
            return "You cannot get that thing.";
        }
    }

    private String executeDropCommand(LinkedHashSet<String> commandEntities, Player currentPlayer, Location currentLocation) {
        String objectToDrop = null;

        if (commandEntities.size() == 1) {
            objectToDrop = commandEntities.iterator().next();
        } else if (commandEntities.size() > 1) {
            return "You can only drop one thing at a time.";
        } else {
            return "What do you want to drop?";
        }

        try {
            currentLocation.pushArtefact(currentPlayer.popInventory(objectToDrop));
            return String.format("Dropped: %s", objectToDrop);
        } catch (Exception e) {
            return "You cannot drop that thing.";
        }
    }

    private String executeGotoCommand(LinkedHashSet<String> commandEntities, Player currentPlayer) {
        if (commandEntities.isEmpty()) {
            return "Where do you want to go?";
        }

        Location toLocation = null;

        for (String word : commandEntities) {
            if (toLocation != null) {
                if (this.locationsHashMap.containsKey(word)) {
                    return "You can only go to one place at a time.";
                }
                if (this.entitiesHashMap.containsKey(word)) {
                    return "You can only go to somewhere.";
                }
            }

            // check if there is a valid path from current location to target location
            if (currentPlayer.getCurrentLocation().hasPath(word)) {
                if (toLocation != null) {
                    return "Which place do you want to go?";
                }

                toLocation = this.locationsHashMap.get(word);
            }
        }
        if (toLocation == null) {
            return "You cannot go to that place.";
        }

        // switch player's current location to the new location
        currentPlayer.setCurrentLocation(toLocation);
        // display new location's description
        return showLocationDetails(toLocation);
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

    private String executeCustomAction(LinkedHashSet<String> commandEntities, Player currentPlayer, Location currentLocation, String mainCommandVerb) {
        if (commandEntities.isEmpty()) {
            return String.format("What exactly do you want to %s?", mainCommandVerb);
        }

        HashSet<GameAction> relatedActions = new HashSet<>();
        GameAction commandAction = null;

        for (GameAction gameAction : this.gameActions) {
            if (gameAction.getTriggers().contains(mainCommandVerb)) {
                relatedActions.add(gameAction);
            }
        }

        for (String commandEntity : commandEntities) {
            boolean extraneousEntity = true;

            for (GameAction relatedAction : relatedActions) {
                if (relatedAction.getSubjects().contains(commandEntity)) {
                    extraneousEntity = false;

                    // case that multiple subjects but all are in one same action
                    if (commandAction == relatedAction) {
                        break;
                    }

                    commandAction = relatedAction;
                    // if found valid custom action subject in this GameAction, mark and skip checking the rest of subjects in this GameAction (if there are)
                    break;
                }
            }
            if (extraneousEntity) {
                return String.format("Extraneous entity %s for trigger %s", commandEntity, mainCommandVerb);
            }
        }

        if (commandAction == null) {
            return String.format("You cannot %s that thing.", mainCommandVerb);
        }

        // check if this action is available for player
        HashSet<String> actionSubjects = commandAction.getSubjects();
        for (String actionSubject : actionSubjects) {
            boolean currentSubjectAvailable = false;
            EntityType subjectType = this.entitiesHashMap.get(actionSubject).getType();
            switch (subjectType) {
                case LOCATION:
                    currentSubjectAvailable = currentLocation.hasPath(actionSubject);
                    break;
                case CHARACTER:
                    currentSubjectAvailable = currentLocation.getCharacters().containsKey(actionSubject);
                    break;
                case ARTEFACT:
                    currentSubjectAvailable = currentPlayer.getInventory().containsKey(actionSubject) || currentLocation.getArtefacts().containsKey(actionSubject);
                    break;
                case FURNITURE:
                    currentSubjectAvailable = currentLocation.getFurniture().containsKey(actionSubject);
                    break;
                default:
                    return String.format("Cannot tell the action subject: %s", actionSubject);
            }

            // if any subject is unavailable, this action is unavailable
            if (!currentSubjectAvailable) {
                return String.format("You are unable to %s here.", mainCommandVerb);
            }
        }

        // consume execution
        for (String objectToConsume : commandAction.getConsumed()) {
            if (objectToConsume.equals("health")) {
                if (currentPlayer.damageHealth(1)) {
                    break;
                } else {
                    throw new RuntimeException("GameSystem: Player.damageHealth(): You don't have enough health to reduce!");
                }
            }

            EntityType entityType = this.entitiesHashMap.get(objectToConsume).getType();
            switch (entityType) {
                case LOCATION:
                    currentLocation.removePath(objectToConsume);
                    break;
                case CHARACTER:
                    this.storeroom.pushCharacter(currentLocation.popCharacter(objectToConsume));
                    break;
                case ARTEFACT:
                    if (currentPlayer.getInventory().containsKey(objectToConsume)) {
                        this.storeroom.pushArtefact(currentPlayer.popInventory(objectToConsume));
                        break;
                    } else if (currentLocation.getArtefacts().containsKey(objectToConsume)) {
                        this.storeroom.pushArtefact(currentLocation.popArtefact(objectToConsume));
                        break;
                    } else {
                        return String.format("Cannot consume \"%s\" here.", objectToConsume);
                    }
                case FURNITURE:
                    this.storeroom.pushFurniture(currentLocation.popFurniture(objectToConsume));
                    break;
                default:
                    return String.format("Cannot consume this entity type. What is \"%s\"?", objectToConsume);
            }
        }

        // produce execution
        for (String objectToProduce : commandAction.getProduced()) {
            if (objectToProduce.equals("health")) {
                if (currentPlayer.restoreHealth(1)) {
                    break;
                } else {
                    throw new RuntimeException("GameSystem: Player.restoreHealth(): You don't have enough capacity to restore so mush health!");
                }
            }

            EntityType entityType = this.entitiesHashMap.get(objectToProduce).getType();
            switch (entityType) {
                case LOCATION:
                    currentLocation.addPath(objectToProduce);
                    break;
                case CHARACTER:
                    currentLocation.pushCharacter(this.storeroom.popCharacter(objectToProduce));
                    break;
                case ARTEFACT:
                    currentLocation.pushArtefact(this.storeroom.popArtefact(objectToProduce));
                    break;
                case FURNITURE:
                    currentLocation.pushFurniture(this.storeroom.popFurniture(objectToProduce));
                    break;
                default:
                    return String.format("Cannot produce this entity type. What is \"%s\"?", objectToProduce);
            }
        }

        return commandAction.getNarration();
    }
}
