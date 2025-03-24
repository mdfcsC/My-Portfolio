package edu.uob.entity;

public abstract class GameEntity
{
    private String name;
    private String description;
    private EntityType entityType;

    public GameEntity(String name, String description, EntityType entityType)
    {
        this.name = name;
        this.description = description;
        this.entityType = entityType;
    }

    public String getName()
    {
        return this.name;
    }

    public String getDescription()
    {
        return this.description;
    }

    public EntityType getType() {
        return this.entityType;
    }
}
