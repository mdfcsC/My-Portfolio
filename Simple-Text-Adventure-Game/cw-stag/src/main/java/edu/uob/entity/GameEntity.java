package edu.uob.entity;

public abstract class GameEntity {
    private String name;
    private String description;
    private EntityType entityType;

    /** name of location where this entity is
        <br>same as name for Location
        <br>null if this entity is taken by player
        <br>seems not useful for Player (see Player as a GameEntity as well)
        <br>livingRoom is a feature that was added after the basic structure was written
    */
    private String livingRoom;

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

    /** name of location where this entity is
     * <br>same as name for Location
     * <br>null if this entity is taken by player
     */
    public String getLivingRoom() {
        return this.livingRoom;
    }

    /** name of location where this entity is
     * <br>same as name for Location
     * <br>null if this entity is taken by player
     */
    public void setLivingRoom(String livingRoom) {
        this.livingRoom = livingRoom;
    }
}
