package edu.uob.entity;

import java.util.HashMap;
import java.util.HashSet;

public class Location extends GameEntity{
    private HashSet<String> paths; // a way to another location
    // is it really a good idea to use HashMap rather than HashSet?
    private HashMap<String, GameEntity> characters;
    private HashMap<String, GameEntity> artefacts;
    private HashMap<String, GameEntity> furniture;
    private HashSet<String> playersNames;

    public Location(String name, String description) {
        super(name, description, EntityType.LOCATION);
        this.paths = new HashSet<>();
        this.characters = new HashMap<>();
        this.artefacts = new HashMap<>();
        this.furniture = new HashMap<>();
        this.playersNames = new HashSet<>();
    }

    public void addPath(String locationName) {
        this.paths.add(locationName);
    }

    public void removePath(String locationName) {
        this.paths.remove(locationName);
    }

    public void addCharacter(String characterName, GameEntity character) {
        this.characters.put(characterName, character);
    }

    public void addArtefact(String artefactName, GameEntity artefact) {
        this.artefacts.put(artefactName, artefact);
    }

    public void addFurniture(String furnitureName, GameEntity furniture) {
        this.furniture.put(furnitureName, furniture);
    }

    public boolean hasPath(String toLocationName) {
        return this.paths.contains(toLocationName);
    }

    public HashSet<String> getPaths() {
        return this.paths;
    }

    public HashMap<String, GameEntity> getCharacters() {
        return this.characters;
    }

    public HashMap<String, GameEntity> getArtefacts() {
        return this.artefacts;
    }

    public HashMap<String, GameEntity> getFurniture() {
        return this.furniture;
    }

    // for artefact that already exists in the game system, just move between locations and player's inventory
    public void pushArtefact(GameEntity artefact) {
        this.pushEntity(artefact, this.artefacts);
//        artefact.setLivingRoom(super.getName());
//        this.artefacts.put(artefact.getName(), artefact);
    }

    public GameEntity popArtefact(String artefactName) {
        return this.popEntity(artefactName, this.artefacts);
//        GameEntity artefactToPop = this.artefacts.get(artefactName);
//        // remove mapping, but not delete the GameEntity?
//        this.artefacts.remove(artefactName);
//        return artefactToPop;
    }

    public void pushCharacter(GameEntity character) {
        this.pushEntity(character, this.characters);
//        character.setLivingRoom(super.getName());
//        this.characters.put(character.getName(), character);
    }

    public GameEntity popCharacter(String characterName) {
        return this.popEntity(characterName, this.characters);
//        GameEntity characterToPop = this.characters.get(characterName);
//        this.characters.remove(characterName);
//        return characterToPop;
    }

    public void pushFurniture(GameEntity furniture) {
        this.pushEntity(furniture, this.furniture);
//        furniture.setLivingRoom(super.getName());
//        this.furniture.put(furniture.getName(), furniture);
    }

    public GameEntity popFurniture(String furnitureName) {
        return this.popEntity(furnitureName, this.furniture);
//        GameEntity furnitureToPop = this.furniture.get(furnitureName);
//        this.furniture.remove(furnitureName);
//        return furnitureToPop;
    }

    /**
     * Generic helper method to add an entity to a specific collection
     *
     * @param <T> Type of game entity
     * @param entity Entity to add
     * @param collection Collection to add entity to
     */
    private <T extends GameEntity> void pushEntity(T entity, HashMap<String, GameEntity> collection) {
        entity.setLivingRoom(super.getName());
        collection.put(entity.getName(), entity);
    }

    /**
     * Generic helper method to remove an entity from a specific collection and return it.
     *
     * @SuppressWarnings("unchecked") is used here because we're performing a cast from GameEntity
     * to the generic type T. This cast is safe in our context because:
     * <br>1. T is constrained to extend GameEntity (T extends GameEntity)
     * <br>2. We maintain type consistency by storing only appropriate entity types in their
     *    respective collections (artefacts, characters, furniture)
     * <br>3. The calling methods ensure type safety by invoking specific pop methods for each entity type
     * <br>
     * <br>Without this annotation, the compiler would generate an "unchecked cast" warning due to Java's
     * type erasure mechanism, which prevents runtime verification of generic type parameters.
     *
     * @param <T> The type of entity to return, must extend GameEntity
     * @param entityName The name of the entity to remove
     * @param collection The collection from which to remove the entity
     * @return The removed entity cast to type T
     * @throws RuntimeException If the entity is not found in the collection
     */
    private <T extends GameEntity> T popEntity(String entityName, HashMap<String, GameEntity> collection) {
        @SuppressWarnings("unchecked")
        T entityToPop = (T) collection.get(entityName);
        collection.remove(entityName);
        return entityToPop;
    }

    public void addPlayer(String playerName) {
        this.playersNames.add(playerName);
    }

    public void removePlayer(String playerName) {
        this.playersNames.remove(playerName);
    }

    public HashSet<String> getPlayersNames() {
        return this.playersNames;
    }

//    @Override
//    public String getLivingRoom() {
//        return super.getName();
//    }
//
//    @Override
//    public void setLivingRoom(String livingRoom) {
//        super.setLivingRoom(super.getName());
//    }
}
