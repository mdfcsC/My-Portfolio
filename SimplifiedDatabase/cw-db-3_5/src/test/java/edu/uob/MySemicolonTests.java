package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MySemicolonTests {
    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName() {
        String randomName = "";
        for(int i=0; i<5 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // test use ends with a semicolon
    @Test
    public void testUseSemicolon() {
        String randomDBName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");

        String response = sendCommandToServer("USE " + randomDBName);
        assertTrue(response.contains("[ERROR]"), "USE command without semicolon should return [ERROR]");
    }

    // Test that CREATE DATABASE command requires a semicolon
    @Test
    public void testCreateDatabaseSemicolon() {
        String randomDBName = generateRandomName();

        String response = sendCommandToServer("CREATE DATABASE " + randomDBName);
        assertTrue(response.contains("[ERROR]"), "CREATE DATABASE command without semicolon should return [ERROR]");
    }

    // Test that CREATE TABLE command requires a semicolon
    @Test
    public void testCreateTableSemicolon() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");

        String response = sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age)");
        assertTrue(response.contains("[ERROR]"), "CREATE TABLE command without semicolon should return [ERROR]");
    }

    // Test that DROP DATABASE command requires a semicolon
    @Test
    public void testDropDatabaseSemicolon() {
        String randomDBName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");

        String response = sendCommandToServer("DROP DATABASE " + randomDBName);
        assertTrue(response.contains("[ERROR]"), "DROP DATABASE command without semicolon should return [ERROR]");
    }

    // Test that DROP TABLE command requires a semicolon
    @Test
    public void testDropTableSemicolon() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age);");

        String response = sendCommandToServer("DROP TABLE " + randomTableName);
        assertTrue(response.contains("[ERROR]"), "DROP TABLE command without semicolon should return [ERROR]");
    }

    // Test that ALTER TABLE command requires a semicolon
    @Test
    public void testAlterTableSemicolon() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age);");

        // Test ADD operation without semicolon
        String response = sendCommandToServer("ALTER TABLE " + randomTableName + " ADD email");
        assertTrue(response.contains("[ERROR]"), "ALTER TABLE ADD command without semicolon should return [ERROR]");

        // Add the column properly for DROP test
        sendCommandToServer("ALTER TABLE " + randomTableName + " ADD email;");

        // Test DROP operation without semicolon
        response = sendCommandToServer("ALTER TABLE " + randomTableName + " DROP email");
        assertTrue(response.contains("[ERROR]"), "ALTER TABLE DROP command without semicolon should return [ERROR]");
    }

    // Test that INSERT command requires a semicolon
    @Test
    public void testInsertSemicolon() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age);");

        String response = sendCommandToServer("INSERT INTO " + randomTableName + " VALUES ('John', 25)");
        assertTrue(response.contains("[ERROR]"), "INSERT command without semicolon should return [ERROR]");
    }

    // Test that SELECT command requires a semicolon
    @Test
    public void testSelectSemicolon() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age);");
        sendCommandToServer("INSERT INTO " + randomTableName + " VALUES ('John', 25);");

        // Test basic SELECT without semicolon
        String response = sendCommandToServer("SELECT * FROM " + randomTableName);
        assertTrue(response.contains("[ERROR]"), "SELECT command without semicolon should return [ERROR]");

        // Test SELECT with condition without semicolon
        response = sendCommandToServer("SELECT * FROM " + randomTableName + " WHERE age > 20");
        assertTrue(response.contains("[ERROR]"), "SELECT with condition command without semicolon should return [ERROR]");
    }

    // Test that UPDATE command requires a semicolon
    @Test
    public void testUpdateSemicolon() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age);");
        sendCommandToServer("INSERT INTO " + randomTableName + " VALUES ('John', 25);");

        String response = sendCommandToServer("UPDATE " + randomTableName + " SET age=30 WHERE name=='John'");
        assertTrue(response.contains("[ERROR]"), "UPDATE command without semicolon should return [ERROR]");
    }

    // Test that DELETE command requires a semicolon
    @Test
    public void testDeleteSemicolon() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age);");
        sendCommandToServer("INSERT INTO " + randomTableName + " VALUES ('John', 25);");

        String response = sendCommandToServer("DELETE FROM " + randomTableName + " WHERE name=='John'");
        assertTrue(response.contains("[ERROR]"), "DELETE command without semicolon should return [ERROR]");
    }

    // Test that JOIN command requires a semicolon
    @Test
    public void testJoinSemicolon() {
        String randomDBName = generateRandomName();
        String table1 = generateRandomName();
        String table2 = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + table1 + " (id, name);");
        sendCommandToServer("CREATE TABLE " + table2 + " (studentId, grade);");
        sendCommandToServer("INSERT INTO " + table1 + " VALUES (1, 'John');");
        sendCommandToServer("INSERT INTO " + table2 + " VALUES (1, 'A');");

        String response = sendCommandToServer("JOIN " + table1 + " AND " + table2 + " ON id AND studentId");
        assertTrue(response.contains("[ERROR]"), "JOIN command without semicolon should return [ERROR]");
    }
}
