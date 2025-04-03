package edu.uob;

import edu.uob.entity.Location;
import edu.uob.entity.Player;
import edu.uob.executor.BuiltInExecutor;
import edu.uob.executor.CustomExecutor;
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
import java.util.LinkedHashSet;
import java.util.LinkedList;

public final class GameServer {

    // my codes
    private GameState gameState;
    private InputParser inputParser;

    private static final char END_OF_TRANSMISSION = 4;

    public static void main(String[] args) throws IOException {
        StringBuilder entitiesFilePath = new StringBuilder();
        StringBuilder actionsFilePath = new StringBuilder();
        entitiesFilePath.append("config").append(File.separator).append("extended-entities.dot");
        actionsFilePath.append("config").append(File.separator).append("extended-actions.xml");

        File entitiesFile = Paths.get(entitiesFilePath.toString()).toAbsolutePath().toFile();
        File actionsFile = Paths.get(actionsFilePath.toString()).toAbsolutePath().toFile();
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
        // Implement your server logic here
        try {
            EntityParser entityParser = new EntityParser(entitiesFile);
            ActionParser actionParser = new ActionParser(actionsFile);

            if (entityParser.getStoreroom() == null) {
                if (!entityParser.generateStoreroom()) {
                    throw new RuntimeException("Failed to generate storeroom!");
                }
            }

            this.gameState = new GameState(entityParser, actionParser);
            this.inputParser = new InputParser(this.gameState);

        } catch (Exception e) {
            StringBuilder errorString = new StringBuilder();
            errorString.append("[ERROR] Failed to initialize game server: ").append(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(errorString.toString());
        }
    }

    public GameState getGameState() {
        return this.gameState;
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        // Implement your server logic here
        try {
            this.inputParser.parseInput(command);

            String playerName = this.inputParser.getPlayerName();
            LinkedHashSet<String> commandEntities = this.inputParser.getCommandEntities();

            String mainCommandVerb = this.inputParser.getMainCommandVerb(); // only for built-in command
            LinkedList<String> possibleTriggers = this.inputParser.getPossibleTriggers();

            // check if the player name exists, case-insensitive when searching
            boolean existingPlayer = false;
            for (String existingName : this.gameState.getPlayersHashMap().keySet()) {
                if (playerName.equalsIgnoreCase(existingName)) {
                    existingPlayer = true;
                    break;
                }
            }
            if (!existingPlayer) {
                this.gameState.addNewPlayer(playerName);
            }

            Player currentPlayer = this.gameState.getPlayersHashMap().get(playerName);
            Location currentLocation = currentPlayer.getCurrentLocation();

            String result = null;
            if (mainCommandVerb != null) {
                result = switch (mainCommandVerb) {
                    case "inventory", "inv":
                        yield new BuiltInExecutor(this.gameState).executeInventory(currentPlayer);
                    case "get":
                        yield new BuiltInExecutor(this.gameState).executeGet(commandEntities, currentPlayer, currentLocation);
                    case "drop":
                        yield new BuiltInExecutor(this.gameState).executeDrop(commandEntities, currentPlayer, currentLocation);
                    case "goto":
                        yield new BuiltInExecutor(this.gameState).executeGoto(commandEntities, currentPlayer);
                    case "look":
                        yield new BuiltInExecutor(this.gameState).executeLook(currentPlayer.getName(), currentLocation);
                    case "health":
                        yield new BuiltInExecutor(this.gameState).executeHealth(currentPlayer);
                    default:
                        throw new RuntimeException(String.format("Unknown command verb: %s", mainCommandVerb));
                };
            } else {
                result = new CustomExecutor(this.gameState).executeAction(commandEntities, currentPlayer, currentLocation, possibleTriggers);
            }

            if (result == null) {
                throw new RuntimeException(String.format("For command verb: \"%s\", cannot find the command entity: %s", mainCommandVerb, commandEntities));
            }
            return result;

        } catch (Exception e) {
            return String.format("[ERROR] Failed to handle command: \n%s", e.getMessage());
        }
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
            System.out.printf("Server listening on port %d\n", portNumber);
            while (!Thread.interrupted()) {
                try {
                    this.blockingHandleConnection(s);
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
                System.out.printf("Received message from %s\n", incomingCommand);
                String result = this.handleCommand(incomingCommand);
                writer.write(result);
                writer.write(String.format("\n%c\n", END_OF_TRANSMISSION));
                writer.flush();
            }
        }
    }
}
