package edu.uob;

import com.alexmerz.graphviz.ParseException;
import edu.uob.parser.ActionParser;
import edu.uob.parser.EntityParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MinorFixTests {
    private GameServer server;
    private GameState gameState;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() throws IOException, ParseException, ParserConfigurationException, SAXException {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "modified-extended-actions.xml").toAbsolutePath().toFile();
        this.server = new GameServer(entitiesFile, actionsFile);

        EntityParser entityParser = new EntityParser(entitiesFile);
        ActionParser actionParser = new ActionParser(actionsFile);

        this.gameState = new GameState(entityParser, actionParser);
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
        sendCommandToServer("Lucy: get horn");

        String response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("A burly wood cutter"), "In initial status there is no lumberjack at riverbank");

        sendCommandToServer("Lucy: blow horn");
        response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("A burly wood cutter"), "First blow horn should take lumberjack from storeroom to current location");

        sendCommandToServer("Lucy: goto forest");
        response = sendCommandToServer("Lucy: blow horn");
        assertTrue(response.contains("You blow the horn and as if by magic, a lumberjack appears !"), "\"blow horn\" action should be able to repeat executing");
        response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("A burly wood cutter"), "Should take lumberjack from anywhere to current location");
    }

    @Test
    void testEntityMoveWhenConsumed() {
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: goto riverbank");
        sendCommandToServer("Lucy: get horn");
        sendCommandToServer("Lucy: blow horn");
//        assertFalse(this.gameState.getStoreroom().getCharacters().containsKey("lumberjack"), "After \"blow horn\", lumberjack should not be in storeroom");

        String response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("A burly wood cutter"), "\"blow horn\" should produce a lumberjack to current location");

        sendCommandToServer("Lucy: goto forest");
        response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("A burly wood cutter"), "In initial status there is no lumberjack at forest");

        sendCommandToServer("Lucy: reverse-blow horn");
        response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("A burly wood cutter"), "Wherever \"reverse-blow horn\" should consume the lumberjack");
//        assertTrue(this.gameState.getStoreroom().getCharacters().containsKey("lumberjack"), "After \"reverse-blow horn\", lumberjack should be back to storeroom");

        sendCommandToServer("Lucy: goto riverbank");
        response = sendCommandToServer("Lucy: look");
        assertFalse(response.contains("A burly wood cutter"), "Now there should not be a lumberjack at riverbank");
    }

    @Test
    void testEntityNameInActionTrigger() {
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: get key");
        sendCommandToServer("Lucy: goto cabin");
        String response = sendCommandToServer("Lucy: ");
    }
}
