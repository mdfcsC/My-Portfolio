package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MyDbTests {
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

    // Test the DROP DATABASE command
    @Test
    public void testDropDatabaseCommand() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK"), "A valid DROP query should return [OK]");
    }

    // Test the DROP TABLE command
    @Test
    public void testDropTableCommand() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();

        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age);");

        // Drop the table and verify
        String response = sendCommandToServer("DROP TABLE " + randomTableName + ";");
        assertTrue(response.contains("[OK]"), "A valid DROP TABLE query should return [OK]");

        // Try to select from the dropped table
        response = sendCommandToServer("SELECT * FROM " + randomTableName + ";");
        assertTrue(response.contains("[ERROR]"), "Selecting from a dropped table should return [ERROR]");
    }

    // Test the ALTER ADD command
    @Test
    public void testAlterTableAddCommand() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();

        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age);");

        // Add a new column
        String response = sendCommandToServer("ALTER TABLE " + randomTableName + " ADD email;");
        assertTrue(response.contains("[OK]"), "Adding a column to a table should return [OK]");

        // Insert a record with the new column
        sendCommandToServer("INSERT INTO " + randomTableName + " VALUES ('John', 25, 'john@example.com');");

        // Verify the new column exists
        response = sendCommandToServer("SELECT * FROM " + randomTableName + ";");
        assertTrue(response.contains("email"), "The newly added column should be present in the results");
        assertTrue(response.contains("john@example.com"), "The value for the new column should be present");
    }

    // Test the ALTER DROP command
    @Test
    public void testAlterTableDropCommand() {
        String randomDBName = generateRandomName();
        String randomTableName = generateRandomName();

        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE " + randomTableName + " (name, age, email);");

        // Drop a column
        String response = sendCommandToServer("ALTER TABLE " + randomTableName + " DROP email;");
        assertTrue(response.contains("[OK]"), "Dropping a column from a table should return [OK]");

        // Insert a record without the dropped column
        sendCommandToServer("INSERT INTO " + randomTableName + " VALUES ('John', 25);");

        // Verify the column was dropped
        response = sendCommandToServer("SELECT * FROM " + randomTableName + ";");
        assertFalse(response.contains("email"), "The dropped column should not be present in the results");
    }

    // Test the SELECT command with condition
    @Test
    public void testSelectCommand() {
        String randomDBName = generateRandomName();

        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE students (name, age, grade);");
        sendCommandToServer("INSERT INTO students VALUES ('Alice', 20, 'A');");
        sendCommandToServer("INSERT INTO students VALUES ('Bob', 22, 'B');");
        sendCommandToServer("INSERT INTO students VALUES ('Charlie', 21, 'A');");

        // Select with single condition
        String response = sendCommandToServer("SELECT * FROM students WHERE grade == 'A';");
        assertTrue(response.contains("[OK]"), "A valid SELECT query should return [OK]");
        assertTrue(response.contains("Alice"), "Alice has grade A and should be in the results");
        assertTrue(response.contains("Charlie"), "Charlie has grade A and should be in the results");
        assertFalse(response.contains("Bob"), "Bob has grade B and should not be in the results");

        // Select with complex condition
        response = sendCommandToServer("SELECT * FROM students WHERE age > 20 AND grade == 'A';");
        assertTrue(response.contains("[OK]"), "A valid SELECT query should return [OK]");
        assertFalse(response.contains("Alice"), "Alice is 20 years old and should not be in the results");
        assertTrue(response.contains("Charlie"), "Charlie is 21 with grade A and should be in the results");
    }

    // Test the UPDATE command
    @Test
    public void testUpdateCommand() {
        String randomDBName = generateRandomName();

        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE employees (name, position, salary);");
        sendCommandToServer("INSERT INTO employees VALUES ('Alice', 'Developer', 50000);");
        sendCommandToServer("INSERT INTO employees VALUES ('Bob', 'Designer', 45000);");

        // Update a single record
        String response = sendCommandToServer("UPDATE employees SET salary=55000 WHERE name=='Alice';");
        assertTrue(response.contains("[OK]"), "A valid UPDATE query should return [OK]");

        // Verify the update was applied
        response = sendCommandToServer("SELECT * FROM employees WHERE name=='Alice';");
        assertTrue(response.contains("55000"), "Alice's salary should have been updated to 55000");

        // Update multiple records
        response = sendCommandToServer("UPDATE employees SET position='Senior' WHERE salary>=45000;");
        assertTrue(response.contains("[OK]"), "A valid UPDATE query should return [OK]");

        // Verify the updates were applied
        response = sendCommandToServer("SELECT * FROM employees;");
        assertTrue(response.contains("Alice") && response.contains("Senior"), "Alice's position should be updated to Senior");
        assertTrue(response.contains("Bob") && response.contains("Senior"), "Bob's position should be updated to Senior");
    }

    // Test the DELETE command
    @Test
    public void testDeleteCommand() {
        String randomDBName = generateRandomName();

        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");
        sendCommandToServer("CREATE TABLE products (name, category, price);");
        sendCommandToServer("INSERT INTO products VALUES ('Laptop', 'Electronics', 1000);");
        sendCommandToServer("INSERT INTO products VALUES ('Phone', 'Electronics', 500);");
        sendCommandToServer("INSERT INTO products VALUES ('Desk', 'Furniture', 200);");

        // Delete a single record
        String response = sendCommandToServer("DELETE FROM products WHERE name=='Laptop';");
        assertTrue(response.contains("[OK]"), "A valid DELETE query should return [OK]");

        // Verify the record was deleted
        response = sendCommandToServer("SELECT * FROM products;");
        assertFalse(response.contains("Laptop"), "Laptop should have been deleted");
        assertTrue(response.contains("Phone"), "Phone should still be in the table");
        assertTrue(response.contains("Desk"), "Desk should still be in the table");

        // Delete records with a condition affecting multiple rows
        response = sendCommandToServer("DELETE FROM products WHERE category=='Electronics';");
        assertTrue(response.contains("[OK]"), "A valid DELETE query should return [OK]");

        // Verify the records were deleted
        response = sendCommandToServer("SELECT * FROM products;");
        assertFalse(response.contains("Phone"), "Phone should have been deleted");
        assertTrue(response.contains("Desk"), "Desk should still be in the table");
    }

    // Test the JOIN command
    @Test
    public void testJoinCommand() {
        String randomDBName = generateRandomName();

        sendCommandToServer("CREATE DATABASE " + randomDBName + ";");
        sendCommandToServer("USE " + randomDBName + ";");

        // Create students table
        // id should be auto-generated
        sendCommandToServer("CREATE TABLE students (name, age);");
        sendCommandToServer("INSERT INTO students VALUES ('Alice', 20);");
        sendCommandToServer("INSERT INTO students VALUES ('Bob', 22);");
        sendCommandToServer("INSERT INTO students VALUES ('Charlie', 21);");

        // Create courses table
        sendCommandToServer("CREATE TABLE courses (studentId, courseName, grade);");
        sendCommandToServer("INSERT INTO courses VALUES (3, 'History', 'A');");
        sendCommandToServer("INSERT INTO courses VALUES (1, 'Science', 'B');");
        sendCommandToServer("INSERT INTO courses VALUES (2, 'Math', 'C');");

        // Test JOIN
        String response = sendCommandToServer("JOIN students AND courses ON id AND studentId;");
        assertTrue(response.contains("[OK]"), "A valid JOIN query should return [OK]");

        // Verify join results
        assertTrue(response.contains("Alice") && response.contains("Science"), "Alice is taking Science");
        assertTrue(response.contains("Bob") && response.contains("Math"), "Bob is taking Math");
        assertTrue(response.contains("Charlie") && response.contains("History"), "Charlie is taking History");
    }
}
