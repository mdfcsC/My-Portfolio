package edu.uob.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class EntityParserTests {
    private EntityParser parser;

    @BeforeEach
    void setUp() {
        try {
            File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
            this.parser = new EntityParser(entitiesFile);
        } catch (Exception e) {
            fail("[TEST] Failed to set up parser: " + e.getMessage());
        }
    }

    @Test
    void testParseLocation() {
        assertEquals(4, this.parser.getLocationMap().size(), "Should have parsed 4 locations: cabin, forest, cellar, storeroom");
        assertEquals("cabin", this.parser.getStartLocation().getName(), "Should have parsed start location: cabin");
        assertEquals("storeroom", this.parser.getStoreroom().getName(), "Should have parsed store location: storeroom");

        assertTrue(this.parser.getLocationMap().get("cellar").getPaths().contains("cabin"), "Should have parsed that cellar can lead to cabin");
    }

    @Test
    void testParseEntities() {
        assertEquals(11, this.parser.getEntityMap().size(), "Should have parsed number of all entities: 11");
        assertTrue(this.parser.getLocationMap().get("forest").getArtefacts().containsKey("key"), "Should have a key artefact in forest!");
        assertTrue(this.parser.getLocationMap().get("forest").getFurniture().containsKey("tree"), "Should have a tree furniture in forest!");
    }
}
