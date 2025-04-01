package edu.uob.executor;

import edu.uob.GameState;
import edu.uob.action.GameAction;
import edu.uob.entity.EntityType;
import edu.uob.entity.Location;
import edu.uob.entity.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class CustomExecutor extends CommandExecutor{
    private LinkedHashSet<String> commandEntities;
    private Player currentPlayer;
    private Location currentLocation;
    private String mainCommandVerb;

    public CustomExecutor(GameState gameState) {
        super(gameState);
    }

    public String executeAction(LinkedHashSet<String> commandEntities, Player currentPlayer, Location currentLocation, String mainCommandVerb) {
        if (commandEntities.isEmpty()) {
            return String.format("What exactly do you want to %s?", mainCommandVerb);
        }

        this.commandEntities = commandEntities;
        this.currentPlayer = currentPlayer;
        this.currentLocation = currentLocation;
        this.mainCommandVerb = mainCommandVerb;

        // find the action that the user wants to perform
        HashSet<GameAction> relatedActions = new HashSet<>();
        GameAction commandAction = null;

        for (GameAction gameAction : this.gameState.getAllActions()) {
            if (gameAction.getTriggers().contains(this.mainCommandVerb)) {
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
                return String.format("Extraneous entity %s for trigger %s", commandEntity, this.mainCommandVerb);
            }
        }

        if (commandAction == null) {
            return String.format("You cannot %s that thing.", this.mainCommandVerb);
        }

        // check if this action is available for player
        if (!this.isAvailable(commandAction)) {
            return String.format("You are unable to %s here.", this.mainCommandVerb);
        }

        // execute this action's consumed and produced
        this.processConsume(commandAction);
        this.processProduce(commandAction);

        // check player's health status
        if (currentPlayer.getHealth() <= 0) {
            currentPlayer.resetStatus(this.gameState.getStartLocation());
            return "You died and lost all of your items, you must return to the start of the game.";
        }

        return commandAction.getNarration();
    }

    /** check if this action is available for player */
    private boolean isAvailable(GameAction commandAction) {
        HashSet<String> actionSubjects = commandAction.getSubjects();
        for (String actionSubject : actionSubjects) {
            boolean currentSubjectAvailable = false;
            EntityType subjectType = this.gameState.getAllEntities().get(actionSubject).getType();
            switch (subjectType) {
                case LOCATION:
                    currentSubjectAvailable = this.currentLocation.hasPath(actionSubject);
                    break;
                case CHARACTER:
                    currentSubjectAvailable = this.currentLocation.getCharacters().containsKey(actionSubject);
                    break;
                case ARTEFACT:
                    currentSubjectAvailable = this.currentPlayer.getInventory().containsKey(actionSubject) || this.currentLocation.getArtefacts().containsKey(actionSubject);
                    break;
                case FURNITURE:
                    currentSubjectAvailable = this.currentLocation.getFurniture().containsKey(actionSubject);
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
    private void processConsume(GameAction commandAction) {
        for (String objectToConsume : commandAction.getConsumed()) {
            if (objectToConsume.equals("health")) {
                if (currentPlayer.damageHealth(1)) {
                    break;
                } else {
                    throw new RuntimeException("GameState: Player.damageHealth(): You don't have enough health to reduce!");
                }
            }

            EntityType entityType = super.gameState.getAllEntities().get(objectToConsume).getType();
            switch (entityType) {
                case LOCATION:
                    currentLocation.removePath(objectToConsume);
                    break;
                case CHARACTER:
                    super.gameState.getStoreroom().pushCharacter(currentLocation.popCharacter(objectToConsume));
                    break;
                case ARTEFACT:
                    if (currentPlayer.getInventory().containsKey(objectToConsume)) {
                        super.gameState.getStoreroom().pushArtefact(currentPlayer.popInventory(objectToConsume));
                        break;
                    } else if (currentLocation.getArtefacts().containsKey(objectToConsume)) {
                        super.gameState.getStoreroom().pushArtefact(currentLocation.popArtefact(objectToConsume));
                        break;
                    } else {
                        throw new RuntimeException(String.format("Cannot consume \"%s\" here.", objectToConsume));
                    }
                case FURNITURE:
                    super.gameState.getStoreroom().pushFurniture(currentLocation.popFurniture(objectToConsume));
                    break;
                default:
                    throw new RuntimeException(String.format("Cannot consume this entity type. What is \"%s\"?", objectToConsume));
            }
        }
    }

    /** produce execution */
    private void processProduce(GameAction commandAction) {
        for (String objectToProduce : commandAction.getProduced()) {
            if (objectToProduce.equals("health")) {
                if (currentPlayer.restoreHealth(1)) {
                    break;
                } else {
                    throw new RuntimeException("GameState: Player.restoreHealth(): You don't have enough capacity to restore so mush health!");
                }
            }

            EntityType entityType =super.gameState.getAllEntities().get(objectToProduce).getType();
            switch (entityType) {
                case LOCATION:
                    currentLocation.addPath(objectToProduce);
                    break;
                case CHARACTER:
                    currentLocation.pushCharacter(super.gameState.getStoreroom().popCharacter(objectToProduce));
                    break;
                case ARTEFACT:
                    currentLocation.pushArtefact(super.gameState.getStoreroom().popArtefact(objectToProduce));
                    break;
                case FURNITURE:
                    currentLocation.pushFurniture(super.gameState.getStoreroom().popFurniture(objectToProduce));
                    break;
                default:
                    throw new RuntimeException(String.format("Cannot produce this entity type. What is \"%s\"?", objectToProduce));
            }
        }
    }
}
