package edu.uob.parser;

import edu.uob.action.GameAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class ActionParserTests {
    private ActionParser parser;

    @BeforeEach
    void setUp() {
        try {
            File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
            this.parser = new ActionParser(actionsFile);
        } catch (Exception e) {
            fail("[TEST] Failed to set up parser: " + e.getMessage());
        }
    }

    @Test
    void testParseAction() {
     assertEquals(2, this.parser.getGameActions().size(), "Expected two actions");

     Iterator<GameAction> iterator = this.parser.getGameActions().iterator();
     GameAction action = iterator.next();
     assertTrue(action.getSubjects().contains("trapdoor"), "Should have a trapdoor subject in the first action");
     action = iterator.next();
     assertEquals(3, action.getTriggers().size(), "Expected three triggers in the second action");

     assertFalse(iterator.hasNext(), "Should only have two actions");
    }
}
