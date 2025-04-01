package edu.uob.extended;

import edu.uob.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MultiplePlayerTests {

    private GameServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        this.server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    void testJoinPlayer() {
        String response = sendCommandToServer("Lucy: ");
        assertTrue(response.contains("ERROR"), "No actual command should fail to execute");
        response = sendCommandToServer("Lucy: asdfghjkl");
        assertTrue(response.contains("ERROR"), "Invalid command should fail to execute");

        response = sendCommandToServer("Amy: look");
        assertFalse(response.contains("Lucy"), "Should not see a player who failed to join the game");

        response = sendCommandToServer("David: look");
        assertTrue(response.contains("Amy"), "Player should see another player who joined the game");
    }

    @Test
    void testSeeEachOther() {
        String response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("Lucy"), "Player should not see herself");

        response = sendCommandToServer("Amy: look");
        assertTrue(response.contains("Lucy"), "Player should see other player in the same place");

        response = sendCommandToServer("David: look");
        assertTrue(response.contains("Lucy"), "Player should see other player in the same place");
        assertTrue(response.contains("Amy"), "Player should see other player in the same place");
        assertFalse(response.contains("David"), "Player should not see herself");

        assertFalse(response.contains("lucy"), "Player name should be case-sensitive");

        sendCommandToServer("Amy: goto forest");
        response = sendCommandToServer("Amy: look");
        assertFalse(response.contains("Lucy"), "Player should not see other player in other place");
        assertFalse(response.contains("David"), "Player should not see other player in other place");
        assertFalse(response.contains("Players"), "Location details should not contain players if there is no other player");

        response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("David"), "Player should see other player in the same place");
        assertFalse(response.contains("Amy"), "Player should not see other player who move to other place");
    }

    @Test
    void testEntityBetweenPlayers() {
        String response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("axe"), "There is an axe in the start location");
        sendCommandToServer("Amy: get axe");
        response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("axe"), "Player cannot see the artefact taken by another player");

        sendCommandToServer("Amy: goto forest");
        sendCommandToServer("Lucy: goto forest");
        response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("log"), "There is no log in the initial forest");
        sendCommandToServer("Amy: cut tree");
        response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("log"), "There is a log in the forest after someone else produce it");

        response = sendCommandToServer("Lucy: get log");
        assertTrue(response.contains("Taken"), "Player can get something which not produced by herself");
        response = sendCommandToServer("Amy: get log");
        assertTrue(response.contains("You cannot get that thing."), "Player cannot get the artefact taken by another player");
    }
}
