package edu.uob.entity;

import edu.uob.parser.EntityType;

public class Furniture extends GameEntity{
    public Furniture(String name, String description) {
        super(name, description, EntityType.FURNITURE);
    }
}
