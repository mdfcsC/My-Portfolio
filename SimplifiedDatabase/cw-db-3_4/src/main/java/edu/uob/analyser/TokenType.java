package edu.uob.analyser;

public enum TokenType {
    // SELECT, CREATE, INSERT, UPDATE, DELETE, DROP, ALTER, USE, JOIN
    COMMAND,

    // FROM, WHERE, AND, OR, ON, SET, INTO, VALUES, TABLE, DATABASE, ADD, DROP, LIKE
    KEYWORD,

    // >, <, =, ==, !=, >=, <=
    OPERATOR,

    // ; , ( )
    DELIMITER,

    // ' "
    QUOTE,

    // names of databases, tables, attributes/columns
    IDENTIFIER,

    // *
    WILDCARD,

    // literal
    STRING_LITERAL,
    INT_LITERAL,
    FLOAT_LITERAL,
    BOOLEAN_LITERAL,
    NULL_LITERAL,

    // end mark
    EOF,
    // invalid mark
    INVALID,
    // other types
    OTHER
}
