package edu.uob.executor;

import edu.uob.GameState;
import edu.uob.action.GameAction;
import edu.uob.entity.EntityType;
import edu.uob.entity.Location;
import edu.uob.entity.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class CustomExecutor extends CommandExecutor{
    public CustomExecutor(GameState gameState) {
        super(gameState);
    }

    public String executeAction(LinkedHashSet<String> commandEntities, Player currentPlayer, Location currentLocation, LinkedList<String> possibleTriggers) {
        if (commandEntities.isEmpty()) {
            return "What exactly do you want to do?";
        }

        // find the action that the user wants to perform
        HashSet<GameAction> relatedActions = new HashSet<>();
        GameAction commandAction = null;

        for (String possibleTrigger : possibleTriggers) {
            for (GameAction gameAction : this.gameState.getAllActions()) {
                if (gameAction.getTriggers().contains(possibleTrigger)) {
                    relatedActions.add(gameAction);
                }
            }
        }

        for (String commandEntity : commandEntities) {
            boolean extraneousEntity = true;

            for (GameAction relatedAction : relatedActions) {
                if (relatedAction.getSubjects().contains(commandEntity)) {
                    extraneousEntity = false;

                    // case that multiple subjects but all are in one same action
                    if (commandAction != null && commandAction == relatedAction) {
                        break;
                    }

                    if (isAvailable(relatedAction, currentPlayer, currentLocation)) {
                        if (commandAction != null) {
                            return "There are more than one choice. Which one do you prefer?";
                        }

                        commandAction = relatedAction;
                    }
                }
            }
            if (extraneousEntity) {
                return String.format("Extraneous entity %s.", commandEntity);
            }
        }

        if (commandAction == null) {
            return String.format("You cannot do it.");
        }

        // execute this action's consumed and produced
        this.processConsume(commandAction, currentPlayer, currentLocation);
        this.processProduce(commandAction, currentPlayer, currentLocation);

        // check player's health status
        if (currentPlayer.getHealth() <= 0) {
            currentPlayer.resetStatus(this.gameState.getStartLocation());
            return "You died and lost all of your items, you must return to the start of the game.";
        }

        return commandAction.getNarration();
    }

    /** check if this action is available for player */
    private boolean isAvailable(GameAction commandAction, Player currentPlayer, Location currentLocation) {
        HashSet<String> actionSubjects = commandAction.getSubjects();

        for (String actionSubject : actionSubjects) {
            boolean currentSubjectAvailable;
            EntityType subjectType = this.gameState.getAllEntities().get(actionSubject).getType();
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
                    throw new RuntimeException(String.format("Cannot tell the action subject: %s", actionSubject));
            }

            // if any subject is unavailable, this action is unavailable
            if (!currentSubjectAvailable) {
                return false;
            }
        }
        return true;
    }

    /** consume execution */
    private void processConsume(GameAction commandAction, Player currentPlayer, Location currentLocation) {
        for (String objectToConsume : commandAction.getConsumed()) {
            if (objectToConsume.equals("health")) {
                if (currentPlayer.damageHealth(1)) {
                    break;
                } else {
                    throw new RuntimeException("GameState: Player.damageHealth(): You don't have enough health to reduce!");
                }
            }

            String objectOldLocationName = super.gameState.getAllEntities().get(objectToConsume).getLivingRoom();
            // It is NOT possible to perform an action where any subject, consumed entity or produced entity is currently in another player's inventory.
            if (objectOldLocationName == null && !currentPlayer.getInventory().containsKey(objectToConsume)) {
                throw new RuntimeException(String.format("Cannot consume \"%s\" here.", objectToConsume));
            }
            Location objectOldLocation = super.gameState.getAllLocations().get(objectOldLocationName);

            EntityType entityType = super.gameState.getAllEntities().get(objectToConsume).getType();
            switch (entityType) {
                case LOCATION:
                    currentLocation.removePath(objectToConsume);
                    break;
                case CHARACTER:
                    super.gameState.getStoreroom().pushCharacter(objectOldLocation.popCharacter(objectToConsume));
                    break;
                case ARTEFACT:
                    if (currentPlayer.getInventory().containsKey(objectToConsume)) {
                        super.gameState.getStoreroom().pushArtefact(currentPlayer.popInventory(objectToConsume));
                        break;
                    } else if (objectOldLocation.getArtefacts().containsKey(objectToConsume)) {
                        super.gameState.getStoreroom().pushArtefact(objectOldLocation.popArtefact(objectToConsume));
                        break;
                    } else {
                        throw new RuntimeException(String.format("Cannot consume \"%s\" here.", objectToConsume));
                    }
                case FURNITURE:
                    super.gameState.getStoreroom().pushFurniture(objectOldLocation.popFurniture(objectToConsume));
                    break;
                default:
                    throw new RuntimeException(String.format("Cannot consume this entity type. What is \"%s\"?", objectToConsume));
            }
        }
    }

    /** produce execution */
    private void processProduce(GameAction commandAction, Player currentPlayer, Location currentLocation) {
        for (String objectToProduce : commandAction.getProduced()) {
            if (objectToProduce.equals("health")) {
                if (currentPlayer.restoreHealth(1)) {
                    break;
                } else {
                    throw new RuntimeException("GameState: Player.restoreHealth(): You don't have enough capacity to restore so mush health!");
                }
            }

            String objectOldLocationName = super.gameState.getAllEntities().get(objectToProduce).getLivingRoom();
            // It is NOT possible to perform an action where any subject, consumed entity or produced entity is currently in another player's inventory.
            if (objectOldLocationName == null && !currentPlayer.getInventory().containsKey(objectToProduce)) {
                throw new RuntimeException(String.format("Cannot produce \"%s\" here.", objectToProduce));
            }
            Location objectOldLocation = super.gameState.getAllLocations().get(objectOldLocationName);

            EntityType entityType =super.gameState.getAllEntities().get(objectToProduce).getType();
            switch (entityType) {
                case LOCATION:
                    currentLocation.addPath(objectToProduce);
                    break;
                case CHARACTER:
                    currentLocation.pushCharacter(objectOldLocation.popCharacter(objectToProduce));
                    break;
                case ARTEFACT:
                    currentLocation.pushArtefact(objectOldLocation.popArtefact(objectToProduce));
                    break;
                case FURNITURE:
                    currentLocation.pushFurniture(objectOldLocation.popFurniture(objectToProduce));
                    break;
                default:
                    throw new RuntimeException(String.format("Cannot produce this entity type. What is \"%s\"?", objectToProduce));
            }
        }
    }
}
