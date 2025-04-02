package edu.uob.extended;

import com.alexmerz.graphviz.ParseException;
import edu.uob.GameServer;
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

public class SinglePlayerTests {

    private GameServer server;
    private ActionParser actionParser;
    private EntityParser entityParser;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() throws IOException, ParseException, ParserConfigurationException, SAXException {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        this.actionParser = new ActionParser(actionsFile);
        this.entityParser = new EntityParser(entitiesFile);
        this.server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    void testConfigFilesLoading() {
        assertEquals(6, this.entityParser.getLocationMap().size(), "Location numbers should be 6");
        assertEquals(21, this.entityParser.getEntityMap().size(), "Total Entity numbers should be 21");
        assertEquals(8, this.actionParser.getGameActions().size(), "Total custom actions should be 8");
    }

    @Test
    void testBuildNewPath() {
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: get key");
        sendCommandToServer("Lucy: goto cabin");
        sendCommandToServer("Lucy: unlock trapdoor");
        String response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("cellar"), "Now should unlock a new path");
    }

    @Test
    void testHealth() {
        String response = sendCommandToServer("Lucy: health");
        assertTrue(response.contains("level: 3"), "Initial health level should be 3");

        sendCommandToServer("Lucy: get potion");
        response = sendCommandToServer("Lucy: drink potion");
        assertTrue(response.contains("You drink the potion and your health improves"), "Drinking potion can be performed successfully even with full blood.");

        response = sendCommandToServer("Lucy: health");
        assertTrue(response.contains("level: 3"), "Max health level is no more than 3");
    }

    @Test
    void testRestoreHealth() {
        sendCommandToServer("Lucy: get potion");
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: get key");
        sendCommandToServer("Lucy: goto cabin");
        sendCommandToServer("Lucy: unlock trapdoor");
        sendCommandToServer("Lucy: goto cellar");
        String response = sendCommandToServer("Lucy: fight elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"), "Should do \"fight elf\" action");

        response = sendCommandToServer("Lucy: health");
        assertTrue(response.contains("level: 2"), "Fighting elf would reduce 1 health");

        sendCommandToServer("Lucy: drink potion");
        response = sendCommandToServer("Lucy: health");
        assertTrue(response.contains("level: 3"), "Drinking potion can restore 1 health");
    }

    @Test
    void testStoreroom() {
        sendCommandToServer("Lucy: get axe");
        String response = sendCommandToServer("Lucy: goto forest");
        assertFalse(response.contains("log"), "Now there should not be a log in forest");

        sendCommandToServer("Lucy: cut down tree");
        response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("log"), "The log should be produced after cutting down tree");
        assertFalse(response.contains("tree"), "The tree should no longer be in forest");
        assertFalse(this.entityParser.getStoreroom().getArtefacts().containsKey("log"), "Now there should not be log in storeroom");
        assertTrue(this.entityParser.getStoreroom().getFurniture().containsKey("tree"), "Now there should be a tree in storeroom");

        response = sendCommandToServer("Lucy: get log");
        assertTrue(response.contains("Taken"), "The log produced should be an artefact");
    }

    @Test
    void testNotHaveSubject() {
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: get key");
        sendCommandToServer("Lucy: goto cabin");
        sendCommandToServer("Lucy: unlock trapdoor");
        sendCommandToServer("Lucy: goto cellar");
        String response = sendCommandToServer("Lucy: pay elf");
        assertTrue(response.contains("You are unable to pay here"), "Should fail to pay elf without coin");
    }

    @Test
    void testProduceMultiThings() {
        sendCommandToServer("Lucy: get coin");
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: get key");
        sendCommandToServer("Lucy: goto cabin");
        sendCommandToServer("Lucy: unlock trapdoor");
        sendCommandToServer("Lucy: goto cellar");
        sendCommandToServer("Lucy: fight elf");
        String response = sendCommandToServer("Lucy: pay elf");
        assertTrue(response.contains("You pay the elf your silver coin and he produces a shovel"), "Fighting should not influence paying elf");

        sendCommandToServer("Lucy: get shovel"); // The entity should NOT automatically appear in a player's inventory
        sendCommandToServer("Lucy: goto cabin");
        sendCommandToServer("Lucy: get axe");
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: chop tree");
        sendCommandToServer("Lucy: get log");
        sendCommandToServer("Lucy: goto riverbank");
        sendCommandToServer("Lucy: bridge log");
        sendCommandToServer("Lucy: goto clearing");
        sendCommandToServer("Lucy: dig ground");
        response = sendCommandToServer("Lucy: get hole");
        assertTrue(response.contains("You cannot get that thing."), "Furniture \"hole\" should be produced in clearing");
        response = sendCommandToServer("Lucy: get gold");
        assertTrue(response.contains("Taken"), "Artefact \"gold\" should be produced in clearing");
    }

    @Test
    void testReborn() {
        sendCommandToServer("Lucy: get coin");
        sendCommandToServer("Lucy: goto forest");
        sendCommandToServer("Lucy: get key");
        sendCommandToServer("Lucy: goto cabin");
        sendCommandToServer("Lucy: unlock trapdoor");
        sendCommandToServer("Lucy: goto cellar");
        sendCommandToServer("Lucy: fight elf");
        sendCommandToServer("Lucy: fight elf");
        String response = sendCommandToServer("Lucy: inventory");
        assertTrue(response.contains("coin"), "Should have coin in player's inventory");

        response = sendCommandToServer("Lucy: fight elf");
        assertTrue(response.contains("You died and lost all of your items, you must return to the start of the game."), "Should die due to lose all blood");

        response = sendCommandToServer("Lucy: look");
        assertTrue(response.contains("cabin"), "Should reborn at the start location");
        response = sendCommandToServer("Lucy: health");
        assertTrue(response.contains("level: 3"), "Should restore all health");
        response = sendCommandToServer("Lucy: inv");
        assertTrue(response.contains("You have nothing."), "Should lose all item in player's inventory");

        response = sendCommandToServer("Lucy: goto cellar");
        assertTrue(response.contains("cellar"), "Established path should not disappear");
        assertTrue(response.contains("coin"), "Should drop belongings in the location where they ran out of health");
    }
}
