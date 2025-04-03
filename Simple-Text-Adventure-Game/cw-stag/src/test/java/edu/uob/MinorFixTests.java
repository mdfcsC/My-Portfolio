package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MinorFixTests {
    private GameServer server;
    private GameState gameState;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "modified-extended-actions.xml").toAbsolutePath().toFile();
        this.server = new GameServer(entitiesFile, actionsFile);
        this.gameState = this.server.getGameState();
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
                    return server.handleCommand(command);
                },
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // Must delete line 24 `<keyphrase>cut</keyphrase>` in extended-actions.xml to pass this test
    // BUT now this test file use modified-extended-actions.xml
    @Test
    void testUninterruptedTrigger() {
        sendCommandToServer("Lucy: get axe");
        sendCommandToServer("Lucy: goto forest");

        String response = sendCommandToServer("Lucy: cut tree down");
        assertTrue(response.contains("ERROR"), "The whole uninterrupted trigger \"cut down\" needs to be present in the command");

        response = sendCommandToServer("Lucy: cutdown tree");
        assertTrue(response.contains("ERROR"), "The whole trigger phrase \"cut down\" needs to be present in the command");

        response = sendCommandToServer("Lucy: down tree");
        assertTrue(response.contains("ERROR"), "The whole trigger phrase \"cut down\" needs to be present in the command");

        response = sendCommandToServer("Lucy: down cut tree");
        assertTrue(response.contains("ERROR"), "The whole trigger phrase \"cut down\" needs to be present in the command");
    }

    @Test
    void testEntityMoveWhenProduced() {
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: goto riverbank");
        String response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("A burly wood cutter"), "In initial status there is no lumberjack at riverbank");

        sendCommandToServer("Lucy: get horn");
        sendCommandToServer("Lucy: blow horn");
        response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("A burly wood cutter"), "First blow horn should take lumberjack from storeroom to current location");
        assertFalse(this.gameState.getStoreroom().getCharacters().containsKey("lumberjack"), "After blowing horn, lumberjack should not be in storeroom");

        sendCommandToServer("Lucy: goto forest");
        response = sendCommandToServer("Lucy: blow horn");
        assertTrue(response.contains("You blow the horn and as if by magic, a lumberjack appears !"), "\"blow horn\" action should be able to repeat executing");
        response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("A burly wood cutter"), "Should take lumberjack from anywhere to current location");
        assertFalse(this.gameState.getAllLocations().get("riverbank").getCharacters().containsKey("lumberjack"), "There should not be lumberjack at its previous location");
    }

    @Test
    void testEntityMoveWhenConsumed() {
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: goto riverbank");
        sendCommandToServer("Lucy: get horn");
        sendCommandToServer("Lucy: blow horn");
        assertFalse(this.gameState.getStoreroom().getCharacters().containsKey("lumberjack"), "After \"blow horn\", lumberjack should not be in storeroom");

        String response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("A burly wood cutter"), "\"blow horn\" should produce a lumberjack to current location");

        sendCommandToServer("Lucy: goto forest");
        response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("A burly wood cutter"), "In initial status there is no lumberjack at forest");

        response = sendCommandToServer("Lucy: reverse-blow horn");
        System.out.println(response);
        assertTrue(response.contains("You reverse-blow the horn and as if by magic, the lumberjack disappears"), "Should tell \"reverse-blow horn\" correctly");

        response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("A burly wood cutter"), "Wherever \"reverse-blow horn\" should consume the lumberjack");
        assertTrue(this.gameState.getStoreroom().getCharacters().containsKey("lumberjack"), "After \"reverse-blow horn\", lumberjack should be back to storeroom");

        sendCommandToServer("Lucy: goto riverbank");
        response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("A burly wood cutter"), "Now there should not be a lumberjack at riverbank");
    }

    @Test
    void testTwoTriggersButOneHasValidSubject() {
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: get key");
        sendCommandToServer("Lucy: goto cabin");
        sendCommandToServer("Lucy: open door with the key");
        sendCommandToServer("Lucy: goto cellar");
        String response = sendCommandToServer("Lucy: fight elf and pay elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"), "Without coin, the command should succeed to fight elf");
        response = sendCommandToServer("Lucy: hit and pay elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"), "Without coin, the command should succeed to hit elf");

        sendCommandToServer("Lucy: goto cabin");
        sendCommandToServer("Lucy: get coin");
        sendCommandToServer("Lucy: goto cellar");
        response = sendCommandToServer("Lucy: fight elf and pay elf");
        assertTrue(response.contains("Which one do you prefer?"), "With coin, should fail due to two valid commands at a time");
    }

    @Test
    void testTwoCompleteCommands() {
        sendCommandToServer("Lucy: get axe");
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: chop tree");
        sendCommandToServer("Lucy: get log");
        sendCommandToServer("Lucy: goto riverbank");
        String response = sendCommandToServer("Lucy: bridge river and blow horn");
        assertTrue(response.contains("Multiple extraneous entities"), "Should fail due to extraneous entities for each other");

        sendCommandToServer("Lucy: get horn");
        response = sendCommandToServer("Lucy: blow horn and bridge river");
        assertTrue(response.contains("Multiple extraneous entities"), "Should fail due to extraneous entities for each other");
    }
}
