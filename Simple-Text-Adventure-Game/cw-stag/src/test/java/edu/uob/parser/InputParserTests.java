package edu.uob.parser;

import edu.uob.GameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputParserTests {

    private GameServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    void testMultiCommandVerb() {
        String response = sendCommandToServer("lucy: get and Look");
        assertTrue(response.contains("You can only issue one command at a time!"), "Should reject multiple built-in commands");

        response = sendCommandToServer("lucy: look, unlock the key");
        assertTrue(response.contains("You can only issue one command at a time!"), "Should reject multiple commands");

        sendCommandToServer("lucy: goto forest");
        sendCommandToServer("lucy: get key");
        sendCommandToServer("lucy: goto cabin");
        response = sendCommandToServer("lucy: unlock,open the trapdoor.");
        assertTrue(response.contains("You unlock the trapdoor and see steps leading down into a cellar"), "Verbs from the same action should not be considered as two different commands");
    }

    @Test
    void testMultiSubjects() {
        String response = sendCommandToServer("lucy: open the trapdoor, axe");
        assertTrue(response.contains("Multiple extraneous entities"), "Should reject multiple extraneous subjects");

        sendCommandToServer("lucy: get axe");
        sendCommandToServer("lucy: goto forest");
        response = sendCommandToServer("lucy: cut tree with axe");
        assertTrue(response.contains("You cut down the tree with the axe"), "Subjects from the same action should not be considered as two different commands");
    }

    @Test
    void testEasyAction() {
        sendCommandToServer("Lucy: get axe");
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: drop axe");
        String response = sendCommandToServer("Lucy: cut down tree");
        assertTrue(response.contains("You cut down the tree with the axe"), "Action should be performed as long as subjects all exist in current location or player's inventory");
    }

    @Test
    void testGotoCurrentLocation() {
        assertTrue(sendCommandToServer("Lucy: goto cabin").contains("You cannot go to that place"));
    }
}
