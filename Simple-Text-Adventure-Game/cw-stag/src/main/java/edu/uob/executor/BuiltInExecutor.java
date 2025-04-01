package edu.uob.executor;

import edu.uob.GameState;
import edu.uob.entity.GameEntity;
import edu.uob.entity.Location;
import edu.uob.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class BuiltInExecutor extends CommandExecutor{
    public BuiltInExecutor(GameState gameState) {
        super(gameState);
    }

    public String executeInventory(Player currentPlayer) {
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

    public String executeGet(LinkedHashSet<String> commandEntities, Player currentPlayer, Location currentLocation) {
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

    public String executeDrop(LinkedHashSet<String> commandEntities, Player currentPlayer, Location currentLocation) {
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

    public String executeGoto(LinkedHashSet<String> commandEntities, Player currentPlayer) {
        if (commandEntities.isEmpty()) {
            return "Where do you want to go?";
        }

        Location toLocation = null;

        for (String word : commandEntities) {
            if (toLocation != null) {
                if (super.gameState.getAllLocations().containsKey(word)) {
                    return "You can only go to one place at a time.";
                }
                if (super.gameState.getAllEntities().containsKey(word)) {
                    return "You can only go to somewhere.";
                }
            }

            // check if there is a valid path from current location to target location
            if (currentPlayer.getCurrentLocation().hasPath(word)) {
                if (toLocation != null) {
                    return "Which place do you want to go?";
                }

                toLocation = super.gameState.getAllLocations().get(word);
            }
        }
        if (toLocation == null) {
            return "You cannot go to that place.";
        }

        // switch player's current location to the new location
        currentPlayer.getCurrentLocation().removePlayer(currentPlayer.getName());
        currentPlayer.setCurrentLocation(toLocation);
        currentPlayer.getCurrentLocation().addPlayer(currentPlayer.getName());
        // display new location's description
        return this.showLocationDetails(currentPlayer.getName(), toLocation);
    }

    public String executeLook(String currentPlayerName, Location currentLocation) {
        return this.showLocationDetails(currentPlayerName, currentLocation);
    }

    public String executeHealth(Player currentPlayer) {
        return String.format("Your current health level: %d", currentPlayer.getHealth());
    }

    private String showLocationDetails(String currentPlayerName, Location currentLocation) {
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

        // only show nearby players' names when there are other players here (not just current player here)
        if (currentLocation.getPlayersNames().size() > 1) {
            details.append("Players: ");
            details.append(showNearbyPlayers(currentPlayerName, currentLocation.getPlayersNames()));
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

    private String showNearbyPlayers(String currentPlayerName, HashSet<String> playersNames) {
        StringBuilder nearbyPlayers = new StringBuilder();
        for (String playerName : playersNames) {
            if (!playerName.equals(currentPlayerName)) {
                nearbyPlayers.append(playerName);
                nearbyPlayers.append(", ");
            }
        }
        // remove the last comma and blank, then add newline
        nearbyPlayers.replace(nearbyPlayers.length() - 2, nearbyPlayers.length(), "\n");
        return nearbyPlayers.toString();
    }
}
