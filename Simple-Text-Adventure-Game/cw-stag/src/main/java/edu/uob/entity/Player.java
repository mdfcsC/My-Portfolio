package edu.uob.entity;

import java.util.HashMap;

public class Player extends GameEntity{
    private int maxHealth;
    private int health;
    // TODO: is it really a good idea to use HashMap rather than HashSet?
    private HashMap<String, GameEntity> inventory;
    private Location currentLocation;

    public Player(String playerName, int maxHealth, Location startLocation) {
        super(playerName, "Yourself, a human playing the game. ", EntityType.PLAYER);
        this.maxHealth = maxHealth;
        this.health = this.maxHealth;
        this.inventory = new HashMap<>();
        this.currentLocation = startLocation;
    }

    public boolean damageHealth(int amount) {
        if (this.health <= 0 || amount <= 0 || amount > this.maxHealth) {
            return false;
        }
        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
        }
        return true;
    }

    public boolean restoreHealth(int amount) {
        if (this.health <= 0 || amount <= 0 || amount > this.maxHealth) {
            return false;
        }
        this.health += amount;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
        return true;
    }

    public HashMap<String, GameEntity> getInventory() {
        return this.inventory;
    }

    // for game entity that already exists in the game system, just move between locations and player's inventory
    public void pushInventory(GameEntity gameEntity) {
        this.inventory.put(gameEntity.getName(), gameEntity);
    }

    public GameEntity popInventory(String name) {
        GameEntity entityToPop = this.inventory.get(name);
        this.inventory.remove(name);
        return entityToPop;
    }

    public Location getCurrentLocation() {
        return this.currentLocation;
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }
}
