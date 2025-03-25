package edu.uob.entity;

import java.util.HashMap;
import java.util.HashSet;

public class Location extends GameEntity{
    private HashSet<String> paths; // a way to another location
    // TODO: is it really a good idea to use HashMap rather than HashSet?
    private HashMap<String, GameEntity> characters;
    private HashMap<String, GameEntity> artefacts;
    private HashMap<String, GameEntity> furniture;

    public Location(String name, String description) {
        super(name, description, EntityType.LOCATION);
        this.paths = new HashSet<>();
        this.characters = new HashMap<>();
        this.artefacts = new HashMap<>();
        this.furniture = new HashMap<>();
    }

    public void addPath(String locationName) {
        this.paths.add(locationName);
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

    // for game entity that already exists in the game system, just move between locations and player's inventory
    public void pushArtefact(GameEntity artefact) {
        this.artefacts.put(artefact.getName(), artefact);
    }

    public GameEntity popArtefact(String artefactName) {
        GameEntity artefactToPop = this.artefacts.get(artefactName);
        // remove mapping, but not delete the GameEntity?
        this.artefacts.remove(artefactName);
        return artefactToPop;
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
}
