package edu.uob.entity;

import edu.uob.parser.EntityType;

public class Artefact extends GameEntity{
    public Artefact(String name, String description) {
        super(name, description, EntityType.ARTEFACTS);
    }
}
