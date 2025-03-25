package edu.uob;

import edu.uob.action.GameAction;
import edu.uob.entity.GameEntity;
import edu.uob.entity.Location;
import edu.uob.entity.Player;
import edu.uob.parser.ActionParser;
import edu.uob.parser.EntityParser;
import edu.uob.parser.InputParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public final class GameServer {

    // my codes
    private HashMap<String, Location> locationsHashMap;
    private Location startLocation;
    private Location storeroom;
    private HashMap<String, GameEntity> entitiesHashMap;
    private HashMap<String, Player> playersHashMap;
    private int maxHealth;
    private HashSet<GameAction> gameActions;
    private InputParser inputParser;

    private static final char END_OF_TRANSMISSION = 4;

    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    public GameServer(File entitiesFile, File actionsFile) {
        // TODO implement your server logic here
        try {
            EntityParser entityParser = new EntityParser(entitiesFile);
            ActionParser actionParser = new ActionParser(actionsFile);

            if (entityParser.getStoreroom() == null) {
                if (!entityParser.genarateStoreroom()) {
                    throw new RuntimeException("Failed to generate storeroom!");
                }
            }

            this.locationsHashMap = entityParser.getLocationMap();
            this.startLocation = entityParser.getStartLocation();
            this.storeroom = entityParser.getStoreroom();
            this.entitiesHashMap = entityParser.getEntityMap();
            this.playersHashMap = new HashMap<>();
            this.maxHealth = 3;
            this.gameActions = actionParser.getGameActions();
            this.inputParser = new InputParser();

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to initialize game server: ");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here

        // divide player name and actual command statement
        /* Returns: the index of the first occurrence of the specified substring, or -1 if there is no such occurrence. */
        int colonIndex = command.indexOf(":");
        if (colonIndex == -1) {
            throw new RuntimeException("Invalid command: no colon to split player name and command statement! ");
        }
        String playerName = command.substring(0, colonIndex).trim().toLowerCase();
        String actualCommand = command.substring(colonIndex + 1).trim().toLowerCase();

        if (!playersHashMap.containsKey(playerName)) {
            Player newPlayer = new Player(playerName, this.maxHealth, this.startLocation);
            this.playersHashMap.put(playerName, newPlayer);
        }
        Player currentPlayer = this.playersHashMap.get(playerName);

        String result = processBuiltInCommand(currentPlayer, actualCommand);
        if (result == null) {
            result = processCustomAction(currentPlayer, actualCommand);
        }

        if (result == null) {
            throw new RuntimeException(String.format("Unknown command: %s", actualCommand));
        }

        return result;
    }

    // my codes

    /** built-in commands: inventory / inv, get, drop, goto, look */
    private String processBuiltInCommand(Player currentPlayer, String actualCommand) {
        Scanner commandScanner = new Scanner(actualCommand);
        if (!commandScanner.hasNext()) {
            throw new RuntimeException("Cannot process void built-in command!");
        }

        String builtInCommand = commandScanner.next().trim().toLowerCase();

        Location currentLocation = currentPlayer.getCurrentLocation();

        switch (builtInCommand) {

            case "inventory", "inv":
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

            case "get":
                if (!commandScanner.hasNext()) {
                    return "What do you want to get?";
                }

                String objectToGet = null;

//                String somethingToGet = commandScanner.nextLine().trim().toLowerCase();
//                Pattern entityNamePattern = this.inputParser.getEntityNamePattern();
//                for (String artefactName : currentLocation.getArtefacts().keySet()) {}

                while (commandScanner.hasNext()) {
                    String somethingToGet = commandScanner.next().trim().toLowerCase();

                    if (objectToGet == null && currentPlayer.getInventory().containsKey(somethingToGet)) {
                        return "You already had that.";
                    }

                    if (currentLocation.getArtefacts().containsKey(somethingToGet)) {
                        if (objectToGet != null) {
                            return "Which one do you want to get?";
                        }

                        objectToGet = somethingToGet;
                    }
                }

                try {
                    currentPlayer.pushInventory(currentLocation.popArtefact(objectToGet));
                    return String.format("Taken: %s", objectToGet);
                } catch (Exception e) {
                    return "You cannot get that thing.";
                }

            /* TODO:
                Lucy:> drop potion, axe
                Dropped: axe
            */
            case "drop":
                if (!commandScanner.hasNext()) {
                    return "What do you want to drop?";
                }

                String objectToDrop = null;

                while (commandScanner.hasNext()) {
                    String somethingToDrop = commandScanner.next().trim().toLowerCase();

                    if (currentPlayer.getInventory().containsKey(somethingToDrop)) {
                        if (objectToDrop != null) {
                            return "Which one do you want to drop?";
                        }

                        objectToDrop = somethingToDrop;
                    }
                }

                try {
                    currentLocation.pushArtefact(currentPlayer.popInventory(objectToDrop));
                    return String.format("Dropped: %s", objectToDrop);
                } catch (Exception e) {
                    return "You cannot drop that thing.";
                }

            case "goto":
                if (!commandScanner.hasNext()) {
                    return "Where do you want to go?";
                }
                String placeToGo = commandScanner.next().trim().toLowerCase();

                // check if there is a valid path from current location to target location
                if (currentPlayer.getCurrentLocation().hasPath(placeToGo)) {
                    // switch player's current location to the new location
                    Location toLocation = this.locationsHashMap.get(placeToGo);
                    currentPlayer.setCurrentLocation(toLocation);
                    // display new location's description
                    return printLocationDetails(toLocation);
                }
                // no way to the target location from current location
                return "You cannot go to that location.";

            case "look":
                return printLocationDetails(currentPlayer.getCurrentLocation());

            default:
                return null;
        }
    }

    private String printLocationDetails(Location currentLocation) {
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
            details.append(printObjectsDetails(currentLocation.getCharacters()));
        }

        if (!currentLocation.getArtefacts().isEmpty()) {
            details.append("Artefacts: ");
            details.append(printObjectsDetails(currentLocation.getArtefacts()));
        }

        if (!currentLocation.getFurniture().isEmpty()) {
            details.append("Furniture: ");
            details.append(printObjectsDetails(currentLocation.getFurniture()));
        }

        // delete the newline at the end
        details.delete(details.length() - 1, details.length());
        return details.toString();
    }

    private String printObjectsDetails(HashMap<String, GameEntity> objectsMap) {
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

    private String processCustomAction(Player currentPlayer, String actualCommand) {
        // TODO: handle custom actions
        return "TODO: DEVELOPING...";
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
