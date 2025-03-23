package edu.uob.entity;

import edu.uob.parser.EntityType;

public class Character extends GameEntity {
    public Character(String name, String description) {
        super(name, description, EntityType.CHARACTERS);
    }
}
